package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.ProductAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Product;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.repository.CartRepository;
import com.example.patientmanagementsystemmobile.response.CartResponse;
import com.example.patientmanagementsystemmobile.response.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ShopFragment extends Fragment implements ProductAdapter.OnProductListener {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private ApiService apiService;
    private CartRepository cartRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        initViews(view);
        setupRecyclerView();
        setupServices();
        loadProducts();

        return view;
    }

    private void initViews(View view) {
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(new ArrayList<>(), getContext(), this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupServices() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        cartRepository = new CartRepository(apiService);
    }

    private void loadProducts() {
        showLoading(true);
        Call<ProductResponse> call = apiService.getProducts();
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getData();
                    productAdapter.updateProducts(products);

                    if (products.isEmpty()) {
                        showError("No products available");
                    }
                } else {
                    showError("Failed to load products");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAddToCart(Product product) {
        // Add 1 quantity by default
        addProductToCart(product, 1);
    }

    @Override
    public void onProductClick(Product product) {
        // TODO: Show product details dialog or navigate to product detail screen
        Toast.makeText(getContext(), "Product: " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    private void addProductToCart(Product product, int quantity) {
        progressBar.setVisibility(View.VISIBLE);
        cartRepository.addToCart(product.getId(), quantity, new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    showError(response.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
