package com.example.patientmanagementsystemmobile.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
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
        Log.d(TAG, "==========================================");
        Log.d(TAG, "scheduleAlarm() called");
        Log.d(TAG, "Medication: " + medication.getMedicationName());
        Log.d(TAG, "Time: " + medication.getFullTime());
        Log.d(TAG, "Enabled: " + medication.isEnabled());
        Log.d(TAG, "==========================================");

        if (!medication.isEnabled()) {
            Log.d(TAG, "❌ Medication is disabled, not scheduling alarm");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "❌ AlarmManager is null");
            return;
        }

        // Check if we can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean canSchedule = alarmManager.canScheduleExactAlarms();
            Log.d(TAG, "Can schedule exact alarms: " + canSchedule);
            if (!canSchedule) {
                Log.w(TAG, "⚠️ Cannot schedule exact alarms - permission not granted!");
                Toast.makeText(context,
                    "Exact alarm permission required for medication reminders. Please grant permission.",
                    Toast.LENGTH_LONG).show();
            }
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alert_id", medication.getId());
        intent.putExtra("medication_name", medication.getMedicationName());
        intent.putExtra("remarks", medication.getRemarks());
        intent.putExtra("time", medication.getTime());
        intent.putExtra("period", medication.getPeriod());
        intent.putExtra("is_enabled", medication.isEnabled());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Log.d(TAG, "✓ PendingIntent created with ID: " + medication.getId());

        // Parse time
        Calendar calendar = parseTimeToCalendar(medication.getTime(), medication.getPeriod());
        if (calendar == null) {
            Log.e(TAG, "Failed to parse time: " + medication.getFullTime());
            return;
        }

        Calendar now = Calendar.getInstance();

        Log.d(TAG, "Current time: " + now.getTime().toString());
        Log.d(TAG, "Scheduled time (parsed): " + calendar.getTime().toString());

        // If the time is within 2 minutes from now, schedule it immediately (add 10 seconds buffer)
        long timeDifference = calendar.getTimeInMillis() - now.getTimeInMillis();
        long minutesDiff = timeDifference / 1000 / 60;

        Log.d(TAG, "Time difference: " + minutesDiff + " minutes (" + timeDifference + " ms)");

        if (timeDifference < 0 && timeDifference > -120000) { // Within past 2 minutes
            Log.d(TAG, "⏰ Time just passed (within 2 min), scheduling for next occurrence in 1 day");
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } else if (timeDifference < 0) {
            // If the time has already passed by more than 2 minutes, schedule for tomorrow
            Log.d(TAG, "⏰ Time already passed, scheduling for tomorrow");
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        } else if (timeDifference < 10000) { // Less than 10 seconds away
            Log.d(TAG, "⏰ Time is very close, adding small buffer");
            // Add small buffer to ensure alarm has time to be set
            calendar.add(Calendar.SECOND, 10);
        } else {
            Log.d(TAG, "⏰ Time is in the future, scheduling for today");
        }

        Log.d(TAG, "Final scheduled time: " + calendar.getTime().toString());

        // IMPORTANT: setRepeating() is NOT reliable on Android 12+ for exact alarms
        // We must use setExact/setExactAndAllowWhileIdle and reschedule after each trigger
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12+, use setExactAndAllowWhileIdle (most reliable)
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "📱 Android 12+ - using setExactAndAllowWhileIdle (EXACT, RELIABLE)");
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "✅ Alarm SET using setExactAndAllowWhileIdle");
                } else {
                    Log.w(TAG, "⚠️ Cannot schedule exact alarms - using setAndAllowWhileIdle");
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "✅ Alarm SET using setAndAllowWhileIdle");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0+, use setExactAndAllowWhileIdle
                Log.d(TAG, "📱 Android 6.0+ - using setExactAndAllowWhileIdle");
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "✅ Alarm SET using setExactAndAllowWhileIdle");
            } else {
                // For older Android versions, use setExact
                Log.d(TAG, "📱 Android < 6.0 - using setExact");
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "✅ Alarm SET using setExact");
            }

            Log.d(TAG, "✅✅✅ SUCCESS! Alarm scheduled for " + medication.getMedicationName() + " at " + medication.getFullTime());
            Log.d(TAG, "Scheduled time: " + calendar.getTime().toString());
            Log.d(TAG, "⚠️ NOTE: Alarm will fire ONCE. AlarmReceiver must reschedule for next day.");
            Log.d(TAG, "==========================================");

            Toast.makeText(context,
                "Alarm set for " + medication.getMedicationName() + " at " + medication.getFullTime(),
                Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "❌❌❌ FAILED to schedule alarm: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context,
                "Failed to schedule alarm: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Schedule a test alarm that fires in 30 seconds (for debugging)
     */
    public static void scheduleTestAlarm(Context context) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "scheduleTestAlarm() called - will fire in 30 seconds");
        Log.d(TAG, "==========================================");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "❌ AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alert_id", 99999);
        intent.putExtra("medication_name", "TEST ALARM");
        intent.putExtra("remarks", "This is a test alarm");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                99999,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 30); // Fire in 30 seconds

        Log.d(TAG, "Current time: " + Calendar.getInstance().getTime().toString());
        Log.d(TAG, "Test alarm will fire at: " + calendar.getTime().toString());

        try {
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

            Log.d(TAG, "✅✅✅ Test alarm scheduled successfully!");
            Log.d(TAG, "==========================================");

            Toast.makeText(context,
                "Test alarm will fire in 30 seconds",
                Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to schedule test alarm: " + e.getMessage());
            e.printStackTrace();
        }
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
                try {
                    // Open the exact alarm settings page
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    Toast.makeText(context,
                        "Please enable exact alarm permission for medication reminders",
                        Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open exact alarm settings: " + e.getMessage());
                    Toast.makeText(context,
                        "Please enable exact alarm permission in system settings",
                        Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Check if battery optimization is ignored for this app
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                String packageName = context.getPackageName();
                boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
                Log.d(TAG, "Battery optimization status: " + (isIgnoring ? "IGNORED ✓" : "ACTIVE ⚠️"));
                return isIgnoring;
            }
        }
        return true; // For older versions, assume no battery optimization
    }

    /**
     * Request to ignore battery optimizations for better alarm reliability
     */
    public static void requestIgnoreBatteryOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    Toast.makeText(context,
                        "Please disable battery optimization for reliable medication reminders",
                        Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open battery optimization settings: " + e.getMessage());
                    Toast.makeText(context,
                        "Please disable battery optimization in app settings",
                        Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Battery optimization already disabled ✓", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Battery optimization not applicable for this Android version", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Schedule a real medication test alarm that fires in 2 minutes
     * This tests the full medication alarm flow unlike the basic test alarm
     */
    public static void scheduleRealTestAlarm(Context context) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "scheduleRealTestAlarm() called - will fire in 2 minutes");
        Log.d(TAG, "==========================================");

        // Create a test medication
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.MINUTE, 2); // 2 minutes from now

        int hour = testTime.get(Calendar.HOUR);
        if (hour == 0) hour = 12;
        int minute = testTime.get(Calendar.MINUTE);
        String period = testTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        String timeStr = String.format("%d:%02d", hour, minute);

        MedicationAlert testMedication = new MedicationAlert(
            88888, // Test ID
            timeStr,
            period,
            "TEST MEDICATION (2 min)",
            "This is a real medication test alarm scheduled for 2 minutes from now",
            true
        );

        Log.d(TAG, "Test medication time: " + testMedication.getFullTime());
        Log.d(TAG, "Will fire at: " + testTime.getTime().toString());

        // Use the regular scheduleAlarm method to test the full flow
        scheduleAlarm(context, testMedication);

        Toast.makeText(context,
            "Real medication test alarm scheduled for 2 minutes from now at " + testMedication.getFullTime(),
            Toast.LENGTH_LONG).show();
    }
}
