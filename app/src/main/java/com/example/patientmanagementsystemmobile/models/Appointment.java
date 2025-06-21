package com.example.patientmanagementsystemmobile.models;

import com.example.patientmanagementsystemmobile.enums.AppointmentStatus;

public class Appointment {
    private int id;
    private String doctorName;
    private String specialty;
    private String date;
    private String time;
    private String purpose;
    private String status;

    public Appointment(int id, String doctorName, String specialty, String date,
                       String time, String purpose, String status) {
        this.id = id;
        this.doctorName = doctorName;
        this.specialty = specialty;
        this.date = date;
        this.time = time;
        this.purpose = purpose;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getSpecialty() { return specialty; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setStatus(String status) { this.status = status; }
}
