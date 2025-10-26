# DoctorsFragment Cart Integration

## Overview

The DoctorsFragment (medication/shop screen) has been successfully integrated with the cart functionality. Users can now browse medications and add them to their cart directly from this screen.

## What Was Updated

### 1. DoctorsFragment.java
**Changes:**
- Added `CartRepository` for cart operations
- Added `productList` to store actual product data with IDs
- Updated `Medication` class to include product ID
- Added `addToCart()` method for adding products to cart
- Added `navigateToCart()` method to navigate to cart screen
- Updated cart icon click listener to navigate to CartFragment
- Updated medication adapter to handle "Add to Cart" button clicks

### 2. item_medication.xml
**Changes:**
- Added stock display (`TextView medicationStock`)
- Added "Add to Cart" button (`Button addToCartButton`)
- Reorganized layout to show both "Details" and "Add to Cart" buttons side by side
- Improved button styling

## Features

### 1. Product Display
- Shows medication name, price, description
- Displays stock availability
- Shows product emoji/icon

### 2. Add to Cart
- Click "Add to Cart" button on any medication
- Automatically adds 1 quantity to cart
- Shows success/error toast message
- Button disabled when product is out of stock
- Button shows "Out of Stock" when quantity is 0

### 3. Cart Navigation
- Click the cart icon (🛒) at the top of the screen
- Navigates to CartFragment
- Can navigate back using back button

### 4. Stock Validation
- Checks stock before enabling "Add to Cart" button
- Disables button and shows "Out of Stock" for unavailable items
- Backend validates stock when adding to cart

## User Flow

1. **Browse Medications**
   - User opens DoctorsFragment (medication/shop tab)
   - Sees grid of available medications
   - Views name, price, description, and stock

2. **Add to Cart**
   - User clicks "Add to Cart" on desired medication
   - Product is added to cart with quantity 1
   - Toast message confirms addition
   - If stock is insufficient, error message is shown

3. **View Cart**
   - User clicks cart icon (🛒) at top
   - Navigates to CartFragment
   - Sees all items in cart
   - Can update quantities or remove items

4. **Continue Shopping**
   - User can press back button to return to medication list
   - Or use bottom navigation to go to other screens

## API Integration

All cart operations use the existing Laravel backend endpoints:
- `POST /api/cart` - Add medication to cart
- `GET /api/cart` - View cart (in CartFragment)
- `PUT /api/cart/{id}` - Update quantity (in CartFragment)
- `DELETE /api/cart/{id}` - Remove item (in CartFragment)

## Navigation Structure

```
Bottom Navigation (HomeActivity)
├── Home (HomeFragment)
├── My Cart (CartFragment) ← Bottom nav
├── Appointment (AppointmentFragment)
├── Rx Alert (RxAlertFragment)
└── Profile (ProfileFragment)

DoctorsFragment (Medication/Shop)
├── Cart Icon (🛒) ← Navigates to CartFragment
└── Medications Grid
    └── Add to Cart Button ← Adds to cart
```

## Key Code Sections

### Adding to Cart (DoctorsFragment.java:191-207)
```java
private void addToCart(int productId, String productName) {
    cartRepository.addToCart(productId, 1, new CartRepository.CartCallback() {
        @Override
        public void onSuccess(CartResponse response) {
            if (response.isSuccess()) {
                Toast.makeText(getContext(), productName + " added to cart!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
        }
    });
}
```

### Navigating to Cart (DoctorsFragment.java:181-189)
```java
private void navigateToCart() {
    if (getActivity() != null) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, new CartFragment())
                .addToBackStack(null)
                .commit();
    }
}
```

### Stock Validation (DoctorsFragment.java:304-314)
```java
// Disable "Add to Cart" button if out of stock
if (medication.getStock() <= 0) {
    addToCartButton.setEnabled(false);
    addToCartButton.setText("Out of Stock");
    addToCartButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.GRAY));
} else {
    addToCartButton.setEnabled(true);
    addToCartButton.setText("Add to Cart");
}
```

## Testing

### Test Scenarios

1. **Add Medication to Cart**
   - ✅ Open DoctorsFragment
   - ✅ Click "Add to Cart" on any medication
   - ✅ See success toast message
   - ✅ Click cart icon to verify item is in cart

2. **Out of Stock Handling**
   - ✅ Find medication with 0 stock
   - ✅ Verify "Add to Cart" button is disabled
   - ✅ Verify button shows "Out of Stock"

3. **Cart Navigation**
   - ✅ Click cart icon (🛒)
   - ✅ Verify CartFragment opens
   - ✅ Verify items are displayed
   - ✅ Press back button to return to medications

4. **Multiple Items**
   - ✅ Add multiple different medications
   - ✅ Click cart icon
   - ✅ Verify all items appear in cart

5. **Error Handling**
   - ✅ Add item exceeding stock
   - ✅ Verify error message is displayed
   - ✅ Add item when not authenticated
   - ✅ Verify appropriate error message

## UI Screenshots Flow

```
┌─────────────────────────┐
│  DoctorsFragment        │
│  (Medication/Shop)      │
│                         │
│  [Search Bar]   [🛒]   │ ← Cart Icon
│                         │
│  ┌───────┐  ┌───────┐  │
│  │ 💊    │  │ 💊    │  │
│  │Amoxil │  │Aspirin│  │
│  │$19.99 │  │$9.99  │  │
│  │Stock:5│  │Stock:0│  │
│  │[Details] [Add]│    │  │
│  └───────┘  └───────┘  │
│                         │
└─────────────────────────┘
         │
         │ Click "Add to Cart"
         ▼
┌─────────────────────────┐
│  Toast Message          │
│  "Amoxil added to cart!"│
└─────────────────────────┘
         │
         │ Click Cart Icon (🛒)
         ▼
┌─────────────────────────┐
│  CartFragment           │
│                         │
│  My Cart (1 item)       │
│                         │
│  ┌───────────────────┐  │
│  │ Amoxil    $19.99  │  │
│  │ [-] 1 [+]         │  │
│  │ Subtotal: $19.99  │  │
│  │ [Remove]          │  │
│  └───────────────────┘  │
│                         │
│  Total: $19.99          │
│  [Clear Cart] [Checkout]│
└─────────────────────────┘
```

## Important Notes

1. **Authentication Required**: Cart operations require user authentication via Sanctum
2. **Stock Validation**: Backend validates stock availability on add/update
3. **Single Quantity**: Each "Add to Cart" click adds quantity 1 (can be adjusted in cart)
4. **Real-time Updates**: Cart totals and item counts update automatically
5. **Error Handling**: All API errors are caught and displayed to user

## Future Enhancements

Consider adding:
- [ ] Quantity selector on product card (not just default 1)
- [ ] Cart badge showing item count on cart icon
- [ ] "Added to cart" animation
- [ ] Quick view product details dialog
- [ ] Product search/filter in medications list
- [ ] Category filters for medications
- [ ] Favorite/wishlist functionality
- [ ] Product image loading (currently using emoji)
- [ ] "Recently Added" section

## Related Files

- `DoctorsFragment.java` - Main medication/shop screen
- `CartFragment.java` - Cart viewing and management
- `CartRepository.java` - Cart API operations
- `item_medication.xml` - Medication card layout
- `fragment_doctors.xml` - Medication screen layout
- `CartController.php` - Backend cart controller (Laravel)
