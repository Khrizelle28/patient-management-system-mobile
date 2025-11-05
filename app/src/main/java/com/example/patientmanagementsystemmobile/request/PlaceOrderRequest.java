package com.example.patientmanagementsystemmobile.request;

public class PlaceOrderRequest {
    private String pickup_name;
    private String contact_number;
    private String notes;

    public PlaceOrderRequest(String pickup_name, String contact_number, String notes) {
        this.pickup_name = pickup_name;
        this.contact_number = contact_number;
        this.notes = notes;
    }

    public String getPickup_name() {
        return pickup_name;
    }

    public void setPickup_name(String pickup_name) {
        this.pickup_name = pickup_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
