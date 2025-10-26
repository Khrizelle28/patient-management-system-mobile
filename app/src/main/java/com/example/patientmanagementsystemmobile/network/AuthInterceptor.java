package com.example.patientmanagementsystemmobile.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Get auth token from SharedPreferences or your auth storage
        String token = getAuthToken();

        // Debug logging
        android.util.Log.d("AuthInterceptor", "URL: " + originalRequest.url());
        android.util.Log.d("AuthInterceptor", "Token: " + (token != null ? "EXISTS (length=" + token.length() + ")" : "NULL"));

        // If token exists, add it to the request
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .build();
            android.util.Log.d("AuthInterceptor", "Authorization header added");
            return chain.proceed(newRequest);
        }

        // If no token, proceed with original request
        android.util.Log.d("AuthInterceptor", "No token found, proceeding without auth");
        return chain.proceed(originalRequest);
    }

    private String getAuthToken() {
        // Get token from SharedPreferences
        // Adjust the preference name and key according to your app
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    // Static method to save token
    public static void saveAuthToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();
    }

    // Static method to clear token
    public static void clearAuthToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();
    }
}
