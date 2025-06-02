package com.example.patientmanagementsystemmobile.models;

public class Person {
    private String name;
    private String specialty;
    private String availableTime;
    private boolean isAvailable;

    public Person(String name, String specialty, String availableTime, boolean isAvailable) {
        this.name = name;
        this.specialty = specialty;
        this.availableTime = availableTime;
        this.isAvailable = isAvailable;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}