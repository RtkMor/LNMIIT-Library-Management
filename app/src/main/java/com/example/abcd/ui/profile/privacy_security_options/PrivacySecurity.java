package com.example.abcd.ui.profile.privacy_security_options;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.abcd.user_data.UserInfo;
import com.example.abcd.databinding.ActivityPrivacySecurityBinding;

import java.util.Objects;

public class PrivacySecurity extends AppCompatActivity {

    private TextView showPhoneNumber, showUserName;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPrivacySecurityBinding binding = ActivityPrivacySecurityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Get UI elements
        showUserName = binding.showUserName;
        showPhoneNumber = binding.showPhoneNumber;

        // Get user info from the UserInfo singleton
        UserInfo userInfo = UserInfo.getInstance();
        username = userInfo.getEmail();
        username = username.substring(0,username.indexOf('@'));

        // Load user info
        loadUserInfo(userInfo);

        binding.backBtnPrivacy.setOnClickListener(v -> finish());

        binding.changePasswordBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PrivacySecurity.this, ChangePassword.class);
            startActivity(intent);
        });

        binding.changePhoneNumberBtn.setOnClickListener(view -> {
            Intent intent = new Intent(PrivacySecurity.this, SendOTPActivity.class);
            intent.putExtra("initialMobileNumber", userInfo.getPhoneNumber());
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private void loadUserInfo(UserInfo userInfo) {
        showUserName.setText(username);
        showPhoneNumber.setText(userInfo.getPhoneNumber());
    }
}

