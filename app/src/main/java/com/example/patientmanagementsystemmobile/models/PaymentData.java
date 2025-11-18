package com.example.patientmanagementsystemmobile.models;

public class PaymentData {
    private String type;
    private int item_id;
    private String payment_id;
    private String approval_url;
    private String status;

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

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getApproval_url() {
        return approval_url;
    }

    public void setApproval_url(String approval_url) {
        this.approval_url = approval_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
