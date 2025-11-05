package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.Order;

import java.util.List;

public class OrderHistoryResponse {
    private boolean success;
    private List<Order> data;
    private String message;

    public OrderHistoryResponse() {
    }

    public OrderHistoryResponse(boolean success, List<Order> data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Order> getData() {
        return data;
    }

    public void setData(List<Order> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
