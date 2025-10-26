# Cart Functionality Implementation

This document describes the complete cart functionality implementation for the Patient Management System Mobile app.

## Overview

The cart functionality allows users to:
- View their shopping cart
- Add products to cart
- Update item quantities
- Remove items from cart
- Clear entire cart
- View cart totals

## Files Created

### Models
- `models/Cart.java` - Cart data model
- `models/CartItem.java` - Cart item data model
- `response/CartResponse.java` - API response wrapper for cart operations
- `request/AddToCartRequest.java` - Request model for adding items to cart
- `request/UpdateCartRequest.java` - Request model for updating cart items

### API & Repository
- `api/ApiService.java` - Updated with cart endpoints
  - `GET /cart` - Get user's cart
  - `POST /cart` - Add item to cart
  - `PUT /cart/{id}` - Update cart item
  - `DELETE /cart/{id}` - Remove item from cart
  - `DELETE /cart-clear` - Clear entire cart

- `repository/CartRepository.java` - Handles all cart API interactions with callbacks

### UI Components
- `CartFragment.java` - Main cart screen fragment
- `ShopFragment.java` - Example product listing with "Add to Cart" functionality
- `adapter/CartAdapter.java` - RecyclerView adapter for cart items
- `adapter/ProductAdapter.java` - RecyclerView adapter for products

### Layouts
- `layout/fragment_cart.xml` - Cart screen layout with:
  - Header showing item count
  - RecyclerView for cart items
  - Empty cart state
  - Bottom summary card with total and action buttons

- `layout/item_cart.xml` - Individual cart item layout with:
  - Product image
  - Product name and price
  - Quantity controls (+/-)
  - Subtotal
  - Remove button

- `layout/fragment_shop.xml` - Shop/products screen layout
- `layout/item_product.xml` - Individual product item layout with "Add to Cart" button

### Navigation
- `HomeActivity.java` - Updated to navigate to CartFragment when "mycart" menu item is selected

## Usage

### 1. Viewing the Cart

Users can access their cart by tapping the "My Cart" icon in the bottom navigation bar. The cart will automatically load and display:
- List of items in cart
- Quantity and subtotal for each item
- Total cart value
- Empty state if cart has no items

### 2. Adding Products to Cart

In your product listing screen (like ShopFragment), implement the ProductAdapter.OnProductListener:

```java
@Override
public void onAddToCart(Product product) {
    cartRepository.addToCart(product.getId(), 1, new CartRepository.CartCallback() {
        @Override
        public void onSuccess(CartResponse response) {
            Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 3. Updating Cart Items

The CartFragment handles quantity updates automatically when users tap the +/- buttons:

```java
@Override
public void onQuantityChanged(CartItem item, int newQuantity) {
    cartRepository.updateCartItem(item.getId(), newQuantity, callback);
}
```

### 4. Removing Items

Users can remove individual items by clicking the "Remove" button. A confirmation dialog appears before deletion.

### 5. Clearing Cart

Users can clear their entire cart by clicking the "Clear Cart" button. A confirmation dialog appears before clearing.

### 6. Checkout

The checkout button is ready for implementation. Currently shows a "coming soon" message.

## API Authentication

**IMPORTANT**: The cart endpoints require authentication via Laravel Sanctum. Make sure your RetrofitClient includes the authentication token in requests:

```java
// Add this to RetrofitClient or create an AuthInterceptor
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer " + getAuthToken())
            .header("Accept", "application/json");
        Request request = requestBuilder.build();
        return chain.proceed(request);
    })
    .build();

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

## Backend Requirements

The Laravel backend must be running with the following:
1. Sanctum authentication configured
2. Cart migrations run
3. CartController with all methods implemented
4. Routes defined in api.php under auth:sanctum middleware

## Testing

To test the cart functionality:

1. Ensure you're logged in (have a valid Sanctum token)
2. Navigate to the shop/products screen
3. Click "Add to Cart" on any product
4. Navigate to "My Cart" from bottom navigation
5. Test:
   - Increasing/decreasing quantities
   - Removing items
   - Clearing cart
   - Viewing updated totals

## Error Handling

The implementation includes error handling for:
- Network failures
- API errors (insufficient stock, etc.)
- Empty cart states
- Stock validation

All errors are displayed as Toast messages to the user.

## Future Enhancements

Consider adding:
1. Checkout flow integration
2. Payment processing
3. Order history
4. Product search and filtering
5. Product image loading with Glide or Picasso
6. Cart badge showing item count on navigation icon
7. Product details screen
8. Wish list functionality
9. Quantity input field (instead of just +/- buttons)
10. Stock availability indicators

## Dependencies

Make sure your build.gradle includes:

```gradle
dependencies {
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'

    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.0'

    // CardView
    implementation 'androidx.cardview:cardview:1.0.0'

    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

    // Optional: Image loading
    implementation 'com.github.bumptech.glide:glide:4.15.1'
}
```

## Notes

- Cart data is stored on the server, not locally
- Cart persists across app sessions as long as user is authenticated
- Stock validation is handled by the backend
- All cart operations require authentication
