package com.example.patientmanagementsystemmobile.models;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String first_name;
    private String middle_name;
    private String last_name;

    private String contact_no;


    // Constructor, getters, and setters
    public String getId() {
        return id;
    }

    public String getFirstName() { return first_name; }

    public String getMiddleName() { return middle_name; }

    public String getLastName() { return last_name; }

    public String getContactNo() { return contact_no; }

    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public void setMiddleName(String middle_name) {
        this.middle_name = middle_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public void setContactNo(String contact_no) {
        this.contact_no = contact_no;
    }
}