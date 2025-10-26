package com.example.patientmanagementsystemmobile.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("id")
    private int id;

    @SerializedName("cart_id")
    private int cartId;

    @SerializedName("product_id")
    private int productId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private String price;

    @SerializedName("product")
    private Product product;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructor
    public CartItem() {
    }

    public CartItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCartId() {
        return cartId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public Product getProduct() {
        return product;
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

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Calculate subtotal for this item
    public double getSubtotal() {
        try {
            double priceValue = Double.parseDouble(price);
            return priceValue * quantity;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
