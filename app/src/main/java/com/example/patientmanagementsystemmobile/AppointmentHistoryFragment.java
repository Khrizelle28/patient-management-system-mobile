package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.AppointmentAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.data.AppointmentData;
import com.example.patientmanagementsystemmobile.models.Appointment;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.response.AppointmentListResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppointmentHistoryFragment extends Fragment {

    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private ProgressBar progressBar;
    private LinearLayout textEmptyState;
    private ImageView buttonBack;
    private ApiService apiService;
    private String currentPatientId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Log.d("AppointmentHistory", "onViewCreated started");

            // Initialize views first
            initViews(view);
            Log.d("AppointmentHistory", "Views initialized");

            // Check if user is logged in
            if (RetrofitClient.currentUser == null) {
                Log.e("AppointmentHistory", "User is null");
                Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
                return;
            }

            currentPatientId = RetrofitClient.currentUser.getId();
            Log.d("AppointmentHistory", "Patient ID: " + currentPatientId);

            // Initialize API service
            apiService = RetrofitClient.getClient().create(ApiService.class);
            Log.d("AppointmentHistory", "API service initialized");

            // Setup RecyclerView
            setupRecyclerView();
            Log.d("AppointmentHistory", "RecyclerView setup complete");

            // Load appointments
            loadAppointmentsFromApi();
            Log.d("AppointmentHistory", "Loading appointments...");

        } catch (Exception e) {
            Log.e("AppointmentHistory", "Error in onViewCreated: " + e.getMessage(), e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            showEmptyState(true);
        }
    }

    private void initViews(View view) {
        recyclerViewAppointments = view.findViewById(R.id.recyclerViewAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        textEmptyState = view.findViewById(R.id.textEmptyState);
        buttonBack = view.findViewById(R.id.buttonBack);

        // Set back button click listener
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void setupRecyclerView() {
        appointmentAdapter = new AppointmentAdapter(new ArrayList<>(), getContext());
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAppointments.setAdapter(appointmentAdapter);
    }

    private void loadAppointmentsFromApi() {
        // Check if patient ID is available
        if (currentPatientId == null || currentPatientId.isEmpty()) {
            Log.e("AppointmentHistory", "Patient ID is null or empty");
            showError("Unable to load appointments - no patient ID");
            showLoading(false);
            showEmptyState(true);
            return;
        }

        Log.d("AppointmentHistory", "Making API call for patient: " + currentPatientId);
        showLoading(true);

        try {
            Call<AppointmentListResponse> call = apiService.getPatientAppointments(currentPatientId);
            call.enqueue(new Callback<AppointmentListResponse>() {
                @Override
                public void onResponse(Call<AppointmentListResponse> call, Response<AppointmentListResponse> response) {
                    Log.d("AppointmentHistory", "API Response received. Code: " + response.code());
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        AppointmentListResponse appointmentResponse = response.body();
                        Log.d("AppointmentHistory", "Response successful: " + appointmentResponse.isSuccess());

                        if (appointmentResponse.isSuccess()) {
                            List<Appointment> appointments = convertToAppointmentList(appointmentResponse.getData());
                            Log.d("AppointmentHistory", "Converted " + appointments.size() + " appointments");
                            appointmentAdapter.updateAppointments(appointments);

                            if (appointments.isEmpty()) {
                                showEmptyState(true);
                            } else {
                                showEmptyState(false);
                            }
                        } else {
                            Log.e("AppointmentHistory", "API returned error: " + appointmentResponse.getMessage());
                            showError("Failed to load appointments: " + appointmentResponse.getMessage());
                            showEmptyState(true);
                        }
                    } else {
                        Log.e("AppointmentHistory", "Response not successful or body is null. Code: " + response.code());
                        showError("Failed to load appointments. Code: " + response.code());
                        showEmptyState(true);
                    }
                }

                @Override
                public void onFailure(Call<AppointmentListResponse> call, Throwable t) {
                    Log.e("AppointmentHistory", "API call failed: " + t.getMessage(), t);
                    showLoading(false);
                    showError("Network error: " + t.getMessage());
                    showEmptyState(true);
                }
            });
        } catch (Exception e) {
            Log.e("AppointmentHistory", "Exception in loadAppointmentsFromApi: " + e.getMessage(), e);
            showLoading(false);
            showError("Error: " + e.getMessage());
            showEmptyState(true);
        }
    }

    private List<Appointment> convertToAppointmentList(List<AppointmentData> appointmentDataList) {
        List<Appointment> appointments = new ArrayList<>();

        // Sort appointmentDataList by date in descending order (latest first)
        Collections.sort(appointmentDataList, new Comparator<AppointmentData>() {
            @Override
            public int compare(AppointmentData a1, AppointmentData a2) {
                // Compare dates in descending order (latest first)
                // Date format is "YYYY-MM-DD" so string comparison works
                String date1 = a1.getAppointment_date() != null ? a1.getAppointment_date() : "";
                String date2 = a2.getAppointment_date() != null ? a2.getAppointment_date() : "";
                return date2.compareTo(date1); // Descending order
            }
        });

        for (AppointmentData data : appointmentDataList) {
            String status = data.getStatus();

            // Skip cancelled appointments
            if ("cancelled".equalsIgnoreCase(status)) {
                Log.d("AppointmentHistory", "Skipping cancelled appointment: " + data.getAppointment_time());
                continue;
            }

            // Show scheduled, completed, and pending appointments
            Log.d("AppointmentHistory", "Appointment for: " + data.getAppointment_time());

            // Format the doctor name and specialty
            String doctorName = "Dr. " + (data.getDoctor() != null ? data.getDoctor().getName() : "Unknown");
            String specialty = data.getDoctor() != null ? data.getDoctor().getSpecialty() : "General";

            // Format patient name from the current user
            String patientName = RetrofitClient.currentUser != null ?
                RetrofitClient.currentUser.getFullName() : "Patient";

            // Format date and time
            String formattedDate = formatDate(data.getAppointment_date());
            String formattedTime = data.getAppointment_time();

            Appointment appointment = new Appointment(
                    data.getId(),
                    patientName,
                    doctorName,
                    specialty,
                    formattedDate,
                    formattedTime,
                    data.getNotes() != null ? data.getNotes() : "Consultation",
                    status != null ? status : "pending"
            );

            appointments.add(appointment);
        }

        return appointments;
    }

    private String formatDate(String dateString) {
        // Format from "2025-06-16" to "June 16, 2025"
        try {
            String[] parts = dateString.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                String[] months = {"", "January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};

                return months[month] + " " + day + ", " + year;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString; // Return original if formatting fails
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewAppointments != null) {
            recyclerViewAppointments.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showEmptyState(boolean show) {
        if (textEmptyState != null) {
            textEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewAppointments != null) {
            recyclerViewAppointments.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Method to refresh appointments
    public void refreshAppointments() {
        loadAppointmentsFromApi();
    }
}
