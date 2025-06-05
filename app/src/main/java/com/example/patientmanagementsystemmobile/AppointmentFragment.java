package com.example.patientmanagementsystemmobile;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.PeopleAdapter;
import com.example.patientmanagementsystemmobile.models.Person;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentFragment extends Fragment {

    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView peopleRecyclerView;
    private PeopleAdapter peopleAdapter;
    private Map<String, List<Person>> scheduleData;
    private String selectedDate;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);

        initViews(view);
        initApiService();
        setupRecyclerView();
        setupCalendar();
        loadScheduleData(); // Load data from API

        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        selectedDateText = view.findViewById(R.id.selectedDateText);
        peopleRecyclerView = view.findViewById(R.id.peopleRecyclerView);
        scheduleData = new HashMap<>();
    }

    private void initApiService() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void loadScheduleData() {
        // Show loading state if needed
        // progressBar.setVisibility(View.VISIBLE);

        Call<Map<String, Object>> call = apiService.getDoctorSchedule();
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
                specialty = "General Practice";
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

            return new Person(name, specialty, schedule, isAvailable);
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

        while (daysAdded < 4 && totalDays < 28) { // Get 4 occurrences of each day
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

                updateSelectedDateDisplay();
                updatePeopleList();
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
                    Toast.makeText(getContext(), "Booking appointment with " + person.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), person.getName() + " is not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        List<Person> availablePeople = scheduleData.get(selectedDate);
        if (availablePeople == null) {
            availablePeople = new ArrayList<>();
        }

        if (peopleAdapter != null) {
            peopleAdapter.updatePeople(availablePeople);
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