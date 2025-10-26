package com.example.patientmanagementsystemmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.PlaceOrderRequest;
import com.example.patientmanagementsystemmobile.response.OrderResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private TextInputEditText etDeliveryAddress, etContactNumber, etNotes;
    private TextView tvItemCount, tvTotalAmount;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private double totalAmount = 0.0;
    private int itemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        loadCartData();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        etContactNumber = findViewById(R.id.etContactNumber);
        etNotes = findViewById(R.id.etNotes);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadCartData() {
        Intent intent = getIntent();
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        itemCount = intent.getIntExtra("item_count", 0);

        tvItemCount.setText(String.valueOf(itemCount));
        tvTotalAmount.setText(String.format("₱%.2f", totalAmount));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> {
            String deliveryAddress = etDeliveryAddress.getText().toString().trim();
            String contactNumber = etContactNumber.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            if (deliveryAddress.isEmpty()) {
                etDeliveryAddress.setError("Delivery address is required");
                etDeliveryAddress.requestFocus();
                return;
            }

            if (contactNumber.isEmpty()) {
                etContactNumber.setError("Contact number is required");
                etContactNumber.requestFocus();
                return;
            }

            placeOrder(deliveryAddress, contactNumber, notes);
        });
    }

    private void placeOrder(String deliveryAddress, String contactNumber, String notes) {
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        PlaceOrderRequest request = new PlaceOrderRequest(deliveryAddress, contactNumber, notes);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.placeOrder(request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    if (orderResponse.isSuccess()) {
                        showSuccessDialog(orderResponse);
                    } else {
                        Toast.makeText(CheckoutActivity.this,
                            orderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this,
                        "Failed to place order. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this,
                    "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(OrderResponse orderResponse) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Placed Successfully!");
        builder.setMessage("Your order has been placed successfully.\n\nOrder Number: " +
            orderResponse.getData().getOrder().getOrder_number());
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Go back to home
            Intent intent = new Intent(CheckoutActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}
