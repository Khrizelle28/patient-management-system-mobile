package com.example.patientmanagementsystemmobile.request;

// AppointmentRequest.java
public class AppointmentRequest {
    private String patient_id;
    private String doctor_id;
    private String appointment_date;
    private String appointment_time;
    private String notes;

    public AppointmentRequest(String patientId, String doctorId, String date, String time, String notes) {
        this.patient_id = patientId;
        this.doctor_id = doctorId;
        this.appointment_date = date;
        this.appointment_time = time;
        this.notes = notes;
    }

    // Getters and setters
    public String getPatient_id() { return patient_id; }
    public void setPatient_id(String patient_id) { this.patient_id = patient_id; }

    public String getDoctor_id() { return doctor_id; }
    public void setDoctor_id(String doctor_id) { this.doctor_id = doctor_id; }

    public String getAppointment_date() { return appointment_date; }
    public void setAppointment_date(String appointment_date) { this.appointment_date = appointment_date; }

    public String getAppointment_time() { return appointment_time; }
    public void setAppointment_time(String appointment_time) { this.appointment_time = appointment_time; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
