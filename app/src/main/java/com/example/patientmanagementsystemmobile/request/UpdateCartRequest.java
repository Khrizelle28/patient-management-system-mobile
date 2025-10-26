package com.example.patientmanagementsystemmobile.request;

import com.google.gson.annotations.SerializedName;

public class UpdateCartRequest {
    @SerializedName("quantity")
    private int quantity;

    // Constructor
    public UpdateCartRequest(int quantity) {
        this.quantity = quantity;
    }

    // Getters
    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
