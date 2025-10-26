package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.Cart;
import com.example.patientmanagementsystemmobile.models.CartItem;
import com.google.gson.annotations.SerializedName;

public class CartResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private CartData data;

    // Constructor
    public CartResponse() {
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public CartData getData() {
        return data;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(CartData data) {
        this.data = data;
    }

    // Inner class for cart data
    public static class CartData {
        @SerializedName("cart")
        private Cart cart;

        @SerializedName("item")
        private CartItem item;

        @SerializedName("total")
        private String total;

        @SerializedName("items_count")
        private int itemsCount;

        public Cart getCart() {
            return cart;
        }

        public void setCart(Cart cart) {
            this.cart = cart;
        }

        public CartItem getItem() {
            return item;
        }

        public void setItem(CartItem item) {
            this.item = item;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public int getItemsCount() {
            return itemsCount;
        }

        public void setItemsCount(int itemsCount) {
            this.itemsCount = itemsCount;
        }
    }
}
