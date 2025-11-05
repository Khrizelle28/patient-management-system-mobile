package com.example.patientmanagementsystemmobile.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.response.MedicationAlertResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Boot completed, rescheduling alarms...");

            // Get patient ID from SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String patientId = prefs.getString("patient_id", null);

            if (patientId != null) {
                rescheduleAlarms(context, patientId);
            } else {
                Log.w(TAG, "No patient ID found, cannot reschedule alarms");
            }
        }
    }

    private void rescheduleAlarms(Context context, String patientId) {
        // Fetch all medication alerts from API and reschedule them
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<MedicationAlertResponse> call = apiService.getPatientMedicationAlerts(patientId);
        call.enqueue(new Callback<MedicationAlertResponse>() {
            @Override
            public void onResponse(Call<MedicationAlertResponse> call, Response<MedicationAlertResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MedicationAlertResponse alertResponse = response.body();

                    if (alertResponse.isSuccess() && alertResponse.getData() != null) {
                        List<MedicationAlertResponse.MedicationAlertData> alerts = alertResponse.getData();

                        for (MedicationAlertResponse.MedicationAlertData alertData : alerts) {
                            MedicationAlert medication = alertData.toMedicationAlert();
                            if (medication.isEnabled()) {
                                AlarmScheduler.scheduleAlarm(context, medication);
                            }
                        }

                        Log.d(TAG, "Rescheduled " + alerts.size() + " alarms");
                    }
                }
            }

            @Override
            public void onFailure(Call<MedicationAlertResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch alarms after boot: " + t.getMessage());
            }
        });
    }
}
