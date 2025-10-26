package com.example.patientmanagementsystemmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.R;
import com.example.patientmanagementsystemmobile.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private Context context;
    private OnProductListener listener;

    public interface OnProductListener {
        void onAddToCart(Product product);
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, Context context, OnProductListener listener) {
        this.products = products;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.textProductName.setText(product.getName());
        holder.textProductPrice.setText("$" + product.getPrice());
        holder.textProductStock.setText("Stock: " + product.getStock());

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            holder.textProductDescription.setText(product.getDescription());
            holder.textProductDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textProductDescription.setVisibility(View.GONE);
        }

        // Check if product is out of stock
        if (product.getStock() <= 0) {
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setText("Out of Stock");
        } else {
            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setText("Add to Cart");
        }

        // Optional: Load product image if you have image loading library
        // Glide.with(context).load(product.getImage()).into(holder.imageProduct);

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(product);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName;
        TextView textProductDescription;
        TextView textProductPrice;
        TextView textProductStock;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductDescription = itemView.findViewById(R.id.textProductDescription);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductStock = itemView.findViewById(R.id.textProductStock);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
