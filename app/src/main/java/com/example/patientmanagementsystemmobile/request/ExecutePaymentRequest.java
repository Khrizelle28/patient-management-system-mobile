package com.example.patientmanagementsystemmobile.request;

public class ExecutePaymentRequest {
    private String payment_id;
    private String payer_id;
    private String type;  // "appointment" or "order"

    public ExecutePaymentRequest(String payment_id, String payer_id, String type) {
        this.payment_id = payment_id;
        this.payer_id = payer_id;
        this.type = type;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getPayer_id() {
        return payer_id;
    }

    public void setPayer_id(String payer_id) {
        this.payer_id = payer_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
