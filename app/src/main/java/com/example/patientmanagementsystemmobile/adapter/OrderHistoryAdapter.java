package com.example.patientmanagementsystemmobile.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.Order;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onViewDetailsClick(Order order);
    }

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Set order number
        holder.textOrderNumber.setText("Order #" + order.getOrder_number());

        // Set order date
        String formattedDate = formatDate(order.getCreated_at());
        holder.textOrderDate.setText(formattedDate);

        // Set status with appropriate color
        String status = order.getStatus();
        holder.textOrderStatus.setText(capitalizeStatus(status));
        setStatusBackground(holder.textOrderStatus, status);

        // Set items count
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.textItemsCount.setText(itemCount + (itemCount == 1 ? " item" : " items"));

        // Set total amount
        holder.textTotalAmount.setText(String.format(Locale.getDefault(), "₱%.2f", order.getTotal_amount()));

        // Set pickup name
        holder.textDeliveryAddress.setText("Pickup by: " + order.getPickup_name());

        // Set click listener for view details button
        holder.buttonViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetailsClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // If parsing fails, try alternative format
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (ParseException ex) {
                return dateString;
            }
        }
    }

    private String capitalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    private void setStatusBackground(TextView textView, String status) {
        int backgroundRes;
        switch (status.toLowerCase()) {
            case "pending":
                backgroundRes = R.drawable.status_badge_pending;
                break;
            case "completed":
                backgroundRes = R.drawable.status_badge_completed;
                break;
            case "cancelled":
                backgroundRes = R.drawable.status_badge_cancelled;
                break;
            default:
                backgroundRes = R.drawable.status_badge_pending;
                break;
        }
        textView.setBackground(ContextCompat.getDrawable(textView.getContext(), backgroundRes));
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderNumber;
        TextView textOrderStatus;
        TextView textOrderDate;
        TextView textItemsCount;
        TextView textTotalAmount;
        TextView textDeliveryAddress;
        MaterialButton buttonViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderNumber = itemView.findViewById(R.id.textOrderNumber);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textItemsCount = itemView.findViewById(R.id.textItemsCount);
            textTotalAmount = itemView.findViewById(R.id.textTotalAmount);
            textDeliveryAddress = itemView.findViewById(R.id.textDeliveryAddress);
            buttonViewDetails = itemView.findViewById(R.id.buttonViewDetails);
        }
    }
}
