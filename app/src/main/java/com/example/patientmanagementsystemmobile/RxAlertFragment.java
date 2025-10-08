package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.patientmanagementsystemmobile.adapter.MedicationAdapter;
import com.example.patientmanagementsystemmobile.models.MedicationAlert;

import java.util.ArrayList;
import java.util.List;

public class RxAlertFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<MedicationAlert> medicationList;

    public RxAlertFragment() {
        // Required empty public constructor
    }

    public static RxAlertFragment newInstance(String param1, String param2) {
        RxAlertFragment fragment = new RxAlertFragment();
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

        // Initialize medication data
        initializeMedicationData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rx_alert, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMedications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter
        adapter = new MedicationAdapter(medicationList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void initializeMedicationData() {
        medicationList = new ArrayList<>();
        medicationList.add(new MedicationAlert("7:00", "AM", "Amoxicillin", true));
        medicationList.add(new MedicationAlert("8:01", "AM", "Paracetamol", true));
        medicationList.add(new MedicationAlert("9:02", "AM", "Alma Pills", true));
        medicationList.add(new MedicationAlert("1:03", "PM", "Cetirizine", true));
        medicationList.add(new MedicationAlert("5:02", "PM", "Alma Pills", true));
    }
}