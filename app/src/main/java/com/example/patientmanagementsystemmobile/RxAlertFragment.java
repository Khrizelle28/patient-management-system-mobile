package com.example.patientmanagementsystemmobile;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.patientmanagementsystemmobile.adapter.MedicationAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;
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

        // Set up adapter
        adapter = new MedicationAdapter(medicationList);
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

        // Load medication alerts from API
        loadMedicationAlertsFromAPI();

        return view;
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

                        // Update adapter
                        if (adapter != null) {
                            adapter.updateList(medicationList);
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

    private void showAddEditDialog(MedicationAlert medication, int position) {
        boolean isEdit = medication != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(isEdit ? "Edit Medication Alert" : "Add Medication Alert");

        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medication_alert, null);
        EditText editTime = dialogView.findViewById(R.id.editTextTime);
        EditText editMedicationName = dialogView.findViewById(R.id.editTextMedicationName);
        EditText editRemarks = dialogView.findViewById(R.id.editTextRemarks);

        // If editing, populate fields
        if (isEdit) {
            editTime.setText(medication.getFullTime());
            editMedicationName.setText(medication.getMedicationName());
            editRemarks.setText(medication.getRemarks());
        }

        // Handle time picker
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(editTime);
            }
        });

        builder.setView(dialogView);

        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            String timeStr = editTime.getText().toString().trim();
            String medicationName = editMedicationName.getText().toString().trim();
            String remarks = editRemarks.getText().toString().trim();

            if (timeStr.isEmpty() || medicationName.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in time and medication name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse time and period
            String[] timeParts = timeStr.split(" ");
            if (timeParts.length != 2) {
                Toast.makeText(getContext(), "Invalid time format", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = timeParts[0];
            String period = timeParts[1];

            if (isEdit) {
                // Update existing medication via API
                updateMedicationAlertAPI(medication, time, period, medicationName, remarks, position);
            } else {
                // Add new medication via API
                saveMedicationAlertAPI(time, period, medicationName, remarks);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTimePicker(EditText editTime) {
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
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void saveMedicationAlertAPI(String time, String period, String medicationName, String remarks) {
        String patientId = RetrofitClient.currentUser.getId();

        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                time,
                period,
                medicationName,
                remarks,
                true
        );

        Call<MedicationAlertResponse> call = apiService.createMedicationAlert(request);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Medication alert added successfully", Toast.LENGTH_SHORT).show();

                        // Get the newly created alert and schedule alarm
                        if (alertResponse.getAlert() != null) {
                            MedicationAlert newAlert = alertResponse.getAlert().toMedicationAlert();
                            AlarmScheduler.scheduleAlarm(requireContext(), newAlert);
                        }

                        // Reload the list from API
                        loadMedicationAlertsFromAPI();
                    } else {
                        Toast.makeText(getContext(), alertResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to add medication alert", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMedicationAlertAPI(MedicationAlert medication, String time, String period, String medicationName, String remarks, int position) {
        String patientId = RetrofitClient.currentUser.getId();

        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                time,
                period,
                medicationName,
                remarks,
                medication.isEnabled()
        );

        Call<MedicationAlertResponse> call = apiService.updateMedicationAlert(medication.getId(), request);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess()) {
                        // Update local object
                        medication.setTime(time);
                        medication.setPeriod(period);
                        medication.setMedicationName(medicationName);
                        medication.setRemarks(remarks);
                        adapter.notifyItemChanged(position);

                        // Reschedule alarm with updated time
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

                        adapter.removeItem(position);
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

        MedicationAlertRequest request = new MedicationAlertRequest(
                patientId,
                medication.getTime(),
                medication.getPeriod(),
                medication.getMedicationName(),
                medication.getRemarks(),
                isEnabled
        );

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
}