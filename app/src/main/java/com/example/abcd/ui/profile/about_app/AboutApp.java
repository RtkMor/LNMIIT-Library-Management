package com.example.abcd.ui.profile.about_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityAboutAppBinding;

import java.util.Objects;

public class AboutApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutAppBinding binding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // back button
        findViewById(R.id.backBtnAboutApp).setOnClickListener(v -> finish());
    }
}