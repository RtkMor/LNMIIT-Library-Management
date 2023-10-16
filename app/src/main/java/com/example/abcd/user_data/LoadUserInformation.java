package com.example.abcd.user_data;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.abcd.MainActivity;
import com.example.abcd.R;
import com.google.firebase.FirebaseApp;

import java.util.Objects;

public class LoadUserInformation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_user_information);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Progress Bar
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseAuthManager.getInstance();

        // Load User Data
        LoadUserData loadUserData = LoadUserData.getInstance();
        loadUserData.loadUserInfo(new LoadUserData.UserInfoLoadListener() {
            @Override
            public void onUserInfoLoaded(UserInfo userInfo) {
                progressBar.setVisibility(View.GONE);

                // Load night mode preference from SharedPreferences
                boolean isNightModeEnabled = loadNightModePreference();

                // Set night mode
                int appCompatNightMode = isNightModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
                AppCompatDelegate.setDefaultNightMode(appCompatNightMode);

                Intent intent = new Intent(LoadUserInformation.this, MainActivity.class);
                intent.putExtra("homeUpdated",true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LoadUserInformation.this, "User info failed to load", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // Function to load night mode preference from SharedPreferences
    private boolean loadNightModePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("night_mode_enabled", false); // Use 'false' as a default value if the preference doesn't exist
    }
}