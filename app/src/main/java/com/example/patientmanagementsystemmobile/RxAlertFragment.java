package com.example.patientmanagementsystemmobile;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.patientmanagementsystemmobile.adapter.MedicationAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import com.example.patientmanagementsystemmobile.models.MedicationAlarmDisplayItem;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.MedicationAlertRequest;
import com.example.patientmanagementsystemmobile.response.MedicationAlertResponse;
import com.example.patientmanagementsystemmobile.utils.AlarmScheduler;
import com.example.patientmanagementsystemmobile.utils.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RxAlertFragment extends Fragment implements MedicationAdapter.OnMedicationActionListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<MedicationAlert> medicationList;
    private FloatingActionButton fabAddAlert;
    private ApiService apiService;
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    public RxAlertFragment() {
        // Required empty public constructor
    }

    public static RxAlertFragment newInstance(String param1, String param2) {
        RxAlertFragment fragment = new RxAlertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize medication list
        medicationList = new ArrayList<>();

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Create notification channel
        NotificationHelper.createNotificationChannel(requireContext());

        // Save patient ID for boot receiver
        savePatientIdToPreferences();
    }

    private void savePatientIdToPreferences() {
        if (RetrofitClient.currentUser != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().putString("patient_id", RetrofitClient.currentUser.getId()).apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rx_alert, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMedications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter with empty display items list
        adapter = new MedicationAdapter(new ArrayList<>(), getContext());
        adapter.setOnMedicationActionListener(this);
        recyclerView.setAdapter(adapter);

        // Initialize FAB
        fabAddAlert = view.findViewById(R.id.fabAddAlert);
        fabAddAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEditDialog(null, -1);
            }
        });

        // Check permissions
        checkAndRequestExactAlarmPermission();

        // Load medication alerts from API
        loadMedicationAlertsFromAPI();

        // Setup auto-refresh for progress bars (every 30 seconds)
        setupAutoRefresh();

        return view;
    }

    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Refresh adapter to update progress bars
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                // Schedule next refresh in 30 seconds
                refreshHandler.postDelayed(this, 30000);
            }
        };
        // Start the refresh cycle
        refreshHandler.postDelayed(refreshRunnable, 30000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the refresh handler when view is destroyed
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void checkAndRequestExactAlarmPermission() {
        if (!AlarmScheduler.canScheduleExactAlarms(requireContext())) {
            Toast.makeText(getContext(),
                "Please grant exact alarm permission for medication reminders to work properly",
                Toast.LENGTH_LONG).show();
            AlarmScheduler.requestExactAlarmPermission(requireContext());
        }
    }

    private void loadMedicationAlertsFromAPI() {
        String patientId = RetrofitClient.currentUser.getId();

        Call<MedicationAlertResponse> call = apiService.getPatientMedicationAlerts(patientId);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess() && alertResponse.getData() != null) {
                        medicationList.clear();

                        // Convert API data to MedicationAlert objects
                        for (MedicationAlertResponse.MedicationAlertData data : alertResponse.getData()) {
                            MedicationAlert medication = data.toMedicationAlert();
                            medicationList.add(medication);

                            // Schedule alarm for enabled medications
                            if (medication.isEnabled()) {
                                AlarmScheduler.scheduleAlarm(requireContext(), medication);
                            }
                        }

                        // Update adapter with expanded display items
                        if (adapter != null) {
                            List<MedicationAlarmDisplayItem> displayItems = expandMedicationsToDisplayItems(medicationList);
                            adapter.updateList(displayItems);
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Failed to load medication alerts: " + response.code());
                    Toast.makeText(getContext(), "Failed to load medication alerts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Log.e("API_ERROR", "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Expand medications into display items.
     * For prescribed pieces medications, create one display item per alarm.
     * For legacy medications, create a single display item.
     */
    private List<MedicationAlarmDisplayItem> expandMedicationsToDisplayItems(List<MedicationAlert> medications) {
        List<MedicationAlarmDisplayItem> displayItems = new ArrayList<>();

        for (MedicationAlert medication : medications) {
            if (medication.getPrescribedPieces() > 0 && medication.getAlarmTimes() != null && !medication.getAlarmTimes().isEmpty()) {
                // Prescribed pieces: create one display item per alarm
                for (int i = 0; i < medication.getAlarmTimes().size(); i++) {
                    displayItems.add(new MedicationAlarmDisplayItem(medication, i));
                }
            } else {
                // Legacy: single display item
                displayItems.add(new MedicationAlarmDisplayItem(medication, -1));
            }
        }

        return displayItems;
    }

    private void showAddEditDialog(MedicationAlert medication, int position) {
        boolean isEdit = medication != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isEdit ? "Edit Medication Alert" : "Add Medication Alert");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medication_alert, null);
        EditText editMedicationName = dialogView.findViewById(R.id.editTextMedicationName);
        EditText editPrescribedPieces = dialogView.findViewById(R.id.editTextPrescribedPieces);
        android.widget.Spinner spinnerTimesPerDay = dialogView.findViewById(R.id.spinnerTimesPerDay);
        EditText editFirstDoseTime = dialogView.findViewById(R.id.editTextFirstDoseTime);
        EditText editRemarks = dialogView.findViewById(R.id.editTextRemarks);

        // Setup Times Per Day Spinner
        String[] timesPerDayOptions = {"1 time per day", "2 times per day", "3 times per day", "4 times per day"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, timesPerDayOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimesPerDay.setAdapter(spinnerAdapter);

        // Day selection buttons (single select for start day)
        TextView dayMonday = dialogView.findViewById(R.id.dayMonday);
        TextView dayTuesday = dialogView.findViewById(R.id.dayTuesday);
        TextView dayWednesday = dialogView.findViewById(R.id.dayWednesday);
        TextView dayThursday = dialogView.findViewById(R.id.dayThursday);
        TextView dayFriday = dialogView.findViewById(R.id.dayFriday);
        TextView daySaturday = dialogView.findViewById(R.id.daySaturday);
        TextView daySunday = dialogView.findViewById(R.id.daySunday);

        TextView[] dayButtons = {dayMonday, dayTuesday, dayWednesday, dayThursday, dayFriday, daySaturday, daySunday};
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        final int[] selectedDayIndex = {-1}; // Only one day can be selected

        // First Dose Time picker
        editFirstDoseTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, selectedHour, selectedMinute) -> {
                        String period = selectedHour >= 12 ? "PM" : "AM";
                        int displayHour = selectedHour % 12;
                        if (displayHour == 0) displayHour = 12;

                        String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s",
                                displayHour, selectedMinute, period);
                        editFirstDoseTime.setText(formattedTime);
                    }, hour, minute, false);

            timePickerDialog.show();
        });

        // If editing, populate fields
        if (isEdit) {
            editMedicationName.setText(medication.getMedicationName());
            editRemarks.setText(medication.getRemarks());

            if (medication.getPrescribedPieces() > 0) {
                editPrescribedPieces.setText(String.valueOf(medication.getPrescribedPieces()));
            }

            if (medication.getTimesPerDay() > 0 && medication.getTimesPerDay() <= 4) {
                spinnerTimesPerDay.setSelection(medication.getTimesPerDay() - 1);
            }

            if (medication.getFirstDoseTime() != null && medication.getFirstDosePeriod() != null) {
                editFirstDoseTime.setText(medication.getFirstDoseTime() + " " + medication.getFirstDosePeriod());
            }

            // Set start day
            if (medication.getStartDay() != null) {
                for (int i = 0; i < dayNames.length; i++) {
                    if (dayNames[i].equals(medication.getStartDay())) {
                        selectedDayIndex[0] = i;
                        dayButtons[i].setBackgroundResource(R.drawable.day_button_selected);
                        dayButtons[i].setTextColor(0xFFFFFFFF);
                        break;
                    }
                }
            }
        }

        // Add click listeners for day buttons (single select)
        for (int i = 0; i < dayButtons.length; i++) {
            final int dayIndex = i;
            dayButtons[i].setOnClickListener(v -> {
                // Deselect previously selected day
                if (selectedDayIndex[0] >= 0 && selectedDayIndex[0] < dayButtons.length) {
                    dayButtons[selectedDayIndex[0]].setBackgroundResource(R.drawable.day_button_unselected);
                    dayButtons[selectedDayIndex[0]].setTextColor(0xFF2196F3);
                }

                // Select new day
                selectedDayIndex[0] = dayIndex;
                dayButtons[dayIndex].setBackgroundResource(R.drawable.day_button_selected);
                dayButtons[dayIndex].setTextColor(0xFFFFFFFF);
            });
        }

        builder.setView(dialogView);

        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            String medicationName = editMedicationName.getText().toString().trim();
            String remarks = editRemarks.getText().toString().trim();
            String prescribedPiecesStr = editPrescribedPieces.getText().toString().trim();
            String firstDoseTime = editFirstDoseTime.getText().toString().trim();
            int timesPerDay = spinnerTimesPerDay.getSelectedItemPosition() + 1;

            // Validation
            if (medicationName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter medication name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (prescribedPiecesStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter prescribed pieces", Toast.LENGTH_SHORT).show();
                return;
            }

            int prescribedPieces;
            try {
                prescribedPieces = Integer.parseInt(prescribedPiecesStr);
                if (prescribedPieces <= 0) {
                    Toast.makeText(getContext(), "Prescribed pieces must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid prescribed pieces", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDayIndex[0] < 0) {
                Toast.makeText(getContext(), "Please select a start day", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstDoseTime.isEmpty()) {
                Toast.makeText(getContext(), "Please select first dose time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate alarm times using auto-scheduling algorithm
            String startDay = dayNames[selectedDayIndex[0]];
            List<MedicationAlert.AlarmTime> alarmTimes = generateAlarmTimes(
                    prescribedPieces, timesPerDay, startDay, firstDoseTime);

            if (alarmTimes.isEmpty()) {
                Toast.makeText(getContext(), "Failed to generate alarm schedule", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                // Update existing medication via API
                updateMedicationAlertAPI(medication, alarmTimes, medicationName, remarks,
                        prescribedPieces, timesPerDay, startDay, firstDoseTime, position);
            } else {
                // Add new medication via API
                saveMedicationAlertAPI(alarmTimes, medicationName, remarks,
                        prescribedPieces, timesPerDay, startDay, firstDoseTime);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Auto-scheduling algorithm to generate alarm times
     * @param prescribedPieces Total number of doses
     * @param timesPerDay How many doses per day
     * @param startDay Starting day of week
     * @param firstDoseTime First dose time (e.g., "8:00 PM")
     * @return List of alarm times
     */
    private List<MedicationAlert.AlarmTime> generateAlarmTimes(int prescribedPieces, int timesPerDay,
                                                                String startDay, String firstDoseTime) {
        List<MedicationAlert.AlarmTime> alarmTimes = new ArrayList<>();

        try {
            // Parse first dose time
            String[] parts = firstDoseTime.split(" ");
            if (parts.length != 2) return alarmTimes;

            String time = parts[0];
            String period = parts[1];

            String[] timeParts = time.split(":");
            if (timeParts.length != 2) return alarmTimes;

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Convert to 24-hour format
            if (period.equals("PM") && hour != 12) {
                hour += 12;
            } else if (period.equals("AM") && hour == 12) {
                hour = 0;
            }

            // Calculate interval in hours
            int intervalHours = 24 / timesPerDay;

            // Generate alarm times
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            for (int i = 0; i < prescribedPieces; i++) {
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Format the time
                String currentPeriod = currentHour >= 12 ? "PM" : "AM";
                int displayHour = currentHour % 12;
                if (displayHour == 0) displayHour = 12;

                String formattedTime = String.format(Locale.getDefault(), "%d:%02d",
                        displayHour, currentMinute);
                alarmTimes.add(new MedicationAlert.AlarmTime(formattedTime, currentPeriod));

                // Add interval for next dose
                calendar.add(Calendar.HOUR_OF_DAY, intervalHours);
            }

        } catch (Exception e) {
            Log.e("RxAlertFragment", "Error generating alarm times", e);
        }

        return alarmTimes;
    }

    private void showTimePickerForNewAlarmTime(List<MedicationAlert.AlarmTime> alarmTimes, RecyclerView recyclerView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String period = selectedHour >= 12 ? "PM" : "AM";
                    int displayHour = selectedHour % 12;
                    if (displayHour == 0) displayHour = 12;

                    String time = String.format(Locale.getDefault(), "%d:%02d", displayHour, selectedMinute);
                    alarmTimes.add(new MedicationAlert.AlarmTime(time, period));
                    recyclerView.getAdapter().notifyDataSetChanged();
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void showTimePickerForAlarmTime(List<MedicationAlert.AlarmTime> alarmTimes, int position, RecyclerView recyclerView) {
        MedicationAlert.AlarmTime currentTime = alarmTimes.get(position);

        // Parse current time
        int hour = 12;
        int minute = 0;
        try {
            String[] parts = currentTime.getTime().split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);

            if (currentTime.getPeriod().equals("PM") && hour != 12) {
                hour += 12;
            } else if (currentTime.getPeriod().equals("AM") && hour == 12) {
                hour = 0;
            }
        } catch (Exception e) {
            Log.e("RxAlertFragment", "Error parsing time", e);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String period = selectedHour >= 12 ? "PM" : "AM";
                    int displayHour = selectedHour % 12;
                    if (displayHour == 0) displayHour = 12;

                    String time = String.format(Locale.getDefault(), "%d:%02d", displayHour, selectedMinute);
                    alarmTimes.set(position, new MedicationAlert.AlarmTime(time, period));
                    recyclerView.getAdapter().notifyDataSetChanged();
                }, hour, minute, false);

        timePickerDialog.show();
    }

    // Callback interface for time picker
    private interface TimePickerCallback {
        void onTimeSet(String time);
    }

    private void showTimePicker(EditText editTime, TimePickerCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String period = selectedHour >= 12 ? "PM" : "AM";
                    int displayHour = selectedHour % 12;
                    if (displayHour == 0) displayHour = 12;

                    String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s",
                            displayHour, selectedMinute, period);
                    editTime.setText(formattedTime);

                    if (callback != null) {
                        callback.onTimeSet(formattedTime);
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }

    /**
     * Update the calculated dose times display
     */
    private void updateCalculatedTimes(String firstDoseTime, String timesPerDayStr,
                                       LinearLayout container, TextView textView) {
        if (firstDoseTime == null || firstDoseTime.isEmpty() || timesPerDayStr == null || timesPerDayStr.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }

        try {
            int timesPerDay = Integer.parseInt(timesPerDayStr);
            if (timesPerDay <= 1) {
                container.setVisibility(View.GONE);
                return;
            }

            List<String> doseTimes = calculateDoseTimes(firstDoseTime, timesPerDay);
            if (doseTimes.isEmpty()) {
                container.setVisibility(View.GONE);
                return;
            }

            // Display the calculated times
            StringBuilder timesText = new StringBuilder();
            for (int i = 0; i < doseTimes.size(); i++) {
                timesText.append("Dose ").append(i + 1).append(": ").append(doseTimes.get(i));
                if (i < doseTimes.size() - 1) {
                    timesText.append("\n");
                }
            }
            textView.setText(timesText.toString());
            container.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            container.setVisibility(View.GONE);
        }
    }

    /**
     * Calculate all dose times based on first dose and times per day
     * Example: First dose at 8:00 AM, 3 times per day -> 8:00 AM, 4:00 PM, 12:00 AM
     */
    private List<String> calculateDoseTimes(String firstDoseTime, int timesPerDay) {
        List<String> doseTimes = new ArrayList<>();

        try {
            // Parse the first dose time
            String[] parts = firstDoseTime.split(" ");
            if (parts.length != 2) return doseTimes;

            String time = parts[0];
            String period = parts[1];

            String[] timeParts = time.split(":");
            if (timeParts.length != 2) return doseTimes;

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Convert to 24-hour format
            if (period.equals("PM") && hour != 12) {
                hour += 12;
            } else if (period.equals("AM") && hour == 12) {
                hour = 0;
            }

            // Calculate interval in minutes
            int intervalMinutes = (24 * 60) / timesPerDay;

            // Generate all dose times
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            for (int i = 0; i < timesPerDay; i++) {
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Format the time
                String currentPeriod = currentHour >= 12 ? "PM" : "AM";
                int displayHour = currentHour % 12;
                if (displayHour == 0) displayHour = 12;

                String formattedTime = String.format(Locale.getDefault(), "%d:%02d %s",
                        displayHour, currentMinute, currentPeriod);
                doseTimes.add(formattedTime);

                // Add interval for next dose
                calendar.add(Calendar.MINUTE, intervalMinutes);
            }

        } catch (Exception e) {
            Log.e("RxAlertFragment", "Error calculating dose times", e);
        }

        return doseTimes;
    }

    private void saveMedicationAlertAPI(List<MedicationAlert.AlarmTime> alarmTimes, String medicationName,
                                        String remarks, int prescribedPieces, int timesPerDay,
                                        String startDay, String firstDoseTime) {
        String patientId = RetrofitClient.currentUser.getId();

        // Get current date as start date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(new java.util.Date());

        // Parse first dose time to get time and period
        String[] timeParts = firstDoseTime.split(" ");
        String time = timeParts.length > 0 ? timeParts[0] : "";
        String period = timeParts.length > 1 ? timeParts[1] : "AM";

        // Create medication alert with auto-generated alarm times
        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                alarmTimes,
                medicationName,
                remarks,
                true,
                "", // No selected days - not needed for prescribed pieces approach
                startDate,
                prescribedPieces // Store prescribed pieces in duration_days
        );

        // Set prescribed pieces feature fields
        request.setPrescribed_pieces(prescribedPieces);
        request.setTimes_per_day(timesPerDay);
        request.setStart_day(startDay);
        request.setFirst_dose_time(time);
        request.setFirst_dose_period(period);

        Call<MedicationAlertResponse> call = apiService.createMedicationAlert(request);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        // Get the newly created alert and schedule alarms
                        if (alertResponse.getAlert() != null) {
                            MedicationAlert newAlert = alertResponse.getAlert().toMedicationAlert();
                            AlarmScheduler.scheduleAlarm(requireContext(), newAlert);
                        }

                        Toast.makeText(getContext(), "Medication alert added successfully with " +
                            alarmTimes.size() + " alarm time(s)", Toast.LENGTH_SHORT).show();
                        loadMedicationAlertsFromAPI();
                    }
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Log.e("API_ERROR", "Failed to create medication alert: " + t.getMessage());
                Toast.makeText(getContext(), "Failed to create alert: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMedicationAlertAPI(MedicationAlert medication, List<MedicationAlert.AlarmTime> alarmTimes,
                                          String medicationName, String remarks, int prescribedPieces,
                                          int timesPerDay, String startDay, String firstDoseTime, int position) {
        String patientId = RetrofitClient.currentUser.getId();

        // Parse first dose time to get time and period
        String[] timeParts = firstDoseTime.split(" ");
        String time = timeParts.length > 0 ? timeParts[0] : "";
        String period = timeParts.length > 1 ? timeParts[1] : "AM";

        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                alarmTimes,
                medicationName,
                remarks,
                medication.isEnabled(),
                "", // No selected days
                medication.getStartDate() != null ? medication.getStartDate() : "",
                prescribedPieces // Store prescribed pieces in duration_days
        );

        // Set prescribed pieces feature fields
        request.setPrescribed_pieces(prescribedPieces);
        request.setTimes_per_day(timesPerDay);
        request.setStart_day(startDay);
        request.setFirst_dose_time(time);
        request.setFirst_dose_period(period);

        Call<MedicationAlertResponse> call = apiService.updateMedicationAlert(medication.getId(), request);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        // Update local object
                        medication.setAlarmTimes(alarmTimes);
                        medication.setMedicationName(medicationName);
                        medication.setRemarks(remarks);
                        medication.setPrescribedPieces(prescribedPieces);
                        medication.setTimesPerDay(timesPerDay);
                        medication.setStartDay(startDay);
                        medication.setFirstDoseTime(time);
                        medication.setFirstDosePeriod(period);

                        // Rebuild display items to reflect changes (number of doses may have changed)
                        List<MedicationAlarmDisplayItem> displayItems = expandMedicationsToDisplayItems(medicationList);
                        adapter.updateList(displayItems);

                        // Reschedule alarms with updated times
                        AlarmScheduler.rescheduleAlarm(requireContext(), medication);

                        Toast.makeText(getContext(), "Medication alert updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), alertResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to update medication alert", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMedicationAlertAPI(MedicationAlert medication, int position) {
        Call<MedicationAlertResponse> call = apiService.deleteMedicationAlert(medication.getId());
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        // Cancel the alarm
                        AlarmScheduler.cancelAlarm(requireContext(), medication.getId());

                        // Find and remove the medication from the list by ID (not position)
                        MedicationAlert toRemove = null;
                        for (MedicationAlert med : medicationList) {
                            if (med.getId() == medication.getId()) {
                                toRemove = med;
                                break;
                            }
                        }

                        if (toRemove != null) {
                            medicationList.remove(toRemove);

                            // Rebuild display items and update adapter
                            List<MedicationAlarmDisplayItem> displayItems = expandMedicationsToDisplayItems(medicationList);
                            adapter.updateList(displayItems);
                        }

                        Toast.makeText(getContext(), "Medication alert deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), alertResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to delete medication alert", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(MedicationAlert medication, int position) {
        showAddEditDialog(medication, position);
    }

    @Override
    public void onDeleteClick(MedicationAlert medication, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Medication");
        builder.setMessage("Are you sure you want to delete " + medication.getMedicationName() + "?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete via API
            deleteMedicationAlertAPI(medication, position);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onToggleSwitch(MedicationAlert medication, boolean isEnabled) {
        // Update via API
        String patientId = RetrofitClient.currentUser.getId();

        // Use the alarm times constructor to support both legacy and prescribed pieces
        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                medication.getAlarmTimes(),
                medication.getMedicationName(),
                medication.getRemarks(),
                isEnabled,
                medication.getSelectedDays() != null ? medication.getSelectedDays() : "",
                medication.getStartDate() != null ? medication.getStartDate() : "",
                medication.getDurationDays()
        );

        // Set prescribed pieces fields if present
        if (medication.getPrescribedPieces() > 0) {
            request.setPrescribed_pieces(medication.getPrescribedPieces());
            request.setTimes_per_day(medication.getTimesPerDay());
            request.setStart_day(medication.getStartDay());
            request.setFirst_dose_time(medication.getFirstDoseTime());
            request.setFirst_dose_period(medication.getFirstDosePeriod());
        }

        Call<MedicationAlertResponse> call = apiService.updateMedicationAlert(medication.getId(), request);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        // Schedule or cancel alarm based on enabled status
                        if (isEnabled) {
                            AlarmScheduler.scheduleAlarm(requireContext(), medication);
                        } else {
                            AlarmScheduler.cancelAlarm(requireContext(), medication.getId());
                        }

                        String status = isEnabled ? "enabled" : "disabled";
                        Toast.makeText(getContext(), medication.getMedicationName() + " " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), alertResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        // Revert switch if failed
                        medication.setEnabled(!isEnabled);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to update alert status", Toast.LENGTH_SHORT).show();
                    // Revert switch if failed
                    medication.setEnabled(!isEnabled);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Revert switch if failed
                medication.setEnabled(!isEnabled);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onMarkAsTaken(MedicationAlert medication, int position) {
        // Refresh the adapter to update progress display
        adapter.notifyItemChanged(position);
        Toast.makeText(getContext(), medication.getMedicationName() + " marked as taken", Toast.LENGTH_SHORT).show();
    }
}