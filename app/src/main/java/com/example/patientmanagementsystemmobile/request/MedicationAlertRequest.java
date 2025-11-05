package com.example.patientmanagementsystemmobile.request;

public class MedicationAlertRequest {
    private String patient_id;
    private String time;
    private String period;
    private String medication_name;
    private String remarks;
    private boolean is_enabled;

    public MedicationAlertRequest(String patient_id, String time, String period, String medication_name, String remarks, boolean is_enabled) {
        this.patient_id = patient_id;
        this.time = time;
        this.period = period;
        this.medication_name = medication_name;
        this.remarks = remarks;
        this.is_enabled = is_enabled;
    }

    // Getters
    public String getPatient_id() {
        return patient_id;
    }

    public String getTime() {
        return time;
    }

    public String getPeriod() {
        return period;
    }

    public String getMedication_name() {
        return medication_name;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isIs_enabled() {
        return is_enabled;
    }

    // Setters
    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setMedication_name(String medication_name) {
        this.medication_name = medication_name;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setIs_enabled(boolean is_enabled) {
        this.is_enabled = is_enabled;
    }
}
