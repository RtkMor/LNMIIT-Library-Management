package com.example.abcd.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abcd.R;
import com.example.abcd.databinding.ActivityOtherProfileBinding;
import com.example.abcd.login.LoginActivity;
import com.example.abcd.user_data.FirebaseAuthManager;
import com.example.abcd.user_data.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class OtherProfileActivity extends AppCompatActivity {

    private TextView myProfileName;
    private TextView myProfileBio;
    private TextView myProfileEmail;
    private ImageView myProfileImage;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOtherProfileBinding binding = ActivityOtherProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Get UI elements
        myProfileName = binding.otherProfileName;
        myProfileBio = binding.otherProfileBio;
        myProfileEmail = binding.otherProfileEmail;
        myProfileImage = binding.otherProfileImage;
        TextView profileUsername = binding.profileUsername;
        ImageView backBtn = binding.backBtnOtherProfile;

        ImageView settingsBtn = binding.settingsIcon;
        View viewBar = binding.viewBar;
        RelativeLayout logOut = binding.logOutBtn;

        String username = getIntent().getStringExtra("username");
        profileUsername.setText(username);

        String currentUser = UserInfo.getInstance().getUsername();

        if(Objects.equals(username, currentUser)){
            profileUsername.setText("My Profile");
            settingsBtn.setVisibility(View.VISIBLE);
            viewBar.setVisibility(View.VISIBLE);
            logOut.setVisibility(View.VISIBLE);
        }

        updateUIWithUserInfo(username);

        // Favorite Books Button
        View favBtn = binding.favoriteBooksBtn;
        favBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OtherProfileActivity.this, FavoriteBookActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Reviewed Books Button
        View revBtn = binding.reviewedBooksBtn;
        revBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OtherProfileActivity.this, ReviewedBooksActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Settings Button
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OtherProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Log Out Button
        logOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Back Button (for someone else's profile)
        backBtn.setOnClickListener(v -> onBackPressed());
    }

    // Update UI with user information
    private void updateUIWithUserInfo(String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(username);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User data found, update the UI elements with this data
                    UserInfo otherUserInfo = dataSnapshot.getValue(UserInfo.class);
                    if (otherUserInfo != null) {
                        myProfileName.setText(otherUserInfo.getName());
                        myProfileBio.setText(otherUserInfo.getBio());
                        myProfileEmail.setText(otherUserInfo.getEmail());

                        // Load and display profile image using Glide
                        String profileImage = otherUserInfo.getProfileImage();
                        if (!TextUtils.isEmpty(profileImage)) {
                            Glide.with(OtherProfileActivity.this)
                                    .load(profileImage)
                                    .apply(RequestOptions.circleCropTransform())
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(myProfileImage);
                        }
                    }
                } else {
                    // Handle the case where the user's profile doesn't exist in the database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occurred during the data fetch
            }
        });
    }
    private void showLogoutConfirmationDialog() {
        Objects.requireNonNull(getSupportActionBar()).hide();

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_logout_confirmation);

        TextView btnCancel = dialog.findViewById(R.id.dialog_button_cancel);
        TextView btnConfirm = dialog.findViewById(R.id.dialog_button_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        dialog.show();
    }
    private void performLogout() {

        FirebaseAuthManager.logOut();
        UserInfo.getInstance().clearUserData();

        Intent loginIntent = new Intent(OtherProfileActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}