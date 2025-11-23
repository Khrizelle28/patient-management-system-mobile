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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.patientmanagementsystemmobile.models.Person;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
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
    private ImageView imageDoctorProfile;
    private TextView textDoctorName;
    private TextView textDoctorSpecialty;
    private TextView textSelectedDate;
    private TextView textSelectedTime;
    private RadioGroup radioGroupServices;
    private RadioButton radioPregnant;
    private RadioButton radioNonPregnant;
    private CheckBox checkboxPapSmear;
    private CheckBox checkboxMedCert;
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
        imageDoctorProfile = view.findViewById(R.id.imageDoctorProfile);
        textDoctorName = view.findViewById(R.id.textDoctorName);
        textDoctorSpecialty = view.findViewById(R.id.textDoctorSpecialty);
        textSelectedDate = view.findViewById(R.id.textSelectedDate);
        textSelectedTime = view.findViewById(R.id.textSelectedTime);
        radioGroupServices = view.findViewById(R.id.radioGroupServices);
        radioPregnant = view.findViewById(R.id.radioPregnant);
        radioNonPregnant = view.findViewById(R.id.radioNonPregnant);
        checkboxPapSmear = view.findViewById(R.id.checkboxPapSmear);
        checkboxMedCert = view.findViewById(R.id.checkboxMedCert);
        buttonGoToPayment = view.findViewById(R.id.buttonGoToPayment);
    }

    private void populateData() {
        if (doctor != null) {
            // Remove "Dr. " prefix if present for display
            String doctorName = doctor.getName().replace("Dr. ", "");
            textDoctorName.setText(doctorName);
            textDoctorSpecialty.setText(doctor.getSpecialty());
            textSelectedTime.setText(doctor.getSchedule());

            // Load doctor's profile picture
            if (doctor.getProfilePic() != null && !doctor.getProfilePic().isEmpty()) {
                String imageUrl = RetrofitClient.getFullImageUrl(doctor.getProfilePic());
                android.util.Log.d("ServiceSelection", "Doctor profile pic from API: " + doctor.getProfilePic());
                android.util.Log.d("ServiceSelection", "Full profile pic URL: " + imageUrl);

                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(imageDoctorProfile);
            } else {
                // Set default profile image
                imageDoctorProfile.setImageResource(R.drawable.ic_person);
            }
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

        // Manual handling for RadioButtons since they're inside LinearLayouts
        radioPregnant.setOnClickListener(v -> {
            radioPregnant.setChecked(true);
            radioNonPregnant.setChecked(false);
        });

        radioNonPregnant.setOnClickListener(v -> {
            radioNonPregnant.setChecked(true);
            radioPregnant.setChecked(false);
        });

        buttonGoToPayment.setOnClickListener(v -> {
            navigateToReceipt();
        });
    }

    private void navigateToReceipt() {
        // Get selected base service (Pregnant or Non-Pregnant)
        String selectedService = getSelectedService();
        double servicePrice = getServicePrice();

        // Check if Pap smear is selected (payment made at clinic, so price is 0 in app)
        boolean hasPapSmear = checkboxPapSmear.isChecked();
        double papSmearPrice = 0.0; // Payment made at clinic

        // Get medical certificate option (payment made at clinic, so price is 0 in app)
        boolean needsMedCert = checkboxMedCert.isChecked();
        double medCertPrice = 0.0; // Payment made at clinic

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
        if (radioPregnant.isChecked()) {
            return "Pregnant";
        } else if (radioNonPregnant.isChecked()) {
            return "Non - Pregnant";
        }
        return "Pregnant"; // default
    }

    private double getServicePrice() {
        // Both Pregnant and Non-Pregnant cost the same
        return 500.0;
    }
}
