package com.example.abcd.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.abcd.R;

import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Button contactUsBtn = findViewById(R.id.contactUsAdminBtn);
        Button changeNewArrivalsBtn = findViewById(R.id.changeNewArrivalsAdminBtn);
        Button addNewBookBtn = findViewById(R.id.addNewBookAdminBtn);

        contactUsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this,ContactUsAdminActivity.class);
            startActivity(intent);
        });

        changeNewArrivalsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this,ChangeNewArrivalsActivity.class);
            startActivity(intent);
        });

        addNewBookBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this,AddNewBookActivity.class);
            startActivity(intent);
        });
    }
}