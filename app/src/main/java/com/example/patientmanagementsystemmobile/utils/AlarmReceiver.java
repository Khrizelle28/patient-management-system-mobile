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
        String medicationName = intent.getStringExtra("medication_name");
        String remarks = intent.getStringExtra("remarks");
        String time = intent.getStringExtra("time");
        String period = intent.getStringExtra("period");
        boolean isEnabled = intent.getBooleanExtra("is_enabled", true);

        Log.d(TAG, "Alert ID: " + alertId);
        Log.d(TAG, "Medication Name: " + medicationName);
        Log.d(TAG, "Remarks: " + remarks);
        Log.d(TAG, "Time: " + time + " " + period);
        Log.d(TAG, "Enabled: " + isEnabled);

        if (medicationName != null) {
            // Play alarm sound (longer duration)
            AlarmSoundPlayer.playAlarmSound(context);
            Log.d(TAG, "🔊 Alarm sound started for: " + medicationName);

            // Show notification
            NotificationHelper.showNotification(context, alertId, medicationName, remarks);
            Log.d(TAG, "✓ Notification shown successfully for: " + medicationName);

            // Reschedule for next day (for repeating alarms, not test alarms)
            // Test alarm IDs: 99999 (30s test), 88888 (2min test)
            if (alertId != 99999 && alertId != 88888 && time != null && period != null && isEnabled) {
                Log.d(TAG, "🔄 Rescheduling alarm for next day...");

                MedicationAlert medication = new MedicationAlert(
                    alertId,
                    time,
                    period,
                    medicationName,
                    remarks,
                    isEnabled
                );

                AlarmScheduler.scheduleAlarm(context, medication);
                Log.d(TAG, "✅ Alarm rescheduled successfully for next day");
            } else if (alertId == 99999 || alertId == 88888) {
                Log.d(TAG, "⏭️ Test alarm detected - not rescheduling");
            } else {
                Log.w(TAG, "⚠️ Alarm not rescheduled - missing time/period or disabled");
            }
        } else {
            Log.e(TAG, "✗ ERROR: Medication name is null!");
        }

        Log.d(TAG, "========================================");
    }
}
