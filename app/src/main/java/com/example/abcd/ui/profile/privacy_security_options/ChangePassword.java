package com.example.abcd.ui.profile.privacy_security_options;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abcd.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    private EditText oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private FirebaseAuth firebaseAuth;
    private String username, newPassword;
    private ProgressBar progressBar;
    private TextView changePasswordButton;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Objects.requireNonNull(getSupportActionBar()).hide();

        firebaseAuth = FirebaseAuth.getInstance();
        String email = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        assert email != null;
        int atIndex = email.indexOf('@');
        username = email.substring(0, atIndex);

        oldPasswordEditText = findViewById(R.id.oldPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        changePasswordButton = findViewById(R.id.confirmBtn);
        ImageView backBtn = findViewById(R.id.backBtnChangePassword);

        // Initialize progressBar
        progressBar = findViewById(R.id.progressBar);

        // change password button
        changePasswordButton.setOnClickListener(view -> changePassword());

        // hide keyboard
        View rootLayout = findViewById(R.id.changePasswordLayout); // Replace with your actual root layout ID
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Check if the touch was outside of the EditText fields
                View currentFocus = getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    Rect outRect = new Rect();
                    currentFocus.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        // If the touch was outside, hide the keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        currentFocus.clearFocus();
                    }
                }
            }
            return false;
        });

        // back button
        backBtn.setOnClickListener(view -> finish());
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if(newPassword.length()<8){
            Toast.makeText(this, "New password too small", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        changePasswordButton.setEnabled(false);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), oldPassword);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            updatePassword();
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            changePasswordButton.setEnabled(true);
                                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            changePasswordButton.setEnabled(true);
                            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            changePasswordButton.setEnabled(true);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("password", newPassword);

        databaseReference.child(username)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    changePasswordButton.setEnabled(true);
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    changePasswordButton.setEnabled(true);
                    Toast.makeText(this, "Failed to update password in database", Toast.LENGTH_SHORT).show();
                });
    }
}
