package com.example.patientmanagementsystemmobile;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.AppointmentAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.data.AppointmentData;
import com.example.patientmanagementsystemmobile.enums.AppointmentStatus;
import com.example.patientmanagementsystemmobile.models.Appointment;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.response.AppointmentListResponse;
import com.example.patientmanagementsystemmobile.response.AppointmentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private TextView textWelcome;
    private TextView textUpcomingAppointments;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String currentPatientId = RetrofitClient.currentUser.getId(); // Replace with actual patient ID from your app's session/preferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        setupApiService();
        loadAppointmentsFromApi();

        return view;
    }

    private void initViews(View view) {
        recyclerViewAppointments = view.findViewById(R.id.recyclerViewAppointments);
        textWelcome = view.findViewById(R.id.textWelcome);
        textUpcomingAppointments = view.findViewById(R.id.textUpcomingAppointments);
        progressBar = view.findViewById(R.id.progressBar);

        // Set welcome message
        textWelcome.setText("");
        textUpcomingAppointments.setText("Your Appointments");
    }

    private void setupRecyclerView() {
        appointmentAdapter = new AppointmentAdapter(new ArrayList<>(), getContext());
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAppointments.setAdapter(appointmentAdapter);
    }

    private void setupApiService() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    private void loadAppointmentsFromApi() {
        showLoading(true);

        Call<AppointmentListResponse> call = apiService.getPatientAppointments(currentPatientId);
        call.enqueue(new Callback<AppointmentListResponse>() {
            @Override
            public void onResponse(Call<AppointmentListResponse> call, Response<AppointmentListResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AppointmentListResponse appointmentResponse = response.body();

                    if (appointmentResponse.isSuccess()) {
                        List<Appointment> appointments = convertToAppointmentList(appointmentResponse.getData());
                        appointmentAdapter.updateAppointments(appointments);

                        if (appointments.isEmpty()) {
                            showEmptyState();
                        }
                    } else {
                        showError("Failed to load appointments: " + appointmentResponse.getMessage());
                    }
                } else {
                    showError("Failed to load appointments. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<AppointmentListResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<Appointment> convertToAppointmentList(List<AppointmentData> appointmentDataList) {
        List<Appointment> appointments = new ArrayList<>();

        for (AppointmentData data : appointmentDataList) {
            String status = data.getStatus();

            // Format the doctor name and specialty
            Log.d("Doctor", "doctor list for: " + data.getAppointment_time());
            String doctorName = "Dr. " + (data.getDoctor() != null ? data.getDoctor().getName() : "Unknown");
            String specialty = data.getDoctor() != null ? data.getDoctor().getSpecialty() : "General";

            // Format date and time
            String formattedDate = formatDate(data.getAppointment_date());
            String formattedTime = data.getAppointment_time();

            Appointment appointment = new Appointment(
                    data.getId(),
                    doctorName,
                    specialty,
                    formattedDate,
                    formattedTime,
                    data.getNotes() != null ? data.getNotes() : "Consultation",
                    status
            );

            appointments.add(appointment);
        }

        return appointments;
    }

    private AppointmentStatus getAppointmentStatus(String status) {
        if (status == null) return AppointmentStatus.SCHEDULED;

        switch (status.toLowerCase()) {
            case "scheduled":
                return AppointmentStatus.SCHEDULED;
            case "completed":
                return AppointmentStatus.COMPLETED;
            case "cancelled":
                return AppointmentStatus.CANCELLED;
            default:
                return AppointmentStatus.UPCOMING;
        }
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

    private String formatTime(String timeString) {
        // Format from "8:00 AM - 11:00 AM" to just "8:00 AM" or handle as needed
        if (timeString.contains(" - ")) {
            return timeString.split(" - ")[0]; // Return start time only
        }
        return timeString;
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerViewAppointments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showEmptyState() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "No appointments found", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to refresh appointments
    public void refreshAppointments() {
        loadAppointmentsFromApi();
    }
}