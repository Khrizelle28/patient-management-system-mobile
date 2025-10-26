package com.example.patientmanagementsystemmobile.response;

import com.example.patientmanagementsystemmobile.models.Product;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    @SerializedName("data")
    private List<Product> data;

    @SerializedName("products")
    private List<Product> products;

    // Handle case where backend returns array directly at root
    @SerializedName("available_products")
    private List<Product> availableProducts;

    public ProductResponse() {
    }

    public List<Product> getData() {
        // Try multiple fields to support different backend response formats
        if (data != null && !data.isEmpty()) {
            return data;
        }
        if (products != null && !products.isEmpty()) {
            return products;
        }
        if (availableProducts != null && !availableProducts.isEmpty()) {
            return availableProducts;
        }
        return data; // Return data (might be null) as fallback
    }

    public void setData(List<Product> data) {
        this.data = data;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setAvailableProducts(List<Product> availableProducts) {
        this.availableProducts = availableProducts;
    }
}
