package com.example.patientmanagementsystemmobile.response;


import com.example.patientmanagementsystemmobile.data.AppointmentData;

// AppointmentResponse.java
public class AppointmentResponse {
    private boolean success;
    private String message;
    private AppointmentData data;

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AppointmentData getData() { return data; }
    public void setData(AppointmentData data) { this.data = data; }
}