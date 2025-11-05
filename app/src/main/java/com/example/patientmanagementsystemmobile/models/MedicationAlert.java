package com.example.patientmanagementsystemmobile.models;

public class MedicationAlert {
    private int id;
    private String time;
    private String period; // AM or PM
    private String medicationName;
    private String remarks; // Description or notes
    private boolean isEnabled;

    public MedicationAlert(int id, String time, String period, String medicationName, String remarks, boolean isEnabled) {
        this.id = id;
        this.time = time;
        this.period = period;
        this.medicationName = medicationName;
        this.remarks = remarks;
        this.isEnabled = isEnabled;
    }

    // Constructor without ID (for creating new alerts)
    public MedicationAlert(String time, String period, String medicationName, String remarks, boolean isEnabled) {
        this.id = -1; // Unassigned ID
        this.time = time;
        this.period = period;
        this.medicationName = medicationName;
        this.remarks = remarks;
        this.isEnabled = isEnabled;
    }

    // Legacy constructor for backward compatibility
    public MedicationAlert(String time, String period, String medicationName, boolean isEnabled) {
        this.id = -1;
        this.time = time;
        this.period = period;
        this.medicationName = medicationName;
        this.remarks = "";
        this.isEnabled = isEnabled;
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

    public String getFullTime() {
        return time + " " + period;
    }
}