package com.example.patientmanagementsystemmobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.patientmanagementsystemmobile.models.MedicationAlert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
     * Mark a medication as taken for today with timestamp
     */
    public void markAsTaken(int medicationId) {
        String today = getTodayDate();
        String key = "taken_" + today + "_" + medicationId;
        long timestamp = System.currentTimeMillis();
        prefs.edit()
            .putBoolean(key, true)
            .putLong(key + "_timestamp", timestamp)
            .apply();
    }

    /**
     * Static method to mark a specific alarm as taken (for specific alarm index)
     */
    public static void markAsTaken(Context context, int medicationId, int alarmIndex) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String today = sdf.format(new Date());
        String key = "taken_" + today + "_" + medicationId + "_" + alarmIndex;
        long timestamp = System.currentTimeMillis();
        prefs.edit()
            .putBoolean(key, true)
            .putLong(key + "_timestamp", timestamp)
            .apply();
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
     * Check if a specific alarm was taken today (for prescribed pieces)
     */
    public boolean wasTakenToday(int medicationId, int alarmIndex) {
        String today = getTodayDate();
        String key = "taken_" + today + "_" + medicationId + "_" + alarmIndex;
        return prefs.getBoolean(key, false);
    }

    /**
     * Get the timestamp when medication was taken today
     * @return timestamp in milliseconds, or -1 if not taken
     */
    public long getTakenTimestamp(int medicationId, String date) {
        String key = "taken_" + date + "_" + medicationId;
        return prefs.getLong(key + "_timestamp", -1);
    }

    /**
     * Check if medication was taken on time (within 5 minutes of scheduled time)
     */
    public boolean wasTakenOnTime(int medicationId, String date, String scheduledTime, String period) {
        long takenTimestamp = getTakenTimestamp(medicationId, date);
        if (takenTimestamp == -1) {
            return false; // Not taken at all
        }

        // Parse scheduled time
        long scheduledTimestamp = parseScheduledTime(date, scheduledTime, period);
        if (scheduledTimestamp == -1) {
            return false;
        }

        // Check if taken within 5 minutes (300,000 milliseconds)
        long timeDifference = Math.abs(takenTimestamp - scheduledTimestamp);
        return timeDifference <= 300000; // 5 minutes = 300,000 ms
    }

    /**
     * Parse scheduled time to timestamp
     */
    private long parseScheduledTime(String date, String time, String period) {
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Convert to 24-hour format
            if (period.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (period.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dateTimeStr = date + " " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Date dateTime = sdf.parse(dateTimeStr);
            return dateTime != null ? dateTime.getTime() : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get weekly dose statuses for each dose (selected days × alarm times)
     * Returns array of status codes: 0=not taken, 1=taken on time (green), 2=taken late/missed (red)
     */
    public int[] getWeeklyDoseStatuses(int medicationId, String selectedDays, List<MedicationAlert.AlarmTime> alarmTimes) {
        if (selectedDays == null || selectedDays.isEmpty() || alarmTimes == null || alarmTimes.isEmpty()) {
            return new int[0];
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        // Get start of this week (Monday)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        // Parse selected days
        String[] days = selectedDays.split(",");
        int[] statuses = new int[days.length * alarmTimes.size()];

        // Check each selected day and each alarm time
        for (int dayIdx = 0; dayIdx < days.length; dayIdx++) {
            try {
                int selectedDay = Integer.parseInt(days[dayIdx].trim());
                Calendar checkCal = (Calendar) cal.clone();
                checkCal.add(Calendar.DAY_OF_YEAR, selectedDay - 1);
                String dateStr = sdf.format(checkCal.getTime());

                for (int alarmIdx = 0; alarmIdx < alarmTimes.size(); alarmIdx++) {
                    int statusIdx = dayIdx * alarmTimes.size() + alarmIdx;
                    MedicationAlert.AlarmTime alarmTime = alarmTimes.get(alarmIdx);

                    // Only check days that have already passed (not future days)
                    if (!checkCal.after(now)) {
                        String key = "taken_" + dateStr + "_" + medicationId + "_" + alarmIdx;
                        boolean wasTaken = prefs.getBoolean(key, false);

                        if (wasTaken) {
                            // Check if taken on time
                            boolean onTime = wasTakenOnTime(medicationId, alarmIdx, dateStr,
                                    alarmTime.getTime(), alarmTime.getPeriod());
                            statuses[statusIdx] = onTime ? 1 : 2; // 1=green (on time), 2=red (late)
                        } else {
                            // Not taken - check if it's late (more than 5 minutes past scheduled time)
                            long scheduledTimestamp = parseScheduledTime(dateStr, alarmTime.getTime(), alarmTime.getPeriod());
                            if (scheduledTimestamp != -1) {
                                long currentTime = now.getTimeInMillis();
                                long timeDifference = currentTime - scheduledTimestamp;

                                // If more than 5 minutes late, mark as red (missed/late)
                                if (timeDifference > 300000) { // 300,000 ms = 5 minutes
                                    statuses[statusIdx] = 2; // Red - late/missed
                                } else {
                                    statuses[statusIdx] = 0; // Gray - not taken yet (still within window)
                                }
                            } else {
                                statuses[statusIdx] = 0; // Gray - not taken yet
                            }
                        }
                    } else {
                        statuses[statusIdx] = 0; // Future day - not taken yet
                    }
                }
            } catch (NumberFormatException e) {
                // Set all alarm times for this day to 0
                for (int alarmIdx = 0; alarmIdx < alarmTimes.size(); alarmIdx++) {
                    int statusIdx = dayIdx * alarmTimes.size() + alarmIdx;
                    statuses[statusIdx] = 0;
                }
            }
        }

        return statuses;
    }

    /**
     * Check if medication was taken on time (for specific alarm index)
     */
    private boolean wasTakenOnTime(int medicationId, int alarmIndex, String date, String scheduledTime, String period) {
        String key = "taken_" + date + "_" + medicationId + "_" + alarmIndex;
        long takenTimestamp = prefs.getLong(key + "_timestamp", -1);
        if (takenTimestamp == -1) {
            return false; // Not taken at all
        }

        // Parse scheduled time
        long scheduledTimestamp = parseScheduledTime(date, scheduledTime, period);
        if (scheduledTimestamp == -1) {
            return false;
        }

        // Check if taken within 5 minutes (300,000 milliseconds)
        long timeDifference = Math.abs(takenTimestamp - scheduledTimestamp);
        return timeDifference <= 300000; // 5 minutes = 300,000 ms
    }

    /**
     * Legacy method - Get weekly dose statuses for each selected day (single time)
     * Returns array of status codes: 0=not taken, 1=taken on time (green), 2=taken late/missed (red)
     */
    public int[] getWeeklyDoseStatuses(int medicationId, String selectedDays, String time, String period) {
        if (selectedDays == null || selectedDays.isEmpty()) {
            return new int[0];
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        String today = getTodayDate();

        // Get start of this week (Monday)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        // Parse selected days
        String[] days = selectedDays.split(",");
        int[] statuses = new int[days.length];

        // Check each selected day in this week
        for (int i = 0; i < days.length; i++) {
            try {
                int selectedDay = Integer.parseInt(days[i].trim());

                Calendar checkCal = (Calendar) cal.clone();
                checkCal.add(Calendar.DAY_OF_YEAR, selectedDay - 1);

                // Only check days that have already passed (not future days)
                if (!checkCal.after(now)) {
                    String dateStr = sdf.format(checkCal.getTime());
                    String key = "taken_" + dateStr + "_" + medicationId;
                    boolean wasTaken = prefs.getBoolean(key, false);

                    if (wasTaken) {
                        // Check if taken on time
                        boolean onTime = wasTakenOnTime(medicationId, dateStr, time, period);
                        statuses[i] = onTime ? 1 : 2; // 1=green (on time), 2=red (late)
                    } else {
                        // Not taken - check if it's late (more than 5 minutes past scheduled time)
                        long scheduledTimestamp = parseScheduledTime(dateStr, time, period);
                        if (scheduledTimestamp != -1) {
                            long currentTime = now.getTimeInMillis();
                            long timeDifference = currentTime - scheduledTimestamp;

                            // If more than 5 minutes late, mark as red (missed/late)
                            if (timeDifference > 300000) { // 300,000 ms = 5 minutes
                                statuses[i] = 2; // Red - late/missed
                            } else {
                                statuses[i] = 0; // Gray - not taken yet (still within window)
                            }
                        } else {
                            statuses[i] = 0; // Gray - not taken yet
                        }
                    }
                } else {
                    statuses[i] = 0; // Future day - not taken yet
                }
            } catch (NumberFormatException e) {
                statuses[i] = 0;
            }
        }

        return statuses;
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
     * Get dose statuses for prescribed pieces medications
     * Returns array of status codes: 0=not taken (gray), 1=taken (green), 2=skipped/late (red)
     */
    public int[] getPrescribedPiecesDoseStatuses(int medicationId, List<MedicationAlert.AlarmTime> alarmTimes, String startDate, int timesPerDay) {
        if (alarmTimes == null || alarmTimes.isEmpty() || startDate == null) {
            return new int[0];
        }

        int[] statuses = new int[alarmTimes.size()];
        String today = getTodayDate();
        Calendar now = Calendar.getInstance();

        // Parse start date
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        Calendar startCal = Calendar.getInstance();
        try {
            Date startDateObj = sdf.parse(startDate);
            if (startDateObj != null) {
                startCal.setTime(startDateObj);
            } else {
                return statuses; // All zeros (gray)
            }
        } catch (Exception e) {
            return statuses; // All zeros (gray)
        }

        // Calculate interval
        int intervalHours = timesPerDay > 0 ? 24 / timesPerDay : 24;

        // Get the FIRST dose time to use as base
        if (alarmTimes.isEmpty()) {
            return statuses;
        }

        MedicationAlert.AlarmTime firstDose = alarmTimes.get(0);
        String[] firstTimeParts = firstDose.getTime().split(":");
        int firstHour = Integer.parseInt(firstTimeParts[0]);
        int firstMinute = Integer.parseInt(firstTimeParts[1]);

        // Convert to 24-hour format
        if (firstDose.getPeriod().equals("PM") && firstHour != 12) {
            firstHour += 12;
        } else if (firstDose.getPeriod().equals("AM") && firstHour == 12) {
            firstHour = 0;
        }

        // Check each alarm
        for (int alarmIdx = 0; alarmIdx < alarmTimes.size(); alarmIdx++) {
            // Check if this alarm was taken
            String key = "taken_" + today + "_" + medicationId + "_" + alarmIdx;
            boolean wasTaken = prefs.getBoolean(key, false);

            if (wasTaken) {
                statuses[alarmIdx] = 1; // Green - taken
            } else {
                // Calculate scheduled timestamp from START DATE using FIRST dose time
                Calendar scheduledCal = (Calendar) startCal.clone();
                scheduledCal.set(Calendar.HOUR_OF_DAY, firstHour);
                scheduledCal.set(Calendar.MINUTE, firstMinute);
                scheduledCal.set(Calendar.SECOND, 0);
                scheduledCal.set(Calendar.MILLISECOND, 0);

                // Add interval hours for this alarm index
                scheduledCal.add(Calendar.HOUR_OF_DAY, alarmIdx * intervalHours);

                // Check if more than 5 minutes late
                long timeDifference = now.getTimeInMillis() - scheduledCal.getTimeInMillis();
                if (timeDifference > 300000) { // 5 minutes = 300,000 ms
                    statuses[alarmIdx] = 2; // Red - skipped/late
                } else {
                    statuses[alarmIdx] = 0; // Gray - not taken yet (still within window or future)
                }
            }
        }

        return statuses;
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
