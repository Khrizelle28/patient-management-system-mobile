package com.example.patientmanagementsystemmobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.patientmanagementsystemmobile.databinding.ActivityHomeBinding;
import com.example.patientmanagementsystemmobile.databinding.ActivityMainBinding;
import com.example.patientmanagementsystemmobile.network.RetrofitClient;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize RetrofitClient to ensure AuthInterceptor has context
        RetrofitClient.init(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if we need to open a specific fragment (e.g., from notification)
        String openFragment = getIntent().getStringExtra("open_fragment");
        if ("rx_alert".equals(openFragment)) {
            replaceFragment(new RxAlertFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.rxalert);
        } else {
            replaceFragment(new HomeFragment());
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.mycart) {
                // Show DoctorsFragment (Shop/Products) when mycart is clicked
                // User can access cart by clicking cart icon in the shop
                replaceFragment(new DoctorsFragment());
            } else if (id == R.id.appointment) {
                replaceFragment(new AppointmentFragment());
            } else if (id == R.id.rxalert) {
                replaceFragment(new RxAlertFragment());
            } else if (id == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }

            return true;
        });

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });



    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();


    }
}