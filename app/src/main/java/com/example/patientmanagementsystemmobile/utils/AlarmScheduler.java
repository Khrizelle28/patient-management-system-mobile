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
     * Schedule alarms for a medication alert (one for each alarm time)
     */
    public static void scheduleAlarm(Context context, MedicationAlert medication) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "scheduleAlarm() called");
        Log.d(TAG, "Medication: " + medication.getMedicationName());
        Log.d(TAG, "Alarm times: " + medication.getAlarmTimes().size());
        Log.d(TAG, "Enabled: " + medication.isEnabled());
        Log.d(TAG, "==========================================");

        if (!medication.isEnabled()) {
            Log.d(TAG, "❌ Medication is disabled, not scheduling alarms");
            return;
        }

        // Schedule a separate alarm for each alarm time
        for (int i = 0; i < medication.getAlarmTimes().size(); i++) {
            scheduleAlarmForTime(context, medication, medication.getAlarmTimes().get(i), i);
        }
    }

    /**
     * Schedule a single alarm for a specific alarm time
     */
    public static void scheduleAlarmForTime(Context context, MedicationAlert medication,
                                            MedicationAlert.AlarmTime alarmTime, int alarmIndex) {
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

        // Create unique request code: medicationId * 100 + alarmIndex
        int requestCode = medication.getId() * 100 + alarmIndex;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alert_id", medication.getId());
        intent.putExtra("alarm_index", alarmIndex);
        intent.putExtra("medication_name", medication.getMedicationName());
        intent.putExtra("remarks", medication.getRemarks());
        intent.putExtra("time", alarmTime.getTime());
        intent.putExtra("period", alarmTime.getPeriod());
        intent.putExtra("is_enabled", medication.isEnabled());
        intent.putExtra("selected_days", medication.getSelectedDays());
        intent.putExtra("start_date", medication.getStartDate());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Log.d(TAG, "✓ PendingIntent created with request code: " + requestCode + " for " + alarmTime.getFullTime());

        // Parse time
        Calendar calendar = parseTimeToCalendar(alarmTime.getTime(), alarmTime.getPeriod());
        if (calendar == null) {
            Log.e(TAG, "Failed to parse time: " + alarmTime.getFullTime());
            return;
        }

        Calendar now = Calendar.getInstance();
        String selectedDays = medication.getSelectedDays();

        // Find next occurrence on a selected day
        calendar = findNextSelectedDayOccurrence(calendar, now, selectedDays);

        Log.d(TAG, "Scheduled time for " + alarmTime.getFullTime() + ": " + calendar.getTime().toString());

        // Schedule the alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

            Log.d(TAG, "✅ Alarm " + (alarmIndex + 1) + " scheduled for " + alarmTime.getFullTime());

        } catch (Exception e) {
            Log.e(TAG, "❌ FAILED to schedule alarm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find the next occurrence of the alarm time on a selected day
     */
    private static Calendar findNextSelectedDayOccurrence(Calendar alarmTime, Calendar now, String selectedDays) {
        // If alarm time is in the future today and today is selected, use it
        if (alarmTime.getTimeInMillis() > now.getTimeInMillis() && isTodaySelectedDay(selectedDays, now)) {
            return alarmTime;
        }

        // Otherwise, find the next selected day
        return findNextSelectedDayFirstDose(alarmTime, now, selectedDays);
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
     * Cancel all alarms for a medication alert (all alarm time indices)
     */
    public static void cancelAlarm(Context context, int alertId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // Cancel all possible alarm indices (0-99)
        for (int i = 0; i < 100; i++) {
            int requestCode = alertId * 100 + i;
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        Log.d(TAG, "All alarms canceled for medication ID: " + alertId);
    }

    /**
     * Reschedule an alarm (cancel old and schedule new)
     */
    public static void rescheduleAlarm(Context context, MedicationAlert medication) {
        cancelAlarm(context, medication.getId());
        scheduleAlarm(context, medication);
    }

    /**
     * Find next dose considering 24-hour cycles that start on selected days
     * @param firstDoseTime The time of the first dose (e.g., 1:00 PM)
     * @param now Current time
     * @param intervalMs Interval between doses in milliseconds
     * @param selectedDays Comma-separated day numbers (1=Mon, 7=Sun)
     * @param timesPerDay Number of doses per day
     * @return Calendar set to next dose time
     */
    private static Calendar findNextDoseInCycle(Calendar firstDoseTime, Calendar now,
                                                long intervalMs, String selectedDays,
                                                int timesPerDay) {

        // CASE 1: First dose hasn't happened yet today
        if (firstDoseTime.getTimeInMillis() > now.getTimeInMillis()) {
            // Check if today is a selected day
            if (isTodaySelectedDay(selectedDays, now)) {
                Log.d(TAG, "First dose is in future and today is selected, scheduling for today");
                return firstDoseTime; // Schedule first dose today
            } else {
                // Find next selected day and schedule first dose
                Log.d(TAG, "First dose is in future but today is NOT selected, finding next selected day");
                return findNextSelectedDayFirstDose(firstDoseTime, now, selectedDays);
            }
        }

        // CASE 2: First dose already happened today - check if in current cycle
        Calendar todayFirstDose = (Calendar) now.clone();
        todayFirstDose.set(Calendar.HOUR_OF_DAY, firstDoseTime.get(Calendar.HOUR_OF_DAY));
        todayFirstDose.set(Calendar.MINUTE, firstDoseTime.get(Calendar.MINUTE));
        todayFirstDose.set(Calendar.SECOND, 0);
        todayFirstDose.set(Calendar.MILLISECOND, 0);

        long timeSinceFirstDoseToday = now.getTimeInMillis() - todayFirstDose.getTimeInMillis();

        // Calculate how many doses have occurred in today's cycle
        long dosesElapsed = timeSinceFirstDoseToday / intervalMs;

        // Calculate next dose time
        Calendar nextDose = (Calendar) todayFirstDose.clone();
        nextDose.setTimeInMillis(todayFirstDose.getTimeInMillis() + ((dosesElapsed + 1) * intervalMs));

        // Check if next dose would exceed 24-hour cycle
        long timeSinceFirstDoseForNext = nextDose.getTimeInMillis() - todayFirstDose.getTimeInMillis();
        long hoursSinceFirst = timeSinceFirstDoseForNext / (1000 * 60 * 60);

        if (hoursSinceFirst >= 24 || dosesElapsed + 1 >= timesPerDay) {
            // Cycle complete - find next selected day
            Log.d(TAG, "24-hour cycle complete (hours: " + hoursSinceFirst + ", doses: " + (dosesElapsed + 1) + "/" + timesPerDay + "), finding next selected day");
            return findNextSelectedDayFirstDose(firstDoseTime, now, selectedDays);
        } else {
            // Still within cycle - schedule next dose
            Log.d(TAG, "Within cycle, scheduling dose " + (dosesElapsed + 2) + "/" + timesPerDay + " at " + nextDose.getTime());
            return nextDose;
        }
    }

    /**
     * Find the next selected day and return first dose time on that day
     */
    private static Calendar findNextSelectedDayFirstDose(Calendar firstDoseTime,
                                                         Calendar now,
                                                         String selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            // No selected days - schedule for tomorrow
            Calendar tomorrow = (Calendar) firstDoseTime.clone();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            Log.d(TAG, "No selected days, scheduling for tomorrow at " + tomorrow.getTime());
            return tomorrow;
        }

        // Parse selected days
        String[] days = selectedDays.split(",");
        int[] selectedDayNumbers = new int[days.length];
        for (int i = 0; i < days.length; i++) {
            try {
                selectedDayNumbers[i] = Integer.parseInt(days[i].trim());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid day number: " + days[i]);
            }
        }

        // Start checking from tomorrow
        Calendar checkDate = (Calendar) now.clone();
        checkDate.add(Calendar.DAY_OF_YEAR, 1);
        checkDate.set(Calendar.HOUR_OF_DAY, firstDoseTime.get(Calendar.HOUR_OF_DAY));
        checkDate.set(Calendar.MINUTE, firstDoseTime.get(Calendar.MINUTE));
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);

        // Check up to 7 days ahead
        for (int i = 0; i < 7; i++) {
            int dayOfWeek = checkDate.get(Calendar.DAY_OF_WEEK);
            int dayNum = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;

            for (int selectedDay : selectedDayNumbers) {
                if (selectedDay == dayNum) {
                    Log.d(TAG, "Next selected day: " + getDayName(dayNum) + " at " + checkDate.getTime());
                    return checkDate;
                }
            }

            checkDate.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Fallback: schedule for tomorrow
        Calendar tomorrow = (Calendar) firstDoseTime.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Log.w(TAG, "Could not find selected day in next 7 days, using tomorrow as fallback");
        return tomorrow;
    }

    /**
     * Check if today is one of the selected days
     */
    private static boolean isTodaySelectedDay(String selectedDays, Calendar now) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            return true; // No selected days = every day
        }

        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int todayNum = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;

        String[] days = selectedDays.split(",");
        for (String day : days) {
            try {
                if (Integer.parseInt(day.trim()) == todayNum) {
                    Log.d(TAG, "Today (" + getDayName(todayNum) + ") is a selected day");
                    return true;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid day number: " + day);
            }
        }
        Log.d(TAG, "Today (" + getDayName(todayNum) + ") is NOT a selected day");
        return false;
    }

    /**
     * Get day name from day number (1=Monday, 7=Sunday)
     */
    private static String getDayName(int dayNum) {
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        if (dayNum >= 1 && dayNum <= 7) {
            return dayNames[dayNum - 1];
        }
        return "Unknown";
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
