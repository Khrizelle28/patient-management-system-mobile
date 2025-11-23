package com.example.patientmanagementsystemmobile.network;

import android.content.Context;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.User;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

//    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static final String BASE_URL = "https://patient-management-system-main-2zywvz.laravel.cloud/api/";
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

    // Call this when user logs out to force recreation of Retrofit instance
    public static void resetClient() {
        retrofit = null;
    }
}
