package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.Product;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    @SerializedName("data")
    private List<Product> data;

    public ProductResponse() {
    }

    public List<Product> getData() {
        return data;
    }

    public void setData(List<Product> data) {
        this.data = data;
    }
}
