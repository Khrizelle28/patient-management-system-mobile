# Troubleshooting: Products Not Showing in DoctorsFragment

## Issue
Products are not displaying in the DoctorsFragment (medication/shop screen).

## Debug Steps Added

I've added detailed logging to help identify the issue. The logs will show:
- When API call starts
- Response success status and code
- Number of products received
- Each product being added
- Any errors that occur

## How to Check Logs

1. **Open Logcat** in Android Studio
2. **Filter by "DoctorsFragment"**
3. **Run the app** and navigate to the DoctorsFragment
4. **Look for these log messages:**

```
D/DoctorsFragment: Fetching products from API...
D/DoctorsFragment: Response received. Success: true, Code: 200
D/DoctorsFragment: Products received: 5
D/DoctorsFragment: Adding product: Amoxicillin
...
D/DoctorsFragment: Adapter notified. Total items: 5
```

## Common Issues and Solutions

### 1. Network Error / Connection Failed

**Symptoms:**
- Toast: "Network error: Failed to connect..."
- Log: Network error

**Solutions:**
- ✅ **Check backend is running** - Make sure Laravel server is running on `http://localhost:8000`
- ✅ **Check emulator network** - Use `http://10.0.2.2:8000/api/` for Android emulator
- ✅ **Check device network** - If using physical device, use your computer's IP address

**Test backend manually:**
```bash
# In browser or Postman
http://localhost:8000/api/products
```

### 2. 404 Not Found Error

**Symptoms:**
- Toast: "Failed to load products. Code: 404"
- Log: Response Code: 404

**Solutions:**
- ✅ **Check route exists** in Laravel `routes/api.php`:
  ```php
  Route::get('products', [ProductController::class, 'index']);
  ```
- ✅ **Clear Laravel route cache**:
  ```bash
  php artisan route:clear
  php artisan route:cache
  ```
- ✅ **Verify ProductController** has `index()` method

### 3. 401 Unauthorized Error

**Symptoms:**
- Toast: "Failed to load products. Code: 401"
- Log: Response Code: 401

**Solutions:**
- ✅ **Check if products endpoint requires auth** - If it doesn't need auth, remove `auth:sanctum` middleware:
  ```php
  // In routes/api.php
  Route::get('products', [ProductController::class, 'index']); // No auth
  ```
- ✅ **If auth is required**, make sure you're logged in and token is saved
- ✅ **Update RetrofitClient** to include AuthInterceptor (see RETROFIT_UPDATE_GUIDE.md)

### 4. 500 Internal Server Error

**Symptoms:**
- Toast: "Failed to load products. Code: 500"
- Log: Response Code: 500

**Solutions:**
- ✅ **Check Laravel logs** at `storage/logs/laravel.log`
- ✅ **Check database connection** - Make sure products table exists
- ✅ **Run migrations**:
  ```bash
  php artisan migrate
  ```
- ✅ **Seed products** if table is empty:
  ```bash
  php artisan db:seed --class=ProductSeeder
  ```

### 5. No Products Available

**Symptoms:**
- Toast: "No products available"
- Log: "Product list is null or empty"
- Response is successful (200) but no data

**Solutions:**
- ✅ **Check database** - Make sure products table has data:
  ```sql
  SELECT * FROM products;
  ```
- ✅ **Add test products**:
  ```sql
  INSERT INTO products (name, description, price, stock, created_at, updated_at)
  VALUES ('Amoxicillin', 'Antibiotic medication', '19.99', 10, NOW(), NOW());
  ```
- ✅ **Check ProductController** returns correct format:
  ```php
  public function index() {
      $products = Product::all();
      return response()->json(['data' => $products]);
  }
  ```

### 6. RecyclerView Not Visible

**Symptoms:**
- No error messages
- Products load successfully (see in logs)
- But RecyclerView is not visible

**Solutions:**
- ✅ **Check fragment_doctors.xml** - Make sure RecyclerView has proper height:
  ```xml
  <androidx.recyclerview.widget.RecyclerView
      android:id="@+id/medicationRecyclerView"
      android:layout_width="match_parent"
      android:layout_height="wrap_content" />
  ```
- ✅ **Check ScrollView height** - The parent ScrollView might not be scrollable
- ✅ **Check item layout** - Verify `item_medication.xml` exists and is correct

## Quick Diagnostics Checklist

Run through this checklist:

- [ ] Laravel backend is running (`php artisan serve`)
- [ ] Products exist in database (`SELECT * FROM products;`)
- [ ] Products endpoint works in browser (`http://localhost:8000/api/products`)
- [ ] Android app can connect to backend (other API calls work)
- [ ] Logcat shows "Fetching products from API..."
- [ ] Logcat shows successful response (Code: 200)
- [ ] Logcat shows products received > 0
- [ ] Toast message appears with product count
- [ ] RecyclerView is visible in layout

## Backend API Response Format

Your ProductController should return data in this format:

```json
{
  "data": [
    {
      "id": 1,
      "name": "Amoxicillin",
      "description": "Antibiotic medication",
      "price": "19.99",
      "stock": 10,
      "image": null,
      "expiration_date": "2025-12-31",
      "created_at": "2025-01-01T00:00:00.000000Z",
      "updated_at": "2025-01-01T00:00:00.000000Z"
    }
  ]
}
```

## Test API Manually

### Using curl:
```bash
curl http://localhost:8000/api/products
```

### Using Postman:
1. GET `http://localhost:8000/api/products`
2. Headers: `Accept: application/json`
3. Should return JSON with products array

## Still Not Working?

After checking all the above, gather this information:

1. **Logcat output** - Filter by "DoctorsFragment"
2. **API response** - What does `http://localhost:8000/api/products` return?
3. **Laravel logs** - Check `storage/logs/laravel.log`
4. **Error messages** - Any toast messages or errors shown?

Then check:
- Is the RecyclerView actually visible on screen?
- Are there any build errors in Android Studio?
- Is the fragment being loaded correctly?

## Next Steps After Fixing

Once products are displaying:
1. Remove or reduce the debug logs (they're verbose)
2. Test "Add to Cart" functionality
3. Test cart icon navigation
4. Verify stock validation works
5. Test with products that have 0 stock

## Contact for Help

If still stuck, provide:
- Complete Logcat output
- API response from browser/Postman
- Laravel log errors (if any)
- Screenshots of what you see
