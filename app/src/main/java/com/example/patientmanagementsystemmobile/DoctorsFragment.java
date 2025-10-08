package com.example.patientmanagementsystemmobile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

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
    }

    private void setupMedicationData() {
        medicationList = new ArrayList<>();
        medicationList.add(new Medication(
                "Amoxicillin",
                "$199.99",
                "Used to treat infections such as respiratory tract infections, ear infections, and urinary tract infections.",
                "💊"
        ));
        medicationList.add(new Medication(
                "Paracetamol",
                "$199.99",
                "Used to alleviate mild to moderate pain such as headache, muscle aches, and fever.",
                "💉"
        ));
    }

    private void setupRecyclerView() {
        medicationAdapter = new MedicationAdapter(medicationList);
        medicationRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
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
    }

    // Medication data class
    public static class Medication {
        private String name;
        private String price;
        private String description;
        private String emoji;

        public Medication(String name, String price, String description, String emoji) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.emoji = emoji;
        }

        // Getters
        public String getName() { return name; }
        public String getPrice() { return price; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
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