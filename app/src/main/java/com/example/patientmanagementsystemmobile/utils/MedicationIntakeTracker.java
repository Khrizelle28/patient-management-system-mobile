package com.example.patientmanagementsystemmobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicationIntakeTracker {
    private static final String PREF_NAME = "medication_intake";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private SharedPreferences prefs;
    private Context context;

    public MedicationIntakeTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get today's date string
     */
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Mark a medication as taken for today
     */
    public void markAsTaken(int medicationId) {
        String today = getTodayDate();
        String key = "taken_" + today + "_" + medicationId;
        prefs.edit().putBoolean(key, true).apply();
    }

    /**
     * Check if medication was taken today
     */
    public boolean wasTakenToday(int medicationId) {
        String today = getTodayDate();
        String key = "taken_" + today + "_" + medicationId;
        return prefs.getBoolean(key, false);
    }

    /**
     * Get weekly intake count - how many selected days have been completed this week
     */
    public int getWeeklyIntakeCount(int medicationId, String selectedDays) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            return 0;
        }

        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // Get start of this week (Monday)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        // Parse selected days
        String[] days = selectedDays.split(",");

        // Check each selected day in this week
        for (String dayStr : days) {
            try {
                int selectedDay = Integer.parseInt(dayStr.trim());
                // selectedDay: 1=Monday, 2=Tuesday, ..., 7=Sunday

                Calendar checkCal = (Calendar) cal.clone();
                checkCal.add(Calendar.DAY_OF_YEAR, selectedDay - 1);

                // Only count days that have already passed (not future days in the week)
                if (!checkCal.after(Calendar.getInstance())) {
                    String dateStr = sdf.format(checkCal.getTime());
                    String key = "taken_" + dateStr + "_" + medicationId;
                    if (prefs.getBoolean(key, false)) {
                        count++;
                    }
                }
            } catch (NumberFormatException e) {
                // Skip invalid day numbers
            }
        }

        return count;
    }

    /**
     * Check if medication duration has expired
     */
    public boolean hasExpired(String startDate, int durationDays) {
        if (startDate == null) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        try {
            Date start = sdf.parse(startDate);
            if (start == null) return false;

            Date endDate = new Date(start.getTime() + (durationDays * 24L * 60 * 60 * 1000));
            Date today = new Date();

            return today.after(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reset intake status for a medication (for testing or manual reset)
     */
    public void resetIntake(int medicationId) {
        String today = getTodayDate();
        String key = "taken_" + today + "_" + medicationId;
        prefs.edit().remove(key).apply();
    }

    /**
     * Clean up old intake records (older than 30 days)
     */
    public void cleanupOldRecords() {
        SharedPreferences.Editor editor = prefs.edit();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        try {
            // Calculate cutoff date (30 days ago)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            String cutoffDate = sdf.format(calendar.getTime());

            // Remove records older than cutoff
            for (String key : prefs.getAll().keySet()) {
                if (key.startsWith("taken_")) {
                    // Extract date from key (format: taken_yyyy-MM-dd_id)
                    String[] parts = key.split("_");
                    if (parts.length >= 2) {
                        String recordDate = parts[1];
                        if (recordDate.compareTo(cutoffDate) < 0) {
                            editor.remove(key);
                        }
                    }
                }
            }

            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
