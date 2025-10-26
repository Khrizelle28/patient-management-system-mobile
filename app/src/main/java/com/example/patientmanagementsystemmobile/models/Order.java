package com.example.patientmanagementsystemmobile.models;

import java.util.List;

public class Order {
    private int id;
    private int patient_user_id;
    private String order_number;
    private double total_amount;
    private String status;
    private String delivery_address;
    private String contact_number;
    private String notes;
    private String created_at;
    private String updated_at;
    private List<OrderItem> items;

    public Order() {
    }

    public Order(int id, int patient_user_id, String order_number, double total_amount,
                 String status, String delivery_address, String contact_number, String notes,
                 String created_at, String updated_at, List<OrderItem> items) {
        this.id = id;
        this.patient_user_id = patient_user_id;
        this.order_number = order_number;
        this.total_amount = total_amount;
        this.status = status;
        this.delivery_address = delivery_address;
        this.contact_number = contact_number;
        this.notes = notes;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.items = items;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatient_user_id() {
        return patient_user_id;
    }

    public void setPatient_user_id(int patient_user_id) {
        this.patient_user_id = patient_user_id;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
