# Cart Functionality - Quick Start Guide

## What Was Created

A complete shopping cart system for your Patient Management mobile app that works with your existing Laravel backend.

## Files Created (17 total)

### Core Models (5 files)
- `models/Cart.java` - Cart data structure
- `models/CartItem.java` - Cart item data structure
- `response/CartResponse.java` - API response wrapper
- `request/AddToCartRequest.java` - Add to cart request
- `request/UpdateCartRequest.java` - Update cart request

### Business Logic (2 files)
- `repository/CartRepository.java` - Handles all cart API calls
- `network/AuthInterceptor.java` - Adds authentication to API requests

### UI Components (4 files)
- `CartFragment.java` - Main cart screen
- `ShopFragment.java` - Example product listing with "Add to Cart"
- `adapter/CartAdapter.java` - Cart items list adapter
- `adapter/ProductAdapter.java` - Products list adapter

### Layouts (4 files)
- `layout/fragment_cart.xml` - Cart screen UI
- `layout/item_cart.xml` - Individual cart item UI
- `layout/fragment_shop.xml` - Shop/products screen UI
- `layout/item_product.xml` - Individual product item UI

### Updated Files (2 files)
- `api/ApiService.java` - Added cart endpoints
- `HomeActivity.java` - Connected cart to navigation

## Quick Implementation Steps

### Step 1: Add Dependencies to build.gradle

```gradle
dependencies {
    // OkHttp for authentication
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

    // Material Design (if not already added)
    implementation 'com.google.android.material:material:1.9.0'
}
```

### Step 2: Update RetrofitClient for Authentication

Follow the guide in `RETROFIT_UPDATE_GUIDE.md` to add authentication support.

**Quick version:**
Add the AuthInterceptor to your RetrofitClient's OkHttp client.

### Step 3: Test the Cart

1. Run your Laravel backend (make sure it's running on `http://localhost:8000`)
2. Log in to the app
3. Tap the "My Cart" icon in bottom navigation
4. Cart screen should load (will be empty initially)

### Step 4: Add Products to Cart

Use the ShopFragment as an example:
```java
// In your product listing screen
cartRepository.addToCart(productId, quantity, new CartRepository.CartCallback() {
    @Override
    public void onSuccess(CartResponse response) {
        Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
});
```

## Key Features

- **View Cart**: See all items, quantities, and total
- **Update Quantity**: Use +/- buttons to adjust quantities
- **Remove Items**: Individual item removal with confirmation
- **Clear Cart**: Clear all items at once with confirmation
- **Stock Validation**: Backend validates stock availability
- **Empty State**: Friendly empty cart message
- **Real-time Totals**: Automatic total calculation

## Navigation

The cart is already connected to the bottom navigation:
- Tap the "My Cart" icon (R.id.mycart)
- Takes you to CartFragment

## Backend Requirements

Your Laravel backend should have:
1. ✅ Routes defined in `api.php` (you already have these)
2. ✅ CartController implemented (you already have this)
3. ⚠️ Make sure Sanctum authentication is working
4. ⚠️ Make sure cart migrations are run

## Testing Checklist

- [ ] Build the project (should compile without errors)
- [ ] Login to the app
- [ ] Navigate to "My Cart"
- [ ] See empty cart state
- [ ] Add a product (from products screen)
- [ ] See item in cart
- [ ] Update quantity with +/- buttons
- [ ] Remove an item
- [ ] Clear entire cart

## Common Issues

### 401 Unauthorized Error
**Solution:** Make sure authentication token is being saved and sent with requests. Check RETROFIT_UPDATE_GUIDE.md

### Network Error
**Solution:**
- Ensure Laravel backend is running
- Check the BASE_URL in RetrofitClient.java (should be `http://10.0.2.2:8000/api/` for emulator)

### Cart not loading
**Solution:**
- Check Logcat for error messages
- Verify user is logged in
- Ensure backend cart table exists

### Can't add to cart
**Solution:**
- Check product stock in database
- Verify product_id is correct
- Check API logs on Laravel side

## Next Steps

1. **Checkout Flow**: Implement order creation from cart
2. **Payment Integration**: Add payment processing
3. **Image Loading**: Use Glide/Picasso for product images
4. **Cart Badge**: Show cart item count on navigation icon
5. **Product Details**: Create detailed product view screen

## Support Files

- `CART_IMPLEMENTATION.md` - Full documentation
- `RETROFIT_UPDATE_GUIDE.md` - Authentication setup guide
- `CART_QUICK_START.md` - This file

## API Endpoints Used

All endpoints require authentication (`auth:sanctum` middleware):

- `GET /api/cart` - Get cart
- `POST /api/cart` - Add to cart
- `PUT /api/cart/{id}` - Update cart item
- `DELETE /api/cart/{id}` - Remove from cart
- `DELETE /api/cart-clear` - Clear cart

## Questions?

Check the detailed documentation in `CART_IMPLEMENTATION.md` for more information about each component and how to use them.
