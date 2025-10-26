package com.example.patientmanagementsystemmobile.request;

public class PlaceOrderRequest {
    private String delivery_address;
    private String contact_number;
    private String notes;

    public PlaceOrderRequest(String delivery_address, String contact_number, String notes) {
        this.delivery_address = delivery_address;
        this.contact_number = contact_number;
        this.notes = notes;
    }

    public String getDelivery_address() {
        return delivery_address;
    }

    public void setDelivery_address(String delivery_address) {
        this.delivery_address = delivery_address;
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
