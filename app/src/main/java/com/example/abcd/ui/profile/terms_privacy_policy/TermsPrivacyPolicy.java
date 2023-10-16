package com.example.abcd.ui.profile.terms_privacy_policy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityTermsPrivacyPolicyBinding;

import java.util.Objects;

public class TermsPrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTermsPrivacyPolicyBinding binding = ActivityTermsPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // back button
        findViewById(R.id.backBtnPrivacyPolicy).setOnClickListener(v -> finish());
    }
}