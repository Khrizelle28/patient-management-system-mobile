package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.data.AppointmentData;

import java.util.List;

public class AppointmentListResponse {
    private boolean success;
    private List<AppointmentData> data;
    private String message;

    public AppointmentListResponse(boolean success, List<AppointmentData> data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public List<AppointmentData> getData() { return data; }
    public String getMessage() { return message; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setData(List<AppointmentData> data) { this.data = data; }
    public void setMessage(String message) { this.message = message; }
}
