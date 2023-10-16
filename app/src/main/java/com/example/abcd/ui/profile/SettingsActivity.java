package com.example.abcd.ui.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abcd.R;
import com.example.abcd.ui.profile.about_app.AboutApp;
import com.example.abcd.ui.profile.about_us.AboutUs;
import com.example.abcd.ui.profile.contact_us.ContactUs;
import com.example.abcd.ui.profile.help_centre.HelpCentre;
import com.example.abcd.ui.profile.terms_privacy_policy.TermsPrivacyPolicy;
import com.example.abcd.user_data.UserInfo;
import com.example.abcd.ui.profile.privacy_security_options.PrivacySecurity;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private boolean isEditProfileFragmentOpen = false;
    private TextView myProfileName;
    private TextView myProfileBio;
    private TextView myProfileEmail;
    private ImageView myProfileImage;
    UserInfo userInfo = UserInfo.getInstance();

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @SuppressLint({"SetTextI18n", "ObsoleteSdkInt", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Get UI elements
        myProfileName = findViewById(R.id.settings_name);
        myProfileBio = findViewById(R.id.settings_bio);
        myProfileEmail = findViewById(R.id.settings_email);
        myProfileImage = findViewById(R.id.settings_profile_image);

        // edit profile button
        ImageView editProfileButton = findViewById(R.id.settings_edit_profile_button);
        editProfileButton.setOnClickListener(v -> {
            invisibleAll();
            openEditProfileFragment();
        });

        // night mode switch
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch btnToggleDark = findViewById(R.id.NightModeSwitch);
        boolean isDarkModeOn = sharedPreferences.getBoolean("night_mode_enabled", false);
        btnToggleDark.setChecked(isDarkModeOn);

        // Listen for Switch changes
        btnToggleDark.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // Toggle between light and dark mode
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            // Update the night mode setting in SharedPreferences
            editor = sharedPreferences.edit();
            editor.putBoolean("night_mode_enabled", isChecked);
            editor.apply();

            // Check if the edit profile fragment is open, and if so, keep the sections invisible
            if (isEditProfileFragmentOpen) {
                Log.d("Debug", "Edit profile fragment is open. Keeping sections invisible.");
                invisibleAll();
            } else {
                Log.d("Debug", "Edit profile fragment is not open. Sections will remain visible.");
            }
        });

        // notification switch
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch notificationSwitch = findViewById(R.id.NotificationSwitch);
        boolean isNotificationEnabled = sharedPreferences.getBoolean("notification_enabled", true);
        notificationSwitch.setChecked(isNotificationEnabled);
        // Add an OnCheckedChangeListener to handle switch state changes
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
            // Update the notification setting in SharedPreferences
            editor = sharedPreferences.edit();
            editor.putBoolean("notification_enabled", isChecked);
            editor.apply();
            if (isChecked) {
                // Notifications enabled
                Toast.makeText(SettingsActivity.this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Notifications disabled
                Toast.makeText(SettingsActivity.this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // privacy security button
        View privacySecurityBtn = findViewById(R.id.privacySecurityBtn);
        privacySecurityBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, PrivacySecurity.class)));

        // contact us button
        View contactUsBtn = findViewById(R.id.contactUsBtn);
        contactUsBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, ContactUs.class)));

        // help centre button
        View helpCentreBtn = findViewById(R.id.helpCentreBtn);
        helpCentreBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, HelpCentre.class)));

        // help centre button
        View privacyPolicyBtn = findViewById(R.id.privacyPolicyBtn);
        privacyPolicyBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, TermsPrivacyPolicy.class)));

        // about app button
        View aboutAppBtn = findViewById(R.id.aboutAppBtn);
        aboutAppBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, AboutApp.class)));

        // about app button
        View aboutUsBtn = findViewById(R.id.aboutUsBtn);
        aboutUsBtn.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, AboutUs.class)));


        // back button
        findViewById(R.id.backBtnSettings).setOnClickListener(v -> finish());

        // Load user data from UserInfo singleton and populate the UI
        loadUserInfoFromSingleton();
    }

    private void loadUserInfoFromSingleton() {

        // Get user data from the singleton
        String name = userInfo.getName();
        String bio = userInfo.getBio();
        String email = userInfo.getEmail();
        String profileImage = userInfo.getProfileImage();

        // Set data to UI
        myProfileName.setText(name);
        myProfileBio.setText(bio);
        myProfileEmail.setText(email);

        // Load and display profile image using Glide
        if (!TextUtils.isEmpty(profileImage)) {
            Glide.with(SettingsActivity.this)
                    .load(profileImage)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(myProfileImage);
        }
    }

    private void openEditProfileFragment() {
        isEditProfileFragmentOpen = true;
        EditProfileFragment editProfileFragment = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editProfileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void invisibleAll() {
        View profileSection1 = findViewById(R.id.ProfileSection1);
        View profileSection2 = findViewById(R.id.ProfileSection2);

        profileSection1.setVisibility(View.GONE);
        profileSection2.setVisibility(View.GONE);
    }
    public void showProfileSections() {
        View profileSection1 = findViewById(R.id.ProfileSection1);
        View profileSection2 = findViewById(R.id.ProfileSection2);

        // Check if the sections are currently set to GONE
        if (profileSection1.getVisibility() == View.GONE) {
            profileSection1.setVisibility(View.VISIBLE);
        }
        if (profileSection2.getVisibility() == View.GONE) {
            profileSection2.setVisibility(View.VISIBLE);
        }
    }
}

