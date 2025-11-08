package com.example.patientmanagementsystemmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientmanagementsystemmobile.adapter.CartAdapter;
import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Cart;
import com.example.patientmanagementsystemmobile.models.CartItem;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.repository.CartRepository;
import com.example.patientmanagementsystemmobile.response.CartResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private TextView textTotal;
    private TextView textTotalCheckout;
    private Button btnCheckout;
    private Button btnClearCart;
    private Button btnStartShopping;
    private android.widget.ImageButton btnBack;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyCart;
    private View cardSummary;

    private CartRepository cartRepository;
    private Cart currentCart;
    private String currentTotal = "0.00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        initViews(view);
        setupRecyclerView();
        setupRepository();
        setupClickListeners();
        loadCart();

        return view;
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        textTotal = view.findViewById(R.id.textTotal);
        textTotalCheckout = view.findViewById(R.id.textTotalCheckout);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnClearCart = view.findViewById(R.id.btnClearCart);
        btnStartShopping = view.findViewById(R.id.btnStartShopping);
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        cardSummary = view.findViewById(R.id.cardSummary);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(new ArrayList<>(), getContext(), this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupRepository() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        cartRepository = new CartRepository(apiService);
    }

    private void setupClickListeners() {
        btnCheckout.setOnClickListener(v -> handleCheckout());
        btnClearCart.setOnClickListener(v -> confirmClearCart());
        btnStartShopping.setOnClickListener(v -> navigateToShop());
        btnBack.setOnClickListener(v -> navigateToShop());
    }

    private void loadCart() {
        android.util.Log.d("CartFragment", "Loading cart...");
        showLoading(true);
        cartRepository.getCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                showLoading(false);
                android.util.Log.d("CartFragment", "Cart loaded successfully: " + response.isSuccess());

                if (response.isSuccess() && response.getData() != null) {
                    currentCart = response.getData().getCart();
                    currentTotal = response.getData().getTotal(); // Get total from CartData
                    android.util.Log.d("CartFragment", "Cart items: " + (currentCart != null && currentCart.getItems() != null ? currentCart.getItems().size() : "null"));
                    android.util.Log.d("CartFragment", "Cart total: " + currentTotal);
                    updateUI();
                } else {
                    android.util.Log.w("CartFragment", "Cart response not successful or data is null");
                    showEmptyCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                android.util.Log.e("CartFragment", "Error loading cart: " + message);
                showError("Failed to load cart: " + message);
                showEmptyCart();
            }
        });
    }

    private void updateUI() {
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            showEmptyCart();
        } else {
            showCartWithItems();
            cartAdapter.updateCartItems(currentCart.getItems());
            updateTotals();
        }
    }

    private void updateTotals() {
        try {
            double total = currentTotal != null ? Double.parseDouble(currentTotal) : 0.0;
            textTotal.setText("₱" + String.format("%.2f", total));
            textTotalCheckout.setText("₱" + String.format("%.2f", total));
        } catch (NumberFormatException e) {
            textTotal.setText("₱0.00");
            textTotalCheckout.setText("₱0.00");
        }
    }

    private void showEmptyCart() {
        layoutEmptyCart.setVisibility(View.VISIBLE);
        recyclerViewCart.setVisibility(View.GONE);
        cardSummary.setVisibility(View.GONE);
    }

    private void showCartWithItems() {
        layoutEmptyCart.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.VISIBLE);
        cardSummary.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        showLoading(true);
        cartRepository.updateCartItem(item.getId(), newQuantity, new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                showLoading(false);
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
                    loadCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showError(message);
            }
        });
    }

    @Override
    public void onItemRemoved(CartItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove this item from your cart?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeItem(CartItem item) {
        showLoading(true);
        cartRepository.removeFromCart(item.getId(), new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                showLoading(false);
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                    loadCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showError(message);
            }
        });
    }

    private void confirmClearCart() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to clear all items from your cart?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    clearCart();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearCart() {
        showLoading(true);
        cartRepository.clearCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                showLoading(false);
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), "Cart cleared", Toast.LENGTH_SHORT).show();
                    loadCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                showError(message);
            }
        });
    }

    private void handleCheckout() {
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to CheckoutFragment
        if (getActivity() != null) {
            CheckoutFragment checkoutFragment = CheckoutFragment.newInstance(
                Double.parseDouble(currentTotal),
                currentCart.getItems().size()
            );

            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, checkoutFragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private void navigateToShop() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void refreshCart() {
        loadCart();
    }
}