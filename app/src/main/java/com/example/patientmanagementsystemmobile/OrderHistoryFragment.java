package com.example.patientmanagementsystemmobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.OrderHistoryAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Order;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.response.OrderHistoryResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {

    private static final String TAG = "OrderHistoryFragment";

    private RecyclerView recyclerViewOrderHistory;
    private LinearLayout emptyOrderHistoryLayout;
    private LinearLayout loadingLayout;
    private ImageView buttonBack;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Order> orderList;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews(view);
        setupOrderHistoryRecyclerView();
        setClickListeners();
        loadOrderHistory();
    }

    private void initViews(View view) {
        recyclerViewOrderHistory = view.findViewById(R.id.recyclerViewOrderHistory);
        emptyOrderHistoryLayout = view.findViewById(R.id.emptyOrderHistoryLayout);
        loadingLayout = view.findViewById(R.id.loadingLayout);
        buttonBack = view.findViewById(R.id.buttonBack);
    }

    private void setupOrderHistoryRecyclerView() {
        orderList = new ArrayList<>();
        orderHistoryAdapter = new OrderHistoryAdapter(orderList);

        recyclerViewOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrderHistory.setAdapter(orderHistoryAdapter);

        // Set click listener for view details
        orderHistoryAdapter.setOnOrderClickListener(order -> {
            showOrderDetailsDialog(order);
        });
    }

    private void setClickListeners() {
        buttonBack.setOnClickListener(v -> {
            // Navigate back to profile
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void loadOrderHistory() {
        if (RetrofitClient.currentUser == null) {
            Log.e(TAG, "Current user is null");
            showEmptyState();
            return;
        }

        showLoadingState();

        Call<OrderHistoryResponse> call = apiService.getOrderHistory();
        call.enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                hideLoadingState();

                if (response.isSuccessful() && response.body() != null) {
                    OrderHistoryResponse orderHistoryResponse = response.body();

                    if (orderHistoryResponse.isSuccess() && orderHistoryResponse.getData() != null) {
                        List<Order> orders = orderHistoryResponse.getData();

                        if (orders.isEmpty()) {
                            showEmptyState();
                        } else {
                            showOrderList();
                            orderList.clear();
                            orderList.addAll(orders);
                            orderHistoryAdapter.updateOrders(orderList);
                        }
                    } else {
                        showEmptyState();
                        Log.e(TAG, "Order history response not successful");
                    }
                } else {
                    showEmptyState();
                    Log.e(TAG, "Failed to load order history: " + response.code());
                    Toast.makeText(getContext(), "Failed to load order history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                hideLoadingState();
                showEmptyState();
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingState() {
        recyclerViewOrderHistory.setVisibility(View.GONE);
        emptyOrderHistoryLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoadingState() {
        loadingLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerViewOrderHistory.setVisibility(View.GONE);
        emptyOrderHistoryLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void showOrderList() {
        recyclerViewOrderHistory.setVisibility(View.VISIBLE);
        emptyOrderHistoryLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);
    }

    private void showOrderDetailsDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Order Details");

        // Build message with order details
        StringBuilder message = new StringBuilder();
        message.append("Order Number: ").append(order.getOrder_number()).append("\n\n");
        message.append("Status: ").append(capitalizeStatus(order.getStatus())).append("\n\n");
        message.append("Pickup By: ").append(order.getPickup_name()).append("\n\n");
        message.append("Contact: ").append(order.getContact_number()).append("\n\n");

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            message.append("Notes: ").append(order.getNotes()).append("\n\n");
        }

        message.append("Items:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (int i = 0; i < order.getItems().size(); i++) {
                var item = order.getItems().get(i);
                message.append((i + 1)).append(". ");

                if (item.getProduct() != null) {
                    message.append(item.getProduct().getName());
                } else {
                    message.append("Product ID: ").append(item.getProduct_id());
                }

                message.append(" x").append(item.getQuantity());
                message.append(" - ₱").append(String.format("%.2f", item.getSubtotal()));
                message.append("\n");
            }
        }

        message.append("\nTotal Amount: ₱").append(String.format("%.2f", order.getTotal_amount()));

        builder.setMessage(message.toString());
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String capitalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }
}
