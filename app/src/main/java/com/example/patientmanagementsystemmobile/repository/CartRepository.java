package com.example.patientmanagementsystemmobile.repository;

import android.util.Log;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.request.AddToCartRequest;
import com.example.patientmanagementsystemmobile.request.UpdateCartRequest;
import com.example.patientmanagementsystemmobile.response.CartResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private ApiService apiService;

    public CartRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // Get cart
    public void getCart(CartCallback callback) {
        Call<CartResponse> call = apiService.getCart();
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load cart");
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Error loading cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Add item to cart
    public void addToCart(int productId, int quantity, CartCallback callback) {
        AddToCartRequest request = new AddToCartRequest(productId, quantity);
        Call<CartResponse> call = apiService.addToCart(request);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add item to cart");
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Error adding to cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Update cart item quantity
    public void updateCartItem(int itemId, int quantity, CartCallback callback) {
        UpdateCartRequest request = new UpdateCartRequest(quantity);
        Call<CartResponse> call = apiService.updateCartItem(itemId, request);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update cart item");
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Error updating cart item", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Remove item from cart
    public void removeFromCart(int itemId, CartCallback callback) {
        Call<CartResponse> call = apiService.removeFromCart(itemId);
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to remove item from cart");
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Error removing from cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Clear entire cart
    public void clearCart(CartCallback callback) {
        Call<CartResponse> call = apiService.clearCart();
        call.enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to clear cart");
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e(TAG, "Error clearing cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Callback interface
    public interface CartCallback {
        void onSuccess(CartResponse response);
        void onError(String message);
    }
}
