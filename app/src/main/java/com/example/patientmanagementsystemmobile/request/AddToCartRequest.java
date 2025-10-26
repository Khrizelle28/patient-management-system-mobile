package com.example.patientmanagementsystemmobile.request;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    @SerializedName("product_id")
    private int productId;

    @SerializedName("quantity")
    private int quantity;

    // Constructor
    public AddToCartRequest(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
