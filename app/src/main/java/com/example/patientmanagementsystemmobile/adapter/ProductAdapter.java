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

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
        holder.textProductPrice.setText("₱" + product.getPrice());
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

        // Load product image
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = "http://10.0.2.2:8000" + product.getImage();
            Log.d("ProductAdapter", "Loading image for " + product.getName() + ": " + imageUrl);

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("ProductAdapter", "Failed to load image: " + imageUrl, e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("ProductAdapter", "Successfully loaded image: " + imageUrl);
                            return false;
                        }
                    })
                    .into(holder.imageProduct);
        } else {
            Log.d("ProductAdapter", "No image for product: " + product.getName());
            // Set placeholder if no image
            holder.imageProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

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
