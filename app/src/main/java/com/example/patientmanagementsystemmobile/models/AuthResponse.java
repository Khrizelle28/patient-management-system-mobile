package com.example.patientmanagementsystemmobile.models;

public class AuthResponse {
    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() { return user; }
}