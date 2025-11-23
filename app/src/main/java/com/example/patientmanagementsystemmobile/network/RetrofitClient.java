package com.example.patientmanagementsystemmobile.network;

import android.content.Context;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.User;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
//    private static final String BASE_URL = "https://patient-management-system-main-2zywvz.laravel.cloud/api/";

    // Base URL for images (without /api/)
    private static final String IMAGE_BASE_URL = "http://10.0.2.2:8000";
//    private static final String IMAGE_BASE_URL = "https://patient-management-system-main-2zywvz.laravel.cloud";

    private static Retrofit retrofit;
    private static Context appContext;

    public static User currentUser = null;

    // Initialize with application context
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create OkHttpClient with AuthInterceptor
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            if (appContext != null) {
                httpClient.addInterceptor(new AuthInterceptor(appContext));
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getUserApiService() {
        return getClient().create(ApiService.class);
    }

    // Get base URL for loading images
    public static String getImageBaseUrl() {
        return IMAGE_BASE_URL;
    }

    // Build full image URL - handles both full URLs and partial paths
    public static String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        // If it's a full URL with http:// or https://
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // Replace patient-management-system.test with 10.0.2.2:8000 for local development
            if (imagePath.contains("patient-management-system.test")) {
                return imagePath.replace("http://patient-management-system.test", "http://10.0.2.2:8000");
            }
            // Otherwise use the URL as-is (for webhost URLs)
            return imagePath;
        }

        // For relative paths (like /storage/images/xxx.png), prepend the base URL
        return IMAGE_BASE_URL + imagePath;
    }

    // Call this when user logs out to force recreation of Retrofit instance
    public static void resetClient() {
        retrofit = null;
    }
}
