package com.example.patientmanagementsystemmobile.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔔 Alarm received at: " + new java.util.Date().toString());
        Log.d(TAG, "========================================");

        // Get medication details from intent
        int alertId = intent.getIntExtra("alert_id", -1);
        int alarmIndex = intent.getIntExtra("alarm_index", 0);
        String medicationName = intent.getStringExtra("medication_name");
        String remarks = intent.getStringExtra("remarks");
        String time = intent.getStringExtra("time");
        String period = intent.getStringExtra("period");
        boolean isEnabled = intent.getBooleanExtra("is_enabled", true);
        String selectedDays = intent.getStringExtra("selected_days");
        String startDate = intent.getStringExtra("start_date");

        Log.d(TAG, "Alert ID: " + alertId);
        Log.d(TAG, "Alarm Index: " + alarmIndex);
        Log.d(TAG, "Medication Name: " + medicationName);
        Log.d(TAG, "Remarks: " + remarks);
        Log.d(TAG, "Time: " + time + " " + period);
        Log.d(TAG, "Enabled: " + isEnabled);

        if (medicationName != null) {
            // Play alarm sound
            AlarmSoundPlayer.playAlarmSound(context);
            Log.d(TAG, "🔊 Alarm sound started for: " + medicationName);

            // Show notification with alarm index info
            NotificationHelper.showNotification(context, alertId * 100 + alarmIndex, medicationName, remarks);
            Log.d(TAG, "✓ Notification shown successfully for: " + medicationName);

            // Track intake (this will be used by progress bar)
            MedicationIntakeTracker.markAsTaken(context, alertId, alarmIndex);
            Log.d(TAG, "✓ Intake tracked for alarm index: " + alarmIndex);

            // Note: Alarms are one-time only in prescribed pieces mode - no rescheduling
            Log.d(TAG, "✅ Alarm completed - one-time dose alarm (no rescheduling)");
        } else {
            Log.e(TAG, "✗ ERROR: Medication name is null!");
        }

        Log.d(TAG, "========================================");
    }
}
