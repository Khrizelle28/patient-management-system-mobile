package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.patientmanagementsystemmobile.models.Person;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ServiceSelectionFragment extends Fragment {

    private static final String ARG_DOCTOR = "doctor";
    private static final String ARG_DATE = "date";
    private static final String ARG_PATIENT_ID = "patient_id";

    private Person doctor;
    private String selectedDate;
    private String patientId;

    private ImageView buttonBack;
    private TextView textDoctorName;
    private TextView textDoctorSpecialty;
    private TextView textSelectedDate;
    private TextView textSelectedTime;
    private RadioGroup radioGroupServices;
    private CheckBox checkboxPapSmear;
    private RadioGroup radioGroupMedCert;
    private MaterialButton buttonGoToPayment;

    public static ServiceSelectionFragment newInstance(Person doctor, String date, String patientId) {
        ServiceSelectionFragment fragment = new ServiceSelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DOCTOR, doctor);
        args.putString(ARG_DATE, date);
        args.putString(ARG_PATIENT_ID, patientId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctor = (Person) getArguments().getSerializable(ARG_DOCTOR);
            selectedDate = getArguments().getString(ARG_DATE);
            patientId = getArguments().getString(ARG_PATIENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        populateData();
        setClickListeners();
    }

    private void initViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);
        textDoctorName = view.findViewById(R.id.textDoctorName);
        textDoctorSpecialty = view.findViewById(R.id.textDoctorSpecialty);
        textSelectedDate = view.findViewById(R.id.textSelectedDate);
        textSelectedTime = view.findViewById(R.id.textSelectedTime);
        radioGroupServices = view.findViewById(R.id.radioGroupServices);
        checkboxPapSmear = view.findViewById(R.id.checkboxPapSmear);
        radioGroupMedCert = view.findViewById(R.id.radioGroupMedCert);
        buttonGoToPayment = view.findViewById(R.id.buttonGoToPayment);
    }

    private void populateData() {
        if (doctor != null) {
            // Remove "Dr. " prefix if present for display
            String doctorName = doctor.getName().replace("Dr. ", "");
            textDoctorName.setText(doctorName);
            textDoctorSpecialty.setText(doctor.getSpecialty());
            textSelectedTime.setText(doctor.getSchedule());
        }

        if (selectedDate != null) {
            // Format date for display
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                textSelectedDate.setText(outputFormat.format(inputFormat.parse(selectedDate)));
            } catch (Exception e) {
                textSelectedDate.setText(selectedDate);
            }
        }
    }

    private void setClickListeners() {
        buttonBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        buttonGoToPayment.setOnClickListener(v -> {
            navigateToReceipt();
        });
    }

    private void navigateToReceipt() {
        // Get selected base service (Pregnant or Non-Pregnant)
        String selectedService = getSelectedService();
        double servicePrice = getServicePrice();

        // Check if Pap smear is selected
        boolean hasPapSmear = checkboxPapSmear.isChecked();
        double papSmearPrice = hasPapSmear ? 1500.0 : 0.0;

        // Get medical certificate option
        boolean needsMedCert = ((RadioButton) getView().findViewById(R.id.radioMedCertYes)).isChecked();
        double medCertPrice = needsMedCert ? 150.0 : 0.0;

        // Navigate to ReceiptFragment
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(
                doctor,
                selectedDate,
                patientId,
                selectedService,
                servicePrice,
                hasPapSmear,
                papSmearPrice,
                needsMedCert,
                medCertPrice
        );

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, receiptFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getSelectedService() {
        int selectedId = radioGroupServices.getCheckedRadioButtonId();
        if (selectedId == R.id.radioPregnant) {
            return "Pregnant";
        } else if (selectedId == R.id.radioNonPregnant) {
            return "Non - Pregnant";
        }
        return "Pregnant"; // default
    }

    private double getServicePrice() {
        // Both Pregnant and Non-Pregnant cost the same
        return 500.0;
    }
}
