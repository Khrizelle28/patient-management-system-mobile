package com.example.patientmanagementsystemmobile.api;

import com.example.patientmanagementsystemmobile.models.AuthResponse;
import com.example.patientmanagementsystemmobile.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("register")
    Call<AuthResponse> register(@Body User user);

    @FormUrlEncoded
    @POST("login")
    Call<AuthResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );
}
