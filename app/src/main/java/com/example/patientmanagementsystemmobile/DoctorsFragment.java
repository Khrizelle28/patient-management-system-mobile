package com.example.patientmanagementsystemmobile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Product;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
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
    private CardView consultCard;
    private Button seeAllButton;
    private TextView cartIcon;

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
        View view = inflater.inflate(R.layout.fragment_doctors, container, false);

        initializeViews(view);
        setupMedicationData();
        setupRecyclerView();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        consultCard = view.findViewById(R.id.consultCard);
        seeAllButton = view.findViewById(R.id.seeAllButton);
        medicationRecyclerView = view.findViewById(R.id.medicationRecyclerView);
        cartIcon = view.findViewById(R.id.cartIcon);
    }

    private void setupMedicationData() {
        medicationList = new ArrayList<>();
        fetchProductsFromApi();
    }

    private void fetchProductsFromApi() {
        ApiService apiService = RetrofitClient.getUserApiService();
        Call<ProductResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getData();
                    if (products != null && !products.isEmpty()) {
                        medicationList.clear();
                        for (Product product : products) {
                            medicationList.add(new Medication(
                                    product.getName(),
                                    "$" + product.getPrice(),
                                    product.getDescription(),
                                    "💊", // Default emoji
                                    product.getStock(),
                                    product.getExpirationDate(),
                                    product.getImage()
                            ));
                        }
                        medicationAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        seeAllButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Showing all medications...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to all medications activity or expand list
        });

        cartIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening cart...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to cart/shop fragment
        });
    }

    // Medication data class
    public static class Medication {
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

        public Medication(String name, String price, String description, String emoji, int stock, String expirationDate, String image) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.emoji = emoji;
            this.stock = stock;
            this.expirationDate = expirationDate;
            this.image = image;
        }

        // Getters
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
            private TextView medicationImage;
            private TextView medicationName;
            private TextView medicationPrice;
            private TextView medicationDescription;
            private Button readMoreButton;

            public ViewHolder(View itemView) {
                super(itemView);
                medicationCard = itemView.findViewById(R.id.medicationCard);
                medicationImage = itemView.findViewById(R.id.medicationImage);
                medicationName = itemView.findViewById(R.id.medicationName);
                medicationPrice = itemView.findViewById(R.id.medicationPrice);
                medicationDescription = itemView.findViewById(R.id.medicationDescription);
                readMoreButton = itemView.findViewById(R.id.readMoreButton);
            }

            public void bind(Medication medication) {
                medicationName.setText(medication.getName());
                medicationPrice.setText(medication.getPrice());
                medicationDescription.setText(medication.getDescription());
                medicationImage.setText(medication.getEmoji());

                readMoreButton.setOnClickListener(v -> {
                    Toast.makeText(itemView.getContext(),
                            "More info about " + medication.getName(),
                            Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to medication details
                });

                medicationCard.setOnClickListener(v -> {
                    Toast.makeText(itemView.getContext(),
                            medication.getName() + " selected",
                            Toast.LENGTH_SHORT).show();
                    // TODO: Add to cart or show details
                });
            }
        }
    }
}