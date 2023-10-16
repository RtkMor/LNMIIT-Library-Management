package com.example.abcd.ui.profile.help_centre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityHelpCentreBinding;

import java.util.Objects;

public class HelpCentre extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelpCentreBinding binding = ActivityHelpCentreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // back button
        findViewById(R.id.backBtnHelpCentre).setOnClickListener(v -> finish());
    }
}