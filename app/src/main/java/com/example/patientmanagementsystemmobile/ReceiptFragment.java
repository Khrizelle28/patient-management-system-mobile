package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Person;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.AppointmentRequest;
import com.example.patientmanagementsystemmobile.response.AppointmentResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptFragment extends Fragment {

    private static final String ARG_DOCTOR = "doctor";
    private static final String ARG_DATE = "date";
    private static final String ARG_PATIENT_ID = "patient_id";
    private static final String ARG_SERVICE = "service";
    private static final String ARG_SERVICE_PRICE = "service_price";
    private static final String ARG_HAS_PAP_SMEAR = "has_pap_smear";
    private static final String ARG_PAP_SMEAR_PRICE = "pap_smear_price";
    private static final String ARG_NEEDS_MED_CERT = "needs_med_cert";
    private static final String ARG_MED_CERT_PRICE = "med_cert_price";

    private Person doctor;
    private String selectedDate;
    private String patientId;
    private String service;
    private double servicePrice;
    private boolean hasPapSmear;
    private double papSmearPrice;
    private boolean needsMedCert;
    private double medCertPrice;

    private ImageView buttonBack;
    private TextView textPatientName;
    private TextView textAmountPaid;
    private LinearLayout layoutServicesList;
    private TextView textDateTime;
    private TextView textEmail;
    private MaterialButton buttonBookAppointment;

    private ApiService apiService;

    public static ReceiptFragment newInstance(Person doctor, String date, String patientId,
                                              String service, double servicePrice,
                                              boolean hasPapSmear, double papSmearPrice,
                                              boolean needsMedCert, double medCertPrice) {
        ReceiptFragment fragment = new ReceiptFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DOCTOR, doctor);
        args.putString(ARG_DATE, date);
        args.putString(ARG_PATIENT_ID, patientId);
        args.putString(ARG_SERVICE, service);
        args.putDouble(ARG_SERVICE_PRICE, servicePrice);
        args.putBoolean(ARG_HAS_PAP_SMEAR, hasPapSmear);
        args.putDouble(ARG_PAP_SMEAR_PRICE, papSmearPrice);
        args.putBoolean(ARG_NEEDS_MED_CERT, needsMedCert);
        args.putDouble(ARG_MED_CERT_PRICE, medCertPrice);
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
            service = getArguments().getString(ARG_SERVICE);
            servicePrice = getArguments().getDouble(ARG_SERVICE_PRICE);
            hasPapSmear = getArguments().getBoolean(ARG_HAS_PAP_SMEAR);
            papSmearPrice = getArguments().getDouble(ARG_PAP_SMEAR_PRICE);
            needsMedCert = getArguments().getBoolean(ARG_NEEDS_MED_CERT);
            medCertPrice = getArguments().getDouble(ARG_MED_CERT_PRICE);
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
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
        textPatientName = view.findViewById(R.id.textPatientName);
        textAmountPaid = view.findViewById(R.id.textAmountPaid);
        layoutServicesList = view.findViewById(R.id.layoutServicesList);
        textDateTime = view.findViewById(R.id.textDateTime);
        buttonBookAppointment = view.findViewById(R.id.buttonBookAppointment);
    }

    private void populateData() {
        // Patient name
        if (RetrofitClient.currentUser != null) {
            textPatientName.setText(RetrofitClient.currentUser.getFullName());
        }

        // Total amount
        double totalAmount = servicePrice + papSmearPrice + medCertPrice;
        textAmountPaid.setText(String.format(Locale.getDefault(), "Php %.2f", totalAmount));

        // Services list
        addServiceToList(service, servicePrice);
        if (hasPapSmear) {
            addServiceToList("Pap smear", papSmearPrice);
        }
        if (needsMedCert) {
            addServiceToList("Medical Certificate", medCertPrice);
        }

        // Date/Time
        if (selectedDate != null && doctor != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                String formattedDate = outputFormat.format(inputFormat.parse(selectedDate));
                textDateTime.setText(formattedDate + "\n" + doctor.getSchedule());
            } catch (Exception e) {
                textDateTime.setText(selectedDate + "\n" + doctor.getSchedule());
            }
        }
    }

    private void addServiceToList(String serviceName, double price) {
        LinearLayout serviceLayout = new LinearLayout(getContext());
        serviceLayout.setOrientation(LinearLayout.HORIZONTAL);
        serviceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView serviceNameText = new TextView(getContext());
        serviceNameText.setText(serviceName);
        serviceNameText.setTextSize(14);
        serviceNameText.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        serviceNameText.setLayoutParams(nameParams);

        TextView servicePriceText = new TextView(getContext());
        servicePriceText.setText(String.format(Locale.getDefault(), "Php %.2f", price));
        servicePriceText.setTextSize(14);
        servicePriceText.setTextColor(getResources().getColor(android.R.color.black));

        serviceLayout.addView(serviceNameText);
        serviceLayout.addView(servicePriceText);

        layoutServicesList.addView(serviceLayout);
    }

    private void setClickListeners() {
        buttonBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        buttonBookAppointment.setOnClickListener(v -> {
            bookAppointment();
        });
    }

    private void bookAppointment() {
        // Create appointment notes with service details
        StringBuilder notes = new StringBuilder();
        notes.append("Service: ").append(service);
        if (hasPapSmear) {
            notes.append(", Pap smear: Yes");
        }
        if (needsMedCert) {
            notes.append(", Medical Certificate: Yes");
        }

        // Calculate total amount
        double totalAmount = servicePrice + papSmearPrice + medCertPrice;

        // Use the new constructor with all service details
        AppointmentRequest request = new AppointmentRequest(
                patientId,
                doctor.getId(),
                selectedDate,
                doctor.getSchedule(),
                notes.toString(),
                service,
                servicePrice,
                hasPapSmear,
                hasPapSmear ? papSmearPrice : 0.0,
                needsMedCert,
                needsMedCert ? medCertPrice : 0.0,
                totalAmount
        );

        Call<AppointmentResponse> call = apiService.createAppointment(request);
        call.enqueue(new Callback<AppointmentResponse>() {
            @Override
            public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AppointmentResponse appointmentResponse = response.body();
                    if (appointmentResponse.isSuccess()) {
                        Toast.makeText(getContext(), "Appointment booked successfully!", Toast.LENGTH_LONG).show();

                        // Navigate back to appointment fragment
                        navigateBackToAppointments();
                    } else {
                        Toast.makeText(getContext(), appointmentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to book appointment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBackToAppointments() {
        // Clear the back stack and return to appointment fragment
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
