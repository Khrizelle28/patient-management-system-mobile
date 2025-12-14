package com.example.patientmanagementsystemmobile.models;

import java.util.ArrayList;
import java.util.List;

public class MedicationAlert {
    // Inner class for alarm time
    public static class AlarmTime {
        private String time;   // "09:00"
        private String period; // "AM" or "PM"

        public AlarmTime(String time, String period) {
            this.time = time;
            this.period = period;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getFullTime() {
            return time + " " + period;
        }
    }

    private int id;
    private String time;  // Legacy field, kept for backward compatibility
    private String period; // Legacy field, kept for backward compatibility
    private List<AlarmTime> alarmTimes; // New field for multiple alarm times
    private String medicationName;
    private String remarks; // Description or notes
    private boolean isEnabled;
    private String selectedDays; // Comma-separated list of selected days (1=Monday, 7=Sunday)
    private int durationDays; // Duration in days
    private String startDate; // Start date in yyyy-MM-dd format

    // Prescribed pieces feature fields
    private int prescribedPieces; // Total number of doses in the medication course
    private int timesPerDay; // How many times per day to take medication
    private String startDay; // Starting day of the week (e.g., "Sunday")
    private String firstDoseTime; // Time of the first dose (e.g., "08:00")
    private String firstDosePeriod; // Period of the first dose (AM/PM)

    public MedicationAlert(int id, String time, String period, String medicationName, String remarks, boolean isEnabled) {
        this.id = id;
        this.time = time;
        this.period = period;
        this.alarmTimes = new ArrayList<>();
        this.alarmTimes.add(new AlarmTime(time, period));
        this.medicationName = medicationName;
        this.remarks = remarks;
        this.isEnabled = isEnabled;
        this.selectedDays = "";
        this.durationDays = 0;
        this.startDate = "";
    }

    // Constructor with all fields
    public MedicationAlert(int id, String time, String period, String medicationName, String remarks,
                          boolean isEnabled, String selectedDays, int durationDays, String startDate) {
        this.id = id;
        this.time = time;
        this.period = period;
        this.alarmTimes = new ArrayList<>();
        this.alarmTimes.add(new AlarmTime(time, period));
        this.medicationName = medicationName;
        this.remarks = remarks;
        this.isEnabled = isEnabled;
        this.selectedDays = selectedDays;
        this.durationDays = durationDays;
        this.startDate = startDate;
    }

    // Constructor without ID (for creating new alerts)
    public MedicationAlert(String time, String period, String medicationName, String remarks, boolean isEnabled) {
        this.id = -1; // Unassigned ID
        this.time = time;
        this.period = period;
        this.alarmTimes = new ArrayList<>();
        this.alarmTimes.add(new AlarmTime(time, period));
        this.medicationName = medicationName;
        this.remarks = remarks;
        this.isEnabled = isEnabled;
        this.selectedDays = "";
        this.durationDays = 0;
        this.startDate = "";
    }

    // Legacy constructor for backward compatibility
    public MedicationAlert(String time, String period, String medicationName, boolean isEnabled) {
        this.id = -1;
        this.time = time;
        this.period = period;
        this.alarmTimes = new ArrayList<>();
        this.alarmTimes.add(new AlarmTime(time, period));
        this.medicationName = medicationName;
        this.remarks = "";
        this.isEnabled = isEnabled;
        this.selectedDays = "";
        this.durationDays = 0;
        this.startDate = "";
    }

    // Getters
    public int getId() {
        return id;
    }

    // Legacy getters (return first alarm time for backward compatibility)
    public String getTime() {
        if (alarmTimes != null && !alarmTimes.isEmpty()) {
            return alarmTimes.get(0).getTime();
        }
        return time != null ? time : "";
    }

    public String getPeriod() {
        if (alarmTimes != null && !alarmTimes.isEmpty()) {
            return alarmTimes.get(0).getPeriod();
        }
        return period != null ? period : "AM";
    }

    // New getter for alarm times list
    public List<AlarmTime> getAlarmTimes() {
        if (alarmTimes == null) {
            alarmTimes = new ArrayList<>();
            if (time != null && !time.isEmpty()) {
                alarmTimes.add(new AlarmTime(time, period != null ? period : "AM"));
            }
        }
        return alarmTimes;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getSelectedDays() {
        return selectedDays;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public String getStartDate() {
        return startDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setSelectedDays(String selectedDays) {
        this.selectedDays = selectedDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // New setter for alarm times list
    public void setAlarmTimes(List<AlarmTime> alarmTimes) {
        this.alarmTimes = alarmTimes;
        // Update legacy fields for backward compatibility
        if (alarmTimes != null && !alarmTimes.isEmpty()) {
            this.time = alarmTimes.get(0).getTime();
            this.period = alarmTimes.get(0).getPeriod();
        }
    }

    public String getFullTime() {
        return getTime() + " " + getPeriod();
    }

    // Getters and setters for prescribed pieces feature
    public int getPrescribedPieces() {
        return prescribedPieces;
    }

    public void setPrescribedPieces(int prescribedPieces) {
        this.prescribedPieces = prescribedPieces;
    }

    public int getTimesPerDay() {
        return timesPerDay;
    }

    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public String getFirstDoseTime() {
        return firstDoseTime;
    }

    public void setFirstDoseTime(String firstDoseTime) {
        this.firstDoseTime = firstDoseTime;
    }

    public String getFirstDosePeriod() {
        return firstDosePeriod;
    }

    public void setFirstDosePeriod(String firstDosePeriod) {
        this.firstDosePeriod = firstDosePeriod;
    }
}
