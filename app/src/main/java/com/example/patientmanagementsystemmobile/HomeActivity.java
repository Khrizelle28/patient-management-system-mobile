package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText;
    private Spinner estimatedTimeSpinner;
    private CheckBox rememberCheckBox;
    private Button goToPaymentButton;
    private ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        estimatedTimeSpinner = findViewById(R.id.estimatedTimeSpinner);
        rememberCheckBox = findViewById(R.id.rememberCheckBox);
//        goToPaymentButton = findViewById(R.id.goToPaymentButton);
        backButton = findViewById(R.id.backButton);

        // Set up spinner
        String[] options = {"Scheduled Pickup (1 Day)", "Scheduled Pickup (2 Days)", "Express Pickup"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        estimatedTimeSpinner.setAdapter(adapter);

        // Handle back button
        backButton.setOnClickListener(v -> finish());

        // Handle button click
        goToPaymentButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String pickupTime = estimatedTimeSpinner.getSelectedItem().toString();
            boolean remember = rememberCheckBox.isChecked();

            // You can replace this Toast with logic to proceed to payment
            Toast.makeText(HomeActivity.this,
                    "Name: " + name + "\nPhone: " + phone + "\nPickup: " + pickupTime,
                    Toast.LENGTH_SHORT).show();
        });

    }
}