package com.example.patientmanagementsystemmobile.models;

public class MedicationAlert {
    private int id;
    private String time;
    private String period; // AM or PM
    private String medicationName;
    private String remarks; // Description or notes
    private boolean isEnabled;
    private String selectedDays; // Comma-separated list of selected days (1=Monday, 7=Sunday)
    private int durationDays; // Duration in days
    private String startDate; // Start date in yyyy-MM-dd format

    public MedicationAlert(int id, String time, String period, String medicationName, String remarks, boolean isEnabled) {
        this.id = id;
        this.time = time;
        this.period = period;
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

    public String getTime() {
        return time;
    }

    public String getPeriod() {
        return period;
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

    public String getFullTime() {
        return time + " " + period;
    }
}
