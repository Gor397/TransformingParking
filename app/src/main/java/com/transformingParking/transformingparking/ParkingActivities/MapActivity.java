package com.transformingParking.transformingparking.ParkingActivities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.transformingParking.transformingparking.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.transformingParking.transformingparking.databinding.ActivityMapBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_map);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("fragment")) {
                String receivedString = intent.getStringExtra("fragment");

                selectFragment(receivedString, navController);
            }
        }
    }

    private void selectFragment(String receivedString, NavController navController) {
        switch (receivedString) {
            case "navigation_home":
                navController.navigate(R.id.navigation_home);
                break;
            case "navigation_notifications":
                navController.navigate(R.id.navigation_notifications);
                break;
            case "navigation_profile":
                navController.navigate(R.id.navigation_profile);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes here
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}