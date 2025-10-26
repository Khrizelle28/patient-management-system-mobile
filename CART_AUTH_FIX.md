# Cart Authentication Fix

## Problem
Cart endpoints require authentication (`auth:sanctum`), but mobile app isn't sending auth tokens.

Error: "Failed to add item to cart" / "Failed to load cart"

## Quick Fix (Testing Only)

### Option 1: Remove Auth Requirement (Testing Only)

**Step 1:** Update `routes/api.php`:
```php
// Remove auth:sanctum middleware
Route::get('cart', [CartController::class, 'index']);
Route::post('cart', [CartController::class, 'store']);
Route::put('cart/{id}', [CartController::class, 'update']);
Route::delete('cart/{id}', [CartController::class, 'destroy']);
Route::delete('cart-clear', [CartController::class, 'clear']);
```

**Step 2:** Modify CartController to use a test user:
```php
public function index(Request $request): JsonResponse
{
    // For testing: use first patient user
    $patientUser = \App\Models\PatientUser::first();

    // Or hardcode an ID
    // $patientUserId = 1;

    $cart = Cart::with(['items.product'])->firstOrCreate(
        ['patient_user_id' => $patientUser->id]
    );

    return response()->json([
        'success' => true,
        'data' => [
            'cart' => $cart,
            'items' => $cart->items,
            'total' => $cart->total,
            'items_count' => $cart->items->sum('quantity'),
        ],
    ]);
}

public function store(AddToCartRequest $request): JsonResponse
{
    // For testing: use first patient user
    $patientUser = \App\Models\PatientUser::first();

    $product = Product::query()->findOrFail($request->product_id);

    if ($product->stock < $request->quantity) {
        return response()->json([
            'success' => false,
            'message' => 'Insufficient stock available.',
        ], 400);
    }

    $cart = Cart::query()->firstOrCreate(['patient_user_id' => $patientUser->id]);

    // ... rest of the method stays the same
}
```

Apply same change to `update()`, `destroy()`, and `clear()` methods.

---

## Proper Solution: Add Authentication to Mobile App

### Step 1: Update RetrofitClient to Send Auth Token

**Replace your `RetrofitClient.java` with:**

```java
package com.example.patientmanagementsystemmobile.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.User;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit;
    private static Retrofit authenticatedRetrofit;
    private static Context appContext;

    public static User currentUser = null;
    public static String authToken = null;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
        loadAuthToken();
    }

    // Save auth token
    public static void saveAuthToken(Context context, String token) {
        authToken = token;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();

        // Clear authenticated retrofit to recreate with new token
        authenticatedRetrofit = null;
    }

    // Load auth token from storage
    private static void loadAuthToken() {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
            authToken = prefs.getString("auth_token", null);
        }
    }

    // Clear auth token
    public static void clearAuthToken(Context context) {
        authToken = null;
        SharedPreferences prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();
        authenticatedRetrofit = null;
    }

    // Get regular Retrofit client (no auth)
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Get authenticated Retrofit client (with auth token)
    public static Retrofit getAuthenticatedClient() {
        if (authenticatedRetrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create auth interceptor
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json");

                // Add auth token if available
                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                    android.util.Log.d("RetrofitClient", "Adding auth token: Bearer " + authToken.substring(0, Math.min(10, authToken.length())) + "...");
                } else {
                    android.util.Log.w("RetrofitClient", "No auth token available!");
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });

            clientBuilder.addInterceptor(loggingInterceptor);

            OkHttpClient client = clientBuilder.build();

            authenticatedRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authenticatedRetrofit;
    }

    public static ApiService getUserApiService() {
        return getClient().create(ApiService.class);
    }

    public static ApiService getAuthenticatedApiService() {
        return getAuthenticatedClient().create(ApiService.class);
    }
}
```

### Step 2: Update CartRepository to Use Authenticated Client

**Modify `CartRepository.java`:**

```java
public class CartRepository {
    private static final String TAG = "CartRepository";
    private ApiService apiService;

    public CartRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // OR create with authenticated service
    public static CartRepository createAuthenticated() {
        return new CartRepository(RetrofitClient.getAuthenticatedApiService());
    }

    // ... rest stays the same
}
```

### Step 3: Update CartFragment and DoctorsFragment

**In `CartFragment.java`:**
```java
private void setupRepository() {
    // Use authenticated client
    ApiService apiService = RetrofitClient.getAuthenticatedApiService();
    cartRepository = new CartRepository(apiService);
}
```

**In `DoctorsFragment.java`:**
```java
private void initializeViews(View view) {
    consultCard = view.findViewById(R.id.consultCard);
    seeAllButton = view.findViewById(R.id.seeAllButton);
    medicationRecyclerView = view.findViewById(R.id.medicationRecyclerView);
    cartIcon = view.findViewById(R.id.cartIcon);

    // Initialize cart repository with authenticated client
    ApiService apiService = RetrofitClient.getAuthenticatedApiService();
    cartRepository = new CartRepository(apiService);
}
```

### Step 4: Save Token After Login

**In your login activity/fragment (where you handle login response):**

```java
@Override
public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
    if (response.isSuccessful() && response.body() != null) {
        AuthResponse authResponse = response.body();

        // Get token from response
        String token = authResponse.getToken(); // or however your API returns it

        // Save the token
        RetrofitClient.saveAuthToken(context, token);

        // Save user info
        RetrofitClient.currentUser = authResponse.getUser();

        // Navigate to home
        startActivity(new Intent(context, HomeActivity.class));
    }
}
```

### Step 5: Initialize RetrofitClient in Application or MainActivity

**In `MainActivity.java` or create an `Application` class:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize RetrofitClient
    RetrofitClient.init(getApplicationContext());

    // ... rest of your code
}
```

### Step 6: Add OkHttp Dependencies

**In `app/build.gradle`:**
```gradle
dependencies {
    // OkHttp for interceptors
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
}
```

Then click "Sync Now" in Android Studio.

---

## Testing

### After Quick Fix (Option 1):
1. Run the app
2. Try adding products to cart
3. Should work without login

### After Proper Solution (Option 2):
1. Run the app
2. Login first
3. Check Logcat for "RetrofitClient" - should see "Adding auth token: Bearer..."
4. Try adding products to cart
5. Check Laravel logs - should see `/api/cart` requests with 200 response

---

## Debugging

Check Logcat for:
```
D/RetrofitClient: Adding auth token: Bearer abcd1234...
D/DoctorsFragment: Adding to cart - Product ID: 1, Quantity: 2
D/DoctorsFragment: Add to cart success: true
```

Or errors:
```
W/RetrofitClient: No auth token available!
E/DoctorsFragment: Add to cart error: 401 Unauthorized
```

---

## Which Option to Choose?

**Option 1 (Quick Fix):** Use if you just want to test cart functionality quickly
- Pros: Works immediately
- Cons: Not secure, uses same cart for everyone

**Option 2 (Proper Solution):** Use for production
- Pros: Secure, each user has their own cart
- Cons: Requires more setup

I recommend **Option 2** for a complete solution!
