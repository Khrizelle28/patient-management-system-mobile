// Create this file: models/Person.java
package com.example.patientmanagementsystemmobile.models;

public class Person {
    private int id;
    private String name;
    private String specialty;
    private String schedule;
    private boolean isAvailable;
    private String licenseNo;
    private String ptrNo;
    private String email;
    private String dayOfWeek;

    // Default constructor
    public Person() {}

    // Constructor for basic info (what you're currently using)
    public Person(String name, String specialty, String schedule, boolean isAvailable) {
        this.name = name;
        this.specialty = specialty;
        this.schedule = schedule;
        this.isAvailable = isAvailable;
    }

    // Full constructor
    public Person(int id, String name, String specialty, String schedule, boolean isAvailable,
                  String licenseNo, String ptrNo, String email, String dayOfWeek) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.schedule = schedule;
        this.isAvailable = isAvailable;
        this.licenseNo = licenseNo;
        this.ptrNo = ptrNo;
        this.email = email;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getSchedule() {
        return schedule;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public String getPtrNo() {
        return ptrNo;
    }

    public String getEmail() {
        return email;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public void setPtrNo(String ptrNo) {
        this.ptrNo = ptrNo;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", schedule='" + schedule + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}