package com.example.patientmanagementsystemmobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.PeopleAdapter;
import com.example.patientmanagementsystemmobile.models.Person;
import com.example.patientmanagementsystemmobile.models.User;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.request.AppointmentRequest;
import com.example.patientmanagementsystemmobile.response.AppointmentResponse;
import com.example.patientmanagementsystemmobile.response.AppointmentListResponse;
import com.example.patientmanagementsystemmobile.data.AppointmentData;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentFragment extends Fragment {

    private CalendarView calendarView;
    private TextView selectedDateText;
    private TextView restrictionText;
    private RecyclerView peopleRecyclerView;
    private PeopleAdapter peopleAdapter;
    private Map<String, List<Person>> scheduleData;
    private String selectedDate;
    private ApiService apiService;

    private User user;
    private boolean hasUpcomingAppointmentFlag = false;
    private String upcomingAppointmentDateStr = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        initViews(view);
        initApiService();
        initCurrentUser();
        setupRecyclerView();
        setupCalendar();
        loadScheduleData(); // Load data from API
        checkPatientAppointmentsOnLoad(); // Check if patient has upcoming appointments
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        selectedDateText = view.findViewById(R.id.selectedDateText);
        restrictionText = view.findViewById(R.id.restrictionText);
        peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        scheduleData = new HashMap<>();
    }

    private void initCurrentUser() {
        user = RetrofitClient.currentUser;
    }

    private void initApiService() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void checkPatientAppointmentsOnLoad() {
        String patientId = RetrofitClient.currentUser.getId();

        Call<AppointmentListResponse> call = apiService.getPatientAppointments(patientId);
        call.enqueue(new Callback<AppointmentListResponse>() {
            @Override
            public void onResponse(Call<AppointmentListResponse> call, Response<AppointmentListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppointmentListResponse appointmentListResponse = response.body();

                    if (appointmentListResponse.isSuccess() && appointmentListResponse.getData() != null) {
                        List<AppointmentData> appointments = appointmentListResponse.getData();
                        upcomingAppointmentDateStr = hasUpcomingAppointment(appointments);
                        hasUpcomingAppointmentFlag = (upcomingAppointmentDateStr != null);

                        // Update the UI after checking appointments
                        updatePeopleList();
                    }
                } else {
                    Log.e("APPOINTMENT_CHECK", "Failed to check appointments: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AppointmentListResponse> call, Throwable t) {
                Log.e("APPOINTMENT_CHECK", "Network error: " + t.getMessage());
            }
        });
    }

    private void refreshAppointmentStatusAndUI() {
        String patientId = RetrofitClient.currentUser.getId();

        Call<AppointmentListResponse> call = apiService.getPatientAppointments(patientId);
        call.enqueue(new Callback<AppointmentListResponse>() {
            @Override
            public void onResponse(Call<AppointmentListResponse> call, Response<AppointmentListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppointmentListResponse appointmentListResponse = response.body();

                    if (appointmentListResponse.isSuccess() && appointmentListResponse.getData() != null) {
                        List<AppointmentData> appointments = appointmentListResponse.getData();
                        upcomingAppointmentDateStr = hasUpcomingAppointment(appointments);
                        hasUpcomingAppointmentFlag = (upcomingAppointmentDateStr != null);

                        // Immediately update the UI to hide doctors list
                        updatePeopleList();
                    }
                } else {
                    Log.e("APPOINTMENT_REFRESH", "Failed to refresh appointments: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AppointmentListResponse> call, Throwable t) {
                Log.e("APPOINTMENT_REFRESH", "Network error: " + t.getMessage());
            }
        });
    }

    private void loadScheduleData() {
        // Load schedule with the currently selected date
        loadScheduleDataForDate(selectedDate);
    }

    private void loadScheduleDataForDate(String date) {
        // Show loading state if needed
        // progressBar.setVisibility(View.VISIBLE);

        Log.d("AppointmentFragment", "Loading schedule for date: " + date);

        Call<Map<String, Object>> call = apiService.getDoctorSchedule(date);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    parseScheduleData(response.body());
                    updatePeopleList(); // Update the list after data is loaded
                } else {
                    Log.e("API_ERROR", "Failed to load schedule: " + response.code());
                    showError("Failed to load doctor schedule");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Network error: " + t.getMessage());
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void parseScheduleData(Map<String, Object> responseData) {
        try {
            // Clear existing data
            scheduleData.clear();

            for (Map.Entry<String, Object> entry : responseData.entrySet()) {
                String dayKey = entry.getKey(); // e.g., "mondayPeople", "tuesdayPeople"

                if (entry.getValue() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> doctorsList = (List<Map<String, Object>>) entry.getValue();
                    Log.d("Appointment list", "doctor list for: " + doctorsList.toString());
                    List<Person> people = new ArrayList<>();
                    for (Map<String, Object> doctorData : doctorsList) {
                        Person person = parseDoctorToPerson(doctorData);
                        if (person != null) {
                            people.add(person);
                        }
                    }

                    // Convert day key to actual dates
                    assignPeopleToActualDates(dayKey, people);
                }
            }
        } catch (Exception e) {
            Log.e("PARSE_ERROR", "Error parsing schedule data: " + e.getMessage());
            showError("Error processing schedule data");
        }
    }

    private Person parseDoctorToPerson(Map<String, Object> doctorData) {
        try {
            String name = (String) doctorData.get("name");
            if (name != null && !name.startsWith("Dr. ")) {
                name = "Dr. " + name;
            }

            String specialty = (String) doctorData.get("specialty");
            if (specialty == null) {
                specialty = "Obstretrician - Gynecologyst";
            }

            String schedule = (String) doctorData.get("schedule");
            if (schedule == null) {
                schedule = "Schedule not set";
            }

            boolean isAvailable = true;
            if (doctorData.containsKey("is_available")) {
                Object availableObj = doctorData.get("is_available");
                if (availableObj instanceof Boolean) {
                    isAvailable = (Boolean) availableObj;
                } else if (availableObj instanceof Number) {
                    isAvailable = ((Number) availableObj).intValue() == 1;
                }
            }

            String id = (String) doctorData.get("id");

            return new Person(id, name, specialty, schedule, isAvailable);
        } catch (Exception e) {
            Log.e("PARSE_ERROR", "Error parsing doctor data: " + e.getMessage());
            return null;
        }
    }

    private void assignPeopleToActualDates(String dayKey, List<Person> people) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Map day keys to Calendar day constants
        int targetDayOfWeek = getDayOfWeekFromKey(dayKey);
        if (targetDayOfWeek == -1) return;

        // Generate dates for the next few weeks
        int daysAdded = 0;
        int totalDays = 0;

        while (daysAdded < 7 && totalDays < 9) { // Get 4 occurrences of each day
            int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            if (currentDayOfWeek == targetDayOfWeek && totalDays > 0) {
                String dateKey = dateFormat.format(cal.getTime());
                scheduleData.put(dateKey, new ArrayList<>(people));
                daysAdded++;
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
            totalDays++;
        }
    }

    private int getDayOfWeekFromKey(String dayKey) {
        switch (dayKey.toLowerCase()) {
            case "mondaypeople":
                return Calendar.MONDAY;
            case "tuesdaypeople":
                return Calendar.TUESDAY;
            case "wednesdaypeople":
                return Calendar.WEDNESDAY;
            case "thursdaypeople":
                return Calendar.THURSDAY;
            case "fridaypeople":
                return Calendar.FRIDAY;
            case "saturdaypeople":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }

    private void setupCalendar() {
        // Set initial selected date to today
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dateFormat.format(today.getTime());
        updateSelectedDateDisplay();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, dayOfMonth);
                selectedDate = dateFormat.format(selectedCal.getTime());

                Log.d("AppointmentFragment", "Calendar date changed to: " + selectedDate);

                updateSelectedDateDisplay();

                // Reload schedule data with the new selected date to check doctor availability
                loadScheduleDataForDate(selectedDate);
            }
        });
    }

    private void setupRecyclerView() {
        if (getContext() != null) {
            peopleAdapter = new PeopleAdapter(new ArrayList<>());
            peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            peopleRecyclerView.setAdapter(peopleAdapter);

            peopleAdapter.setOnPersonClickListener(person -> {
                if (person.isAvailable()) {
                    showBookingDialog(person);
                } else {
                    Toast.makeText(getContext(), person.getName() + " is not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showBookingDialog(Person person) {
        // First, check if the patient has any upcoming appointments
        checkUpcomingAppointments(person);
    }

    private void checkUpcomingAppointments(Person person) {
        String patientId = RetrofitClient.currentUser.getId();

        Call<AppointmentListResponse> call = apiService.getPatientAppointments(patientId);
        call.enqueue(new Callback<AppointmentListResponse>() {
            @Override
            public void onResponse(Call<AppointmentListResponse> call, Response<AppointmentListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppointmentListResponse appointmentListResponse = response.body();

                    if (appointmentListResponse.isSuccess() && appointmentListResponse.getData() != null) {
                        List<AppointmentData> appointments = appointmentListResponse.getData();

                        // Check if there's any upcoming appointment
                        String upcomingAppointmentDate = hasUpcomingAppointment(appointments);

                        if (upcomingAppointmentDate != null) {
                            // Patient has an upcoming appointment, show restriction message
                            showAppointmentRestrictionDialog(upcomingAppointmentDate);
                        } else {
                            // No upcoming appointment, proceed with booking
                            showConfirmBookingDialog(person);
                        }
                    } else {
                        // No appointments found, proceed with booking
                        showConfirmBookingDialog(person);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to check existing appointments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String hasUpcomingAppointment(List<AppointmentData> appointments) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = new Date();

            for (AppointmentData appointment : appointments) {
                String appointmentDateStr = appointment.getAppointment_date();
                Date appointmentDate = dateFormat.parse(appointmentDateStr);

                // Check if appointment date is today or in the future
                if (appointmentDate != null && !appointmentDate.before(today)) {
                    // Format the date for display
                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    return displayFormat.format(appointmentDate);
                }
            }
        } catch (Exception e) {
            Log.e("APPOINTMENT_CHECK", "Error checking upcoming appointments: " + e.getMessage());
        }

        return null; // No upcoming appointments
    }

    private void showAppointmentRestrictionDialog(String upcomingDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Booking Restricted");
        builder.setMessage("You already have an upcoming appointment on " + upcomingDate +
                ". You can only book a new appointment after this date has passed.");

        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfirmBookingDialog(Person person) {
        // Navigate to Service Selection Fragment instead of booking directly
        navigateToServiceSelection(person);
    }

    private void navigateToServiceSelection(Person person) {
        String patientId = RetrofitClient.currentUser.getId();

        ServiceSelectionFragment serviceFragment = ServiceSelectionFragment.newInstance(
                person,
                selectedDate,
                patientId
        );

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, serviceFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void bookAppointment(Person person) {
        String id = RetrofitClient.currentUser.getId();
        String formattedDate = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            format.parse(selectedDate); // Just validate
            formattedDate = selectedDate;
        } catch (Exception e) {
            formattedDate = selectedDate;
        }
        // Add your booking logic here
        // e.g., call API, save to database, etc.
//        Log.d("AppointmentBooking", "Booking appointment for: " + person.toString() + "ID of Patient use: " + formattedDate + " " + person.getSchedule());
//        Log.d("User: ", "User Details" + id);
        AppointmentRequest request = new AppointmentRequest(
                id,
                person.getId(), // doctor ID
                formattedDate, // format: "2024-12-25"
                person.getSchedule(), // format: "14:30"
                "" // notes
        );
//
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<AppointmentResponse> call = apiService.createAppointment(request);

        call.enqueue(new Callback<AppointmentResponse>() {
            @Override
            public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppointmentResponse appointmentResponse = response.body();
                    if (appointmentResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Appointment booked successfully!", Toast.LENGTH_LONG).show();
                        // Immediately refresh appointment status to hide doctors list
                        refreshAppointmentStatusAndUI();
                    } else {
                        Toast.makeText(getContext(), appointmentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to book appointment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelectedDateDisplay() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(selectedDate);
            String formattedDate = outputFormat.format(date);
            selectedDateText.setText("Available on " + formattedDate);
        } catch (Exception e) {
            selectedDateText.setText("Available on " + selectedDate);
        }
    }

    private void updatePeopleList() {
        // Check if patient has upcoming appointment
        if (hasUpcomingAppointmentFlag) {
            // Hide the doctors list
            peopleRecyclerView.setVisibility(View.GONE);
            // Show restriction message with the appointment date
            restrictionText.setText("You have an upcoming appointment on " + upcomingAppointmentDateStr +
                    ". You can book again after your appointment date has passed.");
            restrictionText.setVisibility(View.VISIBLE);
        } else {
            // Show the doctors list normally
            peopleRecyclerView.setVisibility(View.VISIBLE);
            restrictionText.setVisibility(View.GONE);

            List<Person> availablePeople = scheduleData.get(selectedDate);
            if (availablePeople == null) {
                availablePeople = new ArrayList<>();
            }

            if (peopleAdapter != null) {
                peopleAdapter.updatePeople(availablePeople);
            }
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    // Method to refresh data
    public void refreshScheduleData() {
        loadScheduleData();
    }

    // Method to add schedule data programmatically
    public void addScheduleForDate(String date, List<Person> people) {
        scheduleData.put(date, people);
        if (selectedDate.equals(date)) {
            updatePeopleList();
        }
    }

    // Method to get schedule data for a specific date
    public List<Person> getScheduleForDate(String date) {
        return scheduleData.get(date);
    }
}