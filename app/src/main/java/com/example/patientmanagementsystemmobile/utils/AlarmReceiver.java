package com.example.patientmanagementsystemmobile.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received!");

        // Get medication details from intent
        int alertId = intent.getIntExtra("alert_id", -1);
        String medicationName = intent.getStringExtra("medication_name");
        String remarks = intent.getStringExtra("remarks");

        if (medicationName != null) {
            // Show notification
            NotificationHelper.showNotification(context, alertId, medicationName, remarks);

            Log.d(TAG, "Notification shown for: " + medicationName);
        } else {
            Log.e(TAG, "Medication name is null!");
        }
    }
}
