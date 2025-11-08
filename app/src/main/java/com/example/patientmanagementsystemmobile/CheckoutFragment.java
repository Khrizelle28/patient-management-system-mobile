package com.example.patientmanagementsystemmobile;

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
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.PlaceOrderRequest;
import com.example.patientmanagementsystemmobile.response.OrderResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private TextInputEditText etPickupName, etPhoneNumber, etRemarks;
    private TextView tvItemCount, tvTotalAmount;
    private MaterialButton btnPlaceOrder;
    private ProgressBar progressBar;
    private ImageButton btnBack;

    private double totalAmount = 0.0;
    private int itemCount = 0;

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
                        Toast.makeText(getContext(),
                            orderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
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

    private void showSuccessDialog(OrderResponse orderResponse) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Order Placed Successfully!");
        builder.setMessage("Your order has been placed successfully.\n\nOrder Number: " +
            orderResponse.getData().getOrder().getOrder_number());
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
