package com.example.patientmanagementsystemmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Order;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.CreatePaymentRequest;
import com.example.patientmanagementsystemmobile.request.ExecutePaymentRequest;
import com.example.patientmanagementsystemmobile.request.PlaceOrderRequest;
import com.example.patientmanagementsystemmobile.response.OrderResponse;
import com.example.patientmanagementsystemmobile.response.PaymentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private static final int REQUEST_CODE_PAYMENT = 1001;

    private TextInputEditText etPickupName, etPhoneNumber, etRemarks;
    private TextView tvItemCount, tvTotalAmount;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private double totalAmount = 0.0;
    private int itemCount = 0;
    private int currentOrderId = 0;
    private ApiService apiService;

    public static CheckoutFragment newInstance(double totalAmount, int itemCount) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putDouble("total_amount", totalAmount);
        args.putInt("item_count", itemCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        initViews(view);
        loadCartData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        etPickupName = view.findViewById(R.id.etDeliveryAddress);
        etPhoneNumber = view.findViewById(R.id.etContactNumber);
        etRemarks = view.findViewById(R.id.etRemarks);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Auto-fill with patient user data
        autoFillUserData();
    }

    private void autoFillUserData() {
        if (RetrofitClient.currentUser != null) {
            String fullName = RetrofitClient.currentUser.getFullName();
            String contactNo = RetrofitClient.currentUser.getContactNo();

            if (fullName != null && !fullName.isEmpty()) {
                etPickupName.setText(fullName);
            }

            if (contactNo != null && !contactNo.isEmpty()) {
                etPhoneNumber.setText(contactNo);
            }
        }
    }

    private void loadCartData() {
        if (getArguments() != null) {
            totalAmount = getArguments().getDouble("total_amount", 0.0);
            itemCount = getArguments().getInt("item_count", 0);

            tvItemCount.setText(String.valueOf(itemCount));
            tvTotalAmount.setText(String.format("₱%.2f", totalAmount));
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnPlaceOrder.setOnClickListener(v -> {
            String pickupName = etPickupName.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String remarks = etRemarks.getText().toString().trim();

            if (pickupName.isEmpty()) {
                etPickupName.setError("Name is required");
                etPickupName.requestFocus();
                return;
            }

            if (phoneNumber.isEmpty()) {
                etPhoneNumber.setError("Phone number is required");
                etPhoneNumber.requestFocus();
                return;
            }

            // Only send user's remarks to notes, not the pickup time message
            placeOrder(pickupName, phoneNumber, remarks);
        });
    }

    private void placeOrder(String pickupName, String phoneNumber, String notes) {
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        PlaceOrderRequest request = new PlaceOrderRequest(pickupName, phoneNumber, notes);

        apiService.placeOrder(request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    if (orderResponse.isSuccess()) {
                        // Order created successfully, now initiate payment
                        Order order = orderResponse.getData().getOrder();
                        currentOrderId = order.getId();
                        double amount = order.getTotal_amount();

                        Toast.makeText(getContext(),
                            "Order created. Proceeding to payment...", Toast.LENGTH_SHORT).show();

                        // Initiate payment
                        initiatePayment(currentOrderId, amount);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnPlaceOrder.setEnabled(true);
                        Toast.makeText(getContext(),
                            orderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(getContext(),
                        "Failed to place order. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(getContext(),
                    "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initiatePayment(int orderId, double amount) {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
            "order",
            orderId,
            amount,
            "PHP",
            "Order Payment"
        );

        apiService.createPayment(paymentRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PaymentResponse paymentResponse = response.body();
                    String approvalUrl = paymentResponse.getData().getApproval_url();
                    String paymentId = paymentResponse.getData().getPayment_id();

                    // Launch PayPal WebView
                    Intent intent = new Intent(getContext(), PayPalWebViewActivity.class);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_URL, approvalUrl);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_PAYMENT_TYPE, "order");
                    intent.putExtra(PayPalWebViewActivity.EXTRA_ITEM_ID, orderId);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_PAYMENT_ID, paymentId);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                } else {
                    btnPlaceOrder.setEnabled(true);
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to create payment";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(getContext(),
                    "Payment initialization failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                String payerId = data.getStringExtra("payer_id");
                String paymentId = data.getStringExtra("payment_id");
                String paymentType = data.getStringExtra("payment_type");

                if (payerId != null && paymentId != null) {
                    executePayment(paymentId, payerId, paymentType);
                } else {
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();
                }
            } else {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executePayment(String paymentId, String payerId, String paymentType) {
        progressBar.setVisibility(View.VISIBLE);

        ExecutePaymentRequest executeRequest = new ExecutePaymentRequest(paymentId, payerId, paymentType);

        apiService.executePayment(executeRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showPaymentSuccessDialog();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Payment execution failed";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(getContext(),
                    "Payment execution failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPaymentSuccessDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Payment Successful!");
        builder.setMessage("Your payment has been completed successfully.\n\nYour order is now confirmed and ready for pickup.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Go back to home fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
