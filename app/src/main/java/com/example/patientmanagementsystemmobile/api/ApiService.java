package com.example.patientmanagementsystemmobile.api;

import com.example.patientmanagementsystemmobile.models.AuthResponse;
import com.example.patientmanagementsystemmobile.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register")
    Call<AuthResponse> register(@Body User user);

    @POST("login")
    Call<AuthResponse> login(@Body User user);
}
