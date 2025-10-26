# RetrofitClient Authentication Update Guide

To enable authentication for cart API calls, you need to update your `RetrofitClient.java` to include the `AuthInterceptor`.

## Option 1: Update existing RetrofitClient.java

Replace the current `RetrofitClient.java` with this version that includes OkHttp interceptor:

```java
package com.example.patientmanagementsystemmobile.network;

import android.content.Context;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.User;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit;
    private static Context applicationContext;

    public static User currentUser = null;

    // Initialize with application context
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with interceptors
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            // Add auth interceptor if context is available
            if (applicationContext != null) {
                clientBuilder.addInterceptor(new AuthInterceptor(applicationContext));
            }

            // Add logging interceptor for debugging
            clientBuilder.addInterceptor(loggingInterceptor);

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getUserApiService() {
        return getClient().create(ApiService.class);
    }
}
```

## Option 2: Create Authenticated Retrofit Client

If you want to keep the existing RetrofitClient unchanged, create a new `AuthenticatedRetrofitClient.java`:

```java
package com.example.patientmanagementsystemmobile.network;

import android.content.Context;

import com.example.patientmanagementsystemmobile.api.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthenticatedRetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }
}
```

Then use it in CartRepository and CartFragment:
```java
// In CartRepository
ApiService apiService = AuthenticatedRetrofitClient.getApiService(context);

// Or in CartFragment
private void setupRepository() {
    ApiService apiService = AuthenticatedRetrofitClient.getApiService(requireContext());
    cartRepository = new CartRepository(apiService);
}
```

## Required Dependencies

Add these to your `app/build.gradle`:

```gradle
dependencies {
    // Existing dependencies...

    // OkHttp for interceptors
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
}
```

## Usage in Login

After successful login, save the token:

```java
// In your login activity/fragment after successful authentication
@Override
public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
    if (response.isSuccessful() && response.body() != null) {
        AuthResponse authResponse = response.body();
        String token = authResponse.getToken(); // Adjust based on your API response

        // Save the token
        AuthInterceptor.saveAuthToken(context, token);

        // Save user info
        RetrofitClient.currentUser = authResponse.getUser();

        // Navigate to home
        startActivity(new Intent(context, HomeActivity.class));
    }
}
```

## Usage in Logout

Clear the token when user logs out:

```java
// In your logout method
AuthInterceptor.clearAuthToken(context);
RetrofitClient.currentUser = null;
```

## Testing Authentication

To verify authentication is working:

1. Check Logcat for OkHttp logs showing the Authorization header:
   ```
   Authorization: Bearer your-token-here
   ```

2. If you see 401 Unauthorized errors:
   - Token may be expired
   - Token may not be saved correctly
   - Backend may not be recognizing the token

3. Common issues:
   - Token not saved after login
   - Context not initialized in RetrofitClient
   - Backend not configured for Sanctum auth
   - Token format incorrect (should be "Bearer token")

## Initialize RetrofitClient (Option 1 only)

If using Option 1, initialize RetrofitClient in your Application class or MainActivity:

```java
// In Application class onCreate() or MainActivity onCreate()
RetrofitClient.init(getApplicationContext());
```

## Note

The AuthInterceptor automatically adds the authorization token to all API requests. Make sure your Laravel backend is configured to accept Bearer token authentication via Sanctum.
