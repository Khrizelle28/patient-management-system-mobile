package com.example.patientmanagementsystemmobile.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;

import java.util.Calendar;

public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    /**
     * Schedule an alarm for a medication alert
     */
    public static void scheduleAlarm(Context context, MedicationAlert medication) {
        if (!medication.isEnabled()) {
            Log.d(TAG, "Medication is disabled, not scheduling alarm");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alert_id", medication.getId());
        intent.putExtra("medication_name", medication.getMedicationName());
        intent.putExtra("remarks", medication.getRemarks());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Parse time
        Calendar calendar = parseTimeToCalendar(medication.getTime(), medication.getPeriod());
        if (calendar == null) {
            Log.e(TAG, "Failed to parse time: " + medication.getFullTime());
            return;
        }

        // If the time has already passed today, schedule for tomorrow
        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule repeating alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        // Also schedule repeating for subsequent days
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        Log.d(TAG, "Alarm scheduled for " + medication.getMedicationName() + " at " + medication.getFullTime());
        Log.d(TAG, "Scheduled time: " + calendar.getTime().toString());
    }

    /**
     * Cancel an alarm for a medication alert
     */
    public static void cancelAlarm(Context context, int alertId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alertId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, "Alarm canceled for ID: " + alertId);
    }

    /**
     * Reschedule an alarm (cancel old and schedule new)
     */
    public static void rescheduleAlarm(Context context, MedicationAlert medication) {
        cancelAlarm(context, medication.getId());
        scheduleAlarm(context, medication);
    }

    /**
     * Parse time string and period to Calendar object
     * Example: "10:30" and "AM" -> Calendar set to 10:30 AM
     */
    private static Calendar parseTimeToCalendar(String time, String period) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return null;
            }

            int hour = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());

            // Convert to 24-hour format
            if (period.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (period.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if the app can schedule exact alarms (for Android 12+)
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // For older versions, always return true
    }

    /**
     * Request exact alarm permission (for Android 12+)
     */
    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context,
                    "Please enable exact alarm permission in settings for medication reminders to work properly",
                    Toast.LENGTH_LONG).show();

                // Note: You can also open the settings page with:
                // Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                // context.startActivity(intent);
            }
        }
    }
}
