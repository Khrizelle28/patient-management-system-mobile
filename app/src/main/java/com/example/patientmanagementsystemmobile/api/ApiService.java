package com.example.patientmanagementsystemmobile.api;

import com.example.patientmanagementsystemmobile.response.AppointmentListResponse;
import com.example.patientmanagementsystemmobile.models.AuthResponse;
import com.example.patientmanagementsystemmobile.models.User;
import com.example.patientmanagementsystemmobile.request.AddToCartRequest;
import com.example.patientmanagementsystemmobile.request.AppointmentRequest;
import com.example.patientmanagementsystemmobile.request.PlaceOrderRequest;
import com.example.patientmanagementsystemmobile.request.UpdateCartRequest;
import com.example.patientmanagementsystemmobile.response.AppointmentResponse;
import com.example.patientmanagementsystemmobile.response.CartResponse;
import com.example.patientmanagementsystemmobile.response.OrderResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("register")
    Call<AuthResponse> register(@Body User user);

    @FormUrlEncoded
    @POST("login")
    Call<AuthResponse> login(
            @Field("username") String email,
            @Field("password") String password
    );

    @Headers("Accept: application/json")
    @GET("doctor-schedule")
    Call<Map<String, Object>> getDoctorSchedule();

    @Headers("Accept: application/json")
    @POST("appointments")
    Call<AppointmentResponse> createAppointment(@Body AppointmentRequest request);

    @Headers("Accept: application/json")
    @GET("appointments/patient/{patientId}")
    Call<AppointmentListResponse> getPatientAppointments(@Path("patientId") String patientId);

    @Headers("Accept: application/json")
    @GET("available-products")
    Call<Map<String, Object>> getAvailableProducts();

    @Headers("Accept: application/json")
    @GET("products")
    Call<com.example.patientmanagementsystemmobile.response.ProductResponse> getProducts();

    // Cart endpoints
    @Headers("Accept: application/json")
    @GET("cart")
    Call<CartResponse> getCart();

    @Headers("Accept: application/json")
    @POST("cart")
    Call<CartResponse> addToCart(@Body AddToCartRequest request);

    @Headers("Accept: application/json")
    @PUT("cart/{id}")
    Call<CartResponse> updateCartItem(@Path("id") int itemId, @Body UpdateCartRequest request);

    @Headers("Accept: application/json")
    @DELETE("cart/{id}")
    Call<CartResponse> removeFromCart(@Path("id") int itemId);

    @Headers("Accept: application/json")
    @DELETE("cart-clear")
    Call<CartResponse> clearCart();

    // Order endpoints
    @Headers("Accept: application/json")
    @POST("orders/place")
    Call<OrderResponse> placeOrder(@Body PlaceOrderRequest request);
}
