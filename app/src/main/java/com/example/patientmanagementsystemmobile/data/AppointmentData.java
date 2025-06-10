package com.example.patientmanagementsystemmobile.data;

// AppointmentData.java
public class AppointmentData {
    private int id;
    private int patient_id;
    private int doctor_id;
    private String appointment_date;
    private String appointment_time;
    private String status;
    private String notes;

    // Constructors
    public AppointmentData() {}

    public AppointmentData(int id, int patient_id, int doctor_id, String appointment_date,
                           String appointment_time, String status, String notes) {
        this.id = id;
        this.patient_id = patient_id;
        this.doctor_id = doctor_id;
        this.appointment_date = appointment_date;
        this.appointment_time = appointment_time;
        this.status = status;
        this.notes = notes;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getPatient_id() {
        return patient_id;
    }

    public int getDoctor_id() {
        return doctor_id;
    }

    public String getAppointment_date() {
        return appointment_date;
    }

    public String getAppointment_time() {
        return appointment_time;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPatient_id(int patient_id) {
        this.patient_id = patient_id;
    }

    public void setDoctor_id(int doctor_id) {
        this.doctor_id = doctor_id;
    }

    public void setAppointment_date(String appointment_date) {
        this.appointment_date = appointment_date;
    }

    public void setAppointment_time(String appointment_time) {
        this.appointment_time = appointment_time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}