package com.example.patientmanagementsystemmobile.models;

public class MedicationAlert {
    private String time;
    private String period; // AM or PM
    private String medicationName;
    private boolean isEnabled;

    public MedicationAlert(String time, String period, String medicationName, boolean isEnabled) {
        this.time = time;
        this.period = period;
        this.medicationName = medicationName;
        this.isEnabled = isEnabled;
    }

    // Getters
    public String getTime() {
        return time;
    }

    public String getPeriod() {
        return period;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    // Setters
    public void setTime(String time) {
        this.time = time;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getFullTime() {
        return time + " " + period;
    }
}