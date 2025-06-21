package com.example.patientmanagementsystemmobile;

import android.app.AlertDialog;
import android.content.Intent;
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

import com.example.patientmanagementsystemmobile.network.RetrofitClient;


// Optional: If using image loading libraries
// import com.bumptech.glide.Glide;
// import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView userName;
    private LinearLayout logoutLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserData();
        setClickListeners();
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
//        editProfileLayout = view.findViewById(R.id.editProfileLayout);
//        settingsLayout = view.findViewById(R.id.settingsLayout);
//        helpLayout = view.findViewById(R.id.helpLayout);
        logoutLayout = view.findViewById(R.id.logoutLayout);
    }

    private void loadUserData() {
        String name = RetrofitClient.currentUser.getFullName();

        // Set default values if data is empty
        if (name.isEmpty()) {
            name = "Guest User";
        }

        userName.setText(name);

        // Load profile image
        loadProfileImage(null);
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Using Glide (uncomment if you have Glide dependency)
            /*
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(profileImage);
            */

            // Using Picasso (uncomment if you have Picasso dependency)
            /*
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(profileImage);
            */
        } else {
            // Set default profile image
            profileImage.setImageResource(R.drawable.ic_person);
        }
    }

    private void performLogout() {
        // Clear user session data
        RetrofitClient.currentUser = null;

        // Clear any other session data (database, cache, etc.)
        clearUserSession();

        // Show logout message
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to Login Activity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity to prevent back navigation
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void clearUserSession() {
        // Add any additional cleanup logic here
        // For example:
        // - Clear database entries
        // - Clear cached files
        // - Reset app state
        // - Clear authentication tokens
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setIcon(R.drawable.ic_logout);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            performLogout();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setClickListeners() {
        logoutLayout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }
}