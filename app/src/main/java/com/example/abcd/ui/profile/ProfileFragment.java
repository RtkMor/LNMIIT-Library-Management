package com.example.abcd.ui.profile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abcd.R;
import com.example.abcd.login.LoginActivity;
import com.example.abcd.user_data.FirebaseAuthManager;
import com.example.abcd.user_data.UserInfo;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private TextView myProfileName;
    private TextView myProfileBio;
    private TextView myProfileEmail;
    private ImageView myProfileImage;
    private UserInfo userInfo;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        // Get UI elements
        myProfileName = rootView.findViewById(R.id.my_profile_name);
        myProfileBio = rootView.findViewById(R.id.my_profile_bio);
        myProfileEmail = rootView.findViewById(R.id.my_profile_email);
        myProfileImage = rootView.findViewById(R.id.my_profile_image);

        // Retrieve the user information that was loaded at login time
        userInfo = UserInfo.getInstance();
        String username = userInfo.getUsername();

        updateUIWithUserInfo();

        // Settings Button
        ImageView settingsBtn = rootView.findViewById(R.id.settingsIcon);
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        // Favorite Books Button
        View favBtn = rootView.findViewById(R.id.favoriteBooksBtn);
        favBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FavoriteBookActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Reviewed Books Button
        View revBtn = rootView.findViewById(R.id.reviewedBooksBtn);
        revBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReviewedBooksActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        // Log Out Button
        View logOut = rootView.findViewById(R.id.logOutBtn);
        logOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Back Button
        ImageView backBtn = rootView.findViewById(R.id.backBtnProfile);
        backBtn.setOnClickListener(v -> requireActivity().onBackPressed());

        return rootView;
    }

    private void updateUIWithUserInfo() {
        if (userInfo != null) {
            myProfileName.setText(userInfo.getName());
            myProfileBio.setText(userInfo.getBio());
            myProfileEmail.setText(userInfo.getEmail());

            // Load and display profile image using Glide
            String profileImage = userInfo.getProfileImage();
            if (!TextUtils.isEmpty(profileImage)) {
                Glide.with(requireActivity())
                        .load(profileImage)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(myProfileImage);
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        final Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
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

        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        requireActivity().finish();
    }
}
