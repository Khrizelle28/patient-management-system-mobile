package com.example.patientmanagementsystemmobile.request;

public class CreatePaymentRequest {
    private String type;  // "appointment" or "order"
    private int item_id;
    private double amount;
    private String currency;
    private String description;

    public CreatePaymentRequest(String type, int item_id, double amount, String currency, String description) {
        this.type = type;
        this.item_id = item_id;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
