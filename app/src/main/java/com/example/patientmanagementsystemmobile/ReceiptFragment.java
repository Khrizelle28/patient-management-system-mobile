package com.example.patientmanagementsystemmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.patientmanagementsystemmobile.api.ApiService;
import com.example.patientmanagementsystemmobile.models.Person;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;
import com.example.patientmanagementsystemmobile.request.AppointmentRequest;
import com.example.patientmanagementsystemmobile.request.CreatePaymentRequest;
import com.example.patientmanagementsystemmobile.request.ExecutePaymentRequest;
import com.example.patientmanagementsystemmobile.response.AppointmentResponse;
import com.example.patientmanagementsystemmobile.response.PaymentResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptFragment extends Fragment {

    private static final int REQUEST_CODE_PAYMENT = 1001;

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
    private MaterialButton buttonBookAppointment;

    private ApiService apiService;
    private String currentPaymentId;
    private int currentAppointmentId;

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

        // Amount Paid - only base service price (other services paid at clinic)
        textAmountPaid.setText(String.format(Locale.getDefault(), "Php %.2f", servicePrice));

        // Services list - add base service
        addServiceToList(service, servicePrice, false);

        // Add other services (shown but not included in payment)
        if (hasPapSmear) {
            addServiceToList("Pap smear", 1500.0, true);
        }
        if (needsMedCert) {
            addServiceToList("Medical Certificate", 150.0, true);
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

    private void addServiceToList(String serviceName, double price, boolean isOtherService) {
        LinearLayout serviceLayout = new LinearLayout(getContext());
        serviceLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 8);
        serviceLayout.setLayoutParams(layoutParams);

        TextView serviceNameText = new TextView(getContext());
        serviceNameText.setText(serviceName);
        serviceNameText.setTextSize(14);

        // Gray out "other services" that are paid at clinic
        if (isOtherService) {
            serviceNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            serviceNameText.setTextColor(getResources().getColor(android.R.color.black));
        }

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        serviceNameText.setLayoutParams(nameParams);

        TextView servicePriceText = new TextView(getContext());
        servicePriceText.setText(String.format(Locale.getDefault(), "Php %.2f", price));
        servicePriceText.setTextSize(14);

        // Gray out "other services" prices
        if (isOtherService) {
            servicePriceText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            servicePriceText.setTextColor(getResources().getColor(android.R.color.black));
        }

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
        // Disable button and show loading state
        buttonBookAppointment.setEnabled(false);
        buttonBookAppointment.setText("Processing...");

        // If appointment already created (e.g., payment was cancelled), retry payment
        if (currentAppointmentId > 0) {
            initiatePayment(currentAppointmentId, servicePrice);
            return;
        }

        // Create appointment first, then initiate payment
        // Create appointment notes with service details
        StringBuilder notes = new StringBuilder();
        notes.append("Service: ").append(service);
        if (hasPapSmear) {
            notes.append(", Pap smear: Yes (paid at clinic)");
        }
        if (needsMedCert) {
            notes.append(", Medical Certificate: Yes (paid at clinic)");
        }

        // Total amount - only base service price (other services paid at clinic)
        double totalAmount = servicePrice;

        // Create appointment request
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

        // First, create the appointment
        apiService.createAppointment(request).enqueue(new Callback<AppointmentResponse>() {
            @Override
            public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AppointmentResponse appointmentResponse = response.body();
                    currentAppointmentId = appointmentResponse.getData().getId();

                    // Now create payment for this appointment
                    initiatePayment(currentAppointmentId, totalAmount);
                } else {
                    resetButton();
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to create appointment";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentResponse> call, Throwable t) {
                resetButton();
                Toast.makeText(getContext(),
                    "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetButton() {
        buttonBookAppointment.setEnabled(true);
        buttonBookAppointment.setText("Pay with PayPal");
    }

    private void initiatePayment(int appointmentId, double amount) {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
            "appointment",
            appointmentId,
            amount,
            "PHP",
            "Appointment Payment - " + service
        );

        apiService.createPayment(paymentRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PaymentResponse paymentResponse = response.body();
                    String approvalUrl = paymentResponse.getData().getApproval_url();
                    currentPaymentId = paymentResponse.getData().getPayment_id();

                    // Launch PayPal WebView
                    Intent intent = new Intent(getContext(), PayPalWebViewActivity.class);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_URL, approvalUrl);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_PAYMENT_TYPE, "appointment");
                    intent.putExtra(PayPalWebViewActivity.EXTRA_ITEM_ID, appointmentId);
                    intent.putExtra(PayPalWebViewActivity.EXTRA_PAYMENT_ID, currentPaymentId);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                } else {
                    resetButton();
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to create payment";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                resetButton();
                Toast.makeText(getContext(),
                    "Payment initialization failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                String payerId = data.getStringExtra("payer_id");
                String paymentId = data.getStringExtra("payment_id");
                String paymentType = data.getStringExtra("payment_type");

                if (payerId != null && paymentId != null) {
                    executePaymentAndCreateAppointment(paymentId, payerId, paymentType);
                } else {
                    resetButton();
                    Toast.makeText(getContext(), "Payment cancelled. You can try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                resetButton();
                Toast.makeText(getContext(), "Payment cancelled. You can try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executePaymentAndCreateAppointment(String paymentId, String payerId, String paymentType) {
        ExecutePaymentRequest executeRequest = new ExecutePaymentRequest(paymentId, payerId, paymentType);

        apiService.executePayment(executeRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Payment successful, appointment already created and updated by backend
                    showPaymentSuccessDialog();
                } else {
                    resetButton();
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Payment execution failed";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                resetButton();
                Toast.makeText(getContext(),
                    "Payment execution failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPaymentSuccessDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Payment Successful!");
        builder.setMessage("Your payment has been completed successfully.\n\nYour appointment has been booked.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            navigateBackToAppointments();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void navigateBackToAppointments() {
        // Clear the back stack and return to appointment fragment
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
