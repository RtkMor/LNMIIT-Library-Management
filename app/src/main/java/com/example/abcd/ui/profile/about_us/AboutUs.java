package com.example.abcd.ui.profile.about_us;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityAboutUsBinding;

import java.util.Objects;

public class AboutUs extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutUsBinding binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // back button
        findViewById(R.id.backBtnAboutUs).setOnClickListener(v -> finish());
    }
}