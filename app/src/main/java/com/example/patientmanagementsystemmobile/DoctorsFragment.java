package com.example.patientmanagementsystemmobile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Product;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.repository.CartRepository;
import com.example.patientmanagementsystemmobile.response.CartResponse;
import com.example.patientmanagementsystemmobile.response.ProductResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DoctorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DoctorsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView medicationRecyclerView;
    private MedicationAdapter medicationAdapter;
    private List<Medication> medicationList;
    private List<Product> productList;
    private CardView consultCard;
    private TextView cartIcon;
    private TextView textGreeting;
    private CartRepository cartRepository;

    public DoctorsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this Fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DoctorsFragment.
     */
    public static DoctorsFragment newInstance(String param1, String param2) {
        DoctorsFragment fragment = new DoctorsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.util.Log.d("DoctorsFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_doctors, container, false);

        try {
            initializeViews(view);
            setupMedicationData();
            setupRecyclerView();
            setupClickListeners();
            android.util.Log.d("DoctorsFragment", "Fragment setup completed successfully");
        } catch (Exception e) {
            android.util.Log.e("DoctorsFragment", "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error loading fragment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void initializeViews(View view) {
        consultCard = view.findViewById(R.id.consultCard);
        medicationRecyclerView = view.findViewById(R.id.medicationRecyclerView);
        cartIcon = view.findViewById(R.id.cartIcon);
        textGreeting = view.findViewById(R.id.textGreeting);

        // Set patient's name in greeting
        if (RetrofitClient.currentUser != null) {
            String fullName = RetrofitClient.currentUser.getFullName();
            textGreeting.setText("Hello " + fullName + "!");
        }

        // Initialize cart repository
        ApiService apiService = RetrofitClient.getUserApiService();
        cartRepository = new CartRepository(apiService);
    }

    private void setupMedicationData() {
        android.util.Log.d("DoctorsFragment", "setupMedicationData called");
        try {
            medicationList = new ArrayList<>();
            productList = new ArrayList<>();
            android.util.Log.d("DoctorsFragment", "Lists initialized, calling fetchProductsFromApi");
            fetchProductsFromApi();
        } catch (Exception e) {
            android.util.Log.e("DoctorsFragment", "Error in setupMedicationData", e);
            Toast.makeText(getContext(), "Error setting up medications: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchProductsFromApi() {
        try {
            android.util.Log.d("DoctorsFragment", "Creating API service...");
            ApiService apiService = RetrofitClient.getUserApiService();
            android.util.Log.d("DoctorsFragment", "API service created, calling getProducts...");
            Call<ProductResponse> call = apiService.getProducts();
            android.util.Log.d("DoctorsFragment", "Call object created, enqueueing request...");

            call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                android.util.Log.d("DoctorsFragment", "Response received. Success: " + response.isSuccessful() + ", Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getData();
                    android.util.Log.d("DoctorsFragment", "Products received: " + (products != null ? products.size() : "null"));

                    if (products != null && !products.isEmpty()) {
                        medicationList.clear();
                        productList.clear();
                        productList.addAll(products); // Store actual products

                        for (Product product : products) {
                            android.util.Log.d("DoctorsFragment", "Adding product: " + product.getName());
                            medicationList.add(new Medication(
                                    product.getId(),
                                    product.getName(),
                                    "₱" + product.getPrice(),
                                    product.getDescription(),
                                    "💊", // Default emoji
                                    product.getStock(),
                                    product.getExpirationDate(),
                                    product.getImage()
                            ));
                        }
                        medicationAdapter.notifyDataSetChanged();
                        android.util.Log.d("DoctorsFragment", "Adapter notified. Total items: " + medicationList.size());
                        Toast.makeText(getContext(), "Loaded " + products.size() + " products", Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.w("DoctorsFragment", "Product list is null or empty");
                        Toast.makeText(getContext(), "No products available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Failed to load products. Code: " + response.code();
                    android.util.Log.e("DoctorsFragment", errorMsg);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("DoctorsFragment", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                android.util.Log.e("DoctorsFragment", errorMsg, t);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
        } catch (Exception e) {
            android.util.Log.e("DoctorsFragment", "Exception in fetchProductsFromApi", e);
            Toast.makeText(getContext(), "Error creating API call: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        medicationAdapter = new MedicationAdapter(medicationList);
        medicationRecyclerView.setLayoutManager(
                new GridLayoutManager(getContext(), 2)
        );
        medicationRecyclerView.setAdapter(medicationAdapter);
    }

    private void setupClickListeners() {
        consultCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening consultation...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to consultation activity or fragment
        });

        cartIcon.setOnClickListener(v -> {
            // Navigate to CartFragment
            navigateToCart();
        });
    }

    private void navigateToCart() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new CartFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void addToCart(int productId, String productName, int maxStock) {
        // Show quantity selection dialog
        showQuantityDialog(productId, productName, maxStock);
    }

    private void showQuantityDialog(int productId, String productName, int maxStock) {
        if (getContext() == null) return;

        // Create dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity, null);
        builder.setView(dialogView);

        // Find views
        TextView textProductName = dialogView.findViewById(R.id.textProductName);
        EditText textQuantity = dialogView.findViewById(R.id.textQuantity);
        android.widget.ImageButton btnDecrease = dialogView.findViewById(R.id.btnDecrease);
        android.widget.ImageButton btnIncrease = dialogView.findViewById(R.id.btnIncrease);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        textProductName.setText(productName);
        final int[] quantity = {1}; // Start with quantity 1
        textQuantity.setText(String.valueOf(quantity[0]));

        android.app.AlertDialog dialog = builder.create();

        // Handle manual quantity input
        textQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String input = s.toString().trim();
                    if (!input.isEmpty()) {
                        int newQuantity = Integer.parseInt(input);
                        if (newQuantity > maxStock) {
                            textQuantity.setText(String.valueOf(maxStock));
                            textQuantity.setSelection(textQuantity.getText().length());
                            Toast.makeText(getContext(), "Maximum stock: " + maxStock, Toast.LENGTH_SHORT).show();
                        } else if (newQuantity > 0) {
                            quantity[0] = newQuantity;
                        } else {
                            textQuantity.setText("1");
                            textQuantity.setSelection(textQuantity.getText().length());
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

        // Decrease quantity
        btnDecrease.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                textQuantity.setText(String.valueOf(quantity[0]));
            }
        });

        // Increase quantity
        btnIncrease.setOnClickListener(v -> {
            if (quantity[0] < maxStock) {
                quantity[0]++;
                textQuantity.setText(String.valueOf(quantity[0]));
            } else {
                Toast.makeText(getContext(), "Maximum stock: " + maxStock, Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Confirm and add to cart
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            addToCartConfirmed(productId, productName, quantity[0]);
        });

        dialog.show();
    }

    private void addToCartConfirmed(int productId, String productName, int quantity) {
        android.util.Log.d("DoctorsFragment", "Adding to cart - Product ID: " + productId + ", Quantity: " + quantity);

        cartRepository.addToCart(productId, quantity, new CartRepository.CartCallback() {
            @Override
            public void onSuccess(CartResponse response) {
                android.util.Log.d("DoctorsFragment", "Add to cart success: " + response.isSuccess());
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), quantity + "x " + productName + " added to cart!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = response.getMessage() != null ? response.getMessage() : "Failed to add to cart";
                    android.util.Log.e("DoctorsFragment", "Server error: " + errorMsg);
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("DoctorsFragment", "Add to cart error: " + message);
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Medication data class
    public static class Medication {
        private int id;
        private String name;
        private String price;
        private String description;
        private String emoji;
        private int stock;
        private String expirationDate;
        private String image;

        public Medication(String name, String price, String description, String emoji) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.emoji = emoji;
        }

        public Medication(int id, String name, String price, String description, String emoji, int stock, String expirationDate, String image) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.description = description;
            this.emoji = emoji;
            this.stock = stock;
            this.expirationDate = expirationDate;
            this.image = image;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
        public int getStock() { return stock; }
        public String getExpirationDate() { return expirationDate; }
        public String getImage() { return image; }
    }

    // RecyclerView Adapter
    private class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
        private List<Medication> medications;

        public MedicationAdapter(List<Medication> medications) {
            this.medications = medications;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_medication, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Medication medication = medications.get(position);
            holder.bind(medication);
        }

        @Override
        public int getItemCount() {
            return medications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private CardView medicationCard;
            private ImageView medicationImage;
            private TextView medicationName;
            private TextView medicationPrice;
            private TextView medicationDescription;
            private TextView medicationStock;
            private Button addToCartButton;

            public ViewHolder(View itemView) {
                super(itemView);
                medicationCard = itemView.findViewById(R.id.medicationCard);
                medicationImage = itemView.findViewById(R.id.medicationImage);
                medicationName = itemView.findViewById(R.id.medicationName);
                medicationPrice = itemView.findViewById(R.id.medicationPrice);
                medicationDescription = itemView.findViewById(R.id.medicationDescription);
                medicationStock = itemView.findViewById(R.id.medicationStock);
                addToCartButton = itemView.findViewById(R.id.addToCartButton);
            }

            public void bind(Medication medication) {
                medicationName.setText(medication.getName());
                medicationPrice.setText(medication.getPrice());
                medicationDescription.setText(medication.getDescription());
                medicationStock.setText("Stock: " + medication.getStock());

                // Load product image
                if (medication.getImage() != null && !medication.getImage().isEmpty()) {
                    String imageUrl = RetrofitClient.getFullImageUrl(medication.getImage());
                    android.util.Log.d("DoctorsFragment", "Image path from API: " + medication.getImage());
                    android.util.Log.d("DoctorsFragment", "Full image URL: " + imageUrl);

                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(medicationImage);
                } else {
                    android.util.Log.d("DoctorsFragment", "No image for: " + medication.getName());
                    medicationImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }

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

                addToCartButton.setOnClickListener(v -> {
                    // Add to cart with stock info
                    addToCart(medication.getId(), medication.getName(), medication.getStock());
                });

                medicationCard.setOnClickListener(v -> {
                    Toast.makeText(itemView.getContext(),
                            medication.getName() + " - " + medication.getPrice(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}