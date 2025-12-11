package com.example.patientmanagementsystemmobile.request;

public class MedicationAlertRequest {
    private String patient_id;
    private String time;
    private String period;
    private String medication_name;
    private String remarks;
    private boolean is_enabled;
    private String selected_days;
    private String start_date;
    private int duration_days;

    public MedicationAlertRequest(String patient_id, String time, String period, String medication_name, String remarks, boolean is_enabled,
                                  String selected_days, String start_date, int duration_days) {
        this.patient_id = patient_id;
        this.time = time;
        this.period = period;
        this.medication_name = medication_name;
        this.remarks = remarks;
        this.is_enabled = is_enabled;
        this.selected_days = selected_days;
        this.start_date = start_date;
        this.duration_days = duration_days;
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

    public String getSelected_days() {
        return selected_days;
    }

    public String getStart_date() {
        return start_date;
    }

    public int getDuration_days() {
        return duration_days;
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

    public void setSelected_days(String selected_days) {
        this.selected_days = selected_days;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public void setDuration_days(int duration_days) {
        this.duration_days = duration_days;
    }
}
