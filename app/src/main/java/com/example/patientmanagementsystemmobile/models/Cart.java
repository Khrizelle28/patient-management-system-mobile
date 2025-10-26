package com.example.patientmanagementsystemmobile.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Cart {
    @SerializedName("id")
    private int id;

    @SerializedName("patient_user_id")
    private int patientUserId;

    @SerializedName("items")
    private List<CartItem> items;

    @SerializedName("total")
    private String total;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructor
    public Cart() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getPatientUserId() {
        return patientUserId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public String getTotal() {
        return total;
    }

    // Helper method to get total as double
    public double getTotalAsDouble() {
        try {
            return total != null ? Double.parseDouble(total) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPatientUserId(int patientUserId) {
        this.patientUserId = patientUserId;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
