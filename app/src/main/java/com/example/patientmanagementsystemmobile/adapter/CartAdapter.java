package com.example.patientmanagementsystemmobile.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.CartItem;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;

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

        // Remove existing TextWatcher to avoid multiple listeners
        if (holder.quantityWatcher != null) {
            holder.textQuantity.removeTextChangedListener(holder.quantityWatcher);
        }

        if (item.getProduct() != null) {
            holder.textProductName.setText(item.getProduct().getName());
            holder.textProductPrice.setText("₱" + String.format("%.2f", Double.parseDouble(item.getPrice())));
            holder.textQuantity.setText(String.valueOf(item.getQuantity()));

            // Set description if available
            if (item.getProduct().getDescription() != null && !item.getProduct().getDescription().isEmpty()) {
                holder.textProductDescription.setText(item.getProduct().getDescription());
            }

            // Load product image
            if (item.getProduct().getImage() != null && !item.getProduct().getImage().isEmpty()) {
                String imageUrl = RetrofitClient.getFullImageUrl(item.getProduct().getImage());
                android.util.Log.d("CartAdapter", "Image path from API: " + item.getProduct().getImage());
                android.util.Log.d("CartAdapter", "Full image URL: " + imageUrl);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageProduct);
            } else {
                holder.imageProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Handle manual quantity input
        holder.quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String quantityStr = s.toString().trim();
                    if (!quantityStr.isEmpty()) {
                        int newQuantity = Integer.parseInt(quantityStr);
                        int stock = item.getProduct() != null ? item.getProduct().getStock() : Integer.MAX_VALUE;

                        if (newQuantity > 0 && newQuantity <= stock) {
                            if (newQuantity != item.getQuantity()) {
                                listener.onQuantityChanged(item, newQuantity);
                            }
                        } else if (newQuantity > stock) {
                            holder.textQuantity.setText(String.valueOf(stock));
                        } else if (newQuantity <= 0) {
                            holder.textQuantity.setText("1");
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        };
        holder.textQuantity.addTextChangedListener(holder.quantityWatcher);

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
        EditText textQuantity;
        ImageButton btnDecrease;
        ImageButton btnIncrease;
        ImageButton btnRemove;
        TextWatcher quantityWatcher;

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
