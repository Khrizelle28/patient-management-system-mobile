package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.PaymentData;

public class PaymentResponse {
    private boolean success;
    private String message;
    private PaymentData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }
}
