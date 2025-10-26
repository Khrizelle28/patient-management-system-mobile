package com.example.patientmanagementsystemmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, Context context, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        if (item.getProduct() != null) {
            holder.textProductName.setText(item.getProduct().getName());
            holder.textProductPrice.setText("$" + String.format("%.2f", Double.parseDouble(item.getPrice())));
            holder.textQuantity.setText("x" + String.valueOf(item.getQuantity()));

            // Set description if available
            if (item.getProduct().getDescription() != null && !item.getProduct().getDescription().isEmpty()) {
                holder.textProductDescription.setText(item.getProduct().getDescription());
            }

            // Optional: Load product image if you have image loading library
            // Glide.with(context).load(item.getProduct().getImage()).into(holder.imageProduct);
        }

        // Decrease quantity
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                listener.onQuantityChanged(item, currentQuantity - 1);
            }
        });

        // Increase quantity
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            int stock = item.getProduct() != null ? item.getProduct().getStock() : Integer.MAX_VALUE;
            if (currentQuantity < stock) {
                listener.onQuantityChanged(item, currentQuantity + 1);
            }
        });

        // Remove item
        holder.btnRemove.setOnClickListener(v -> {
            listener.onItemRemoved(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newCartItems) {
        this.cartItems = newCartItems;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName;
        TextView textProductDescription;
        TextView textProductPrice;
        TextView textQuantity;
        ImageButton btnDecrease;
        ImageButton btnIncrease;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductDescription = itemView.findViewById(R.id.textProductDescription);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
