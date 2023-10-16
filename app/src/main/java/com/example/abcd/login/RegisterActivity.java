package com.example.abcd.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize progressBar
        progressBar = findViewById(R.id.progressBar);

        // handle click go back
        binding.backBtnRegister.setOnClickListener(view -> finish());

        // Set an OnTouchListener to the root layout to hide the keyboard
        binding.rootLayout.setOnTouchListener((v, event) -> {
            // Check if an EditText has focus
            if (binding.emailEt.isFocused() || binding.passwordEt.isFocused() || binding.nameEt.isFocused() || binding.confirmPasswordEt.isFocused()) {
                // Clear focus from the EditText fields
                binding.nameEt.clearFocus();
                binding.emailEt.clearFocus();
                binding.passwordEt.clearFocus();
                binding.confirmPasswordEt.clearFocus();

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.emailEt.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(binding.passwordEt.getWindowToken(), 0);
            }
            return false; // Return false to allow other touch events to be handled
        });

        // handle register button
        binding.registerBtn.setOnClickListener(view -> validateData());
    }

    private String name = "";
    private String email = "";
    private String password = "";
    private String username = "";

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().toLowerCase(Locale.ROOT).trim();
        password = binding.passwordEt.getText().toString();
        String confirmPassword = binding.confirmPasswordEt.getText().toString();
        String collegeDomain = "@lnmiit.ac.in";
        if(TextUtils.isEmpty(name)){
            binding.nameEt.setError("Enter name!");
            binding.nameEt.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Enter valid email!");
            binding.emailEt.requestFocus();
        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordEt.setError("Enter password!");
            binding.passwordEt.requestFocus();
        }
        else if(password.length()<8){
            binding.passwordEt.setError("Too short!");
            binding.passwordEt.requestFocus();
        }
        else if(!password.equals(confirmPassword)){
            binding.confirmPasswordEt.setError("Password doesn't match!");
            binding.confirmPasswordEt.requestFocus();
        }
        else if(!email.endsWith(collegeDomain)){
            binding.emailEt.setError("Enter college email!");
            binding.emailEt.requestFocus();
        }
        else{
            int atIndex = email.indexOf('@');
            username = email.substring(0, atIndex);
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // show progress bar
        showProgressBar(true);
        // Create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Send verification email
                    Objects.requireNonNull(authResult.getUser()).sendEmailVerification();
                    updateUserInfo();
                })
                .addOnFailureListener(e -> {
                    // Hide progressBar and show toast
                    showProgressBar(false);
                    Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {

        showProgressBar(true);

        // setup data to add in database
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("name",name);
        hashMap.put("email",email);
        hashMap.put("username",email.substring(0,email.indexOf('@')));
        hashMap.put("profileImage","");
        hashMap.put("userType","user");
        hashMap.put("password",password);
        hashMap.put("phoneNumber","-");

        // set data to database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(username)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    showProgressBar(false);
                    Toast.makeText(RegisterActivity.this, "User Registered Successfully. Please Verify Your Email!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgressBar(false);
                    Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressBar(boolean isVisible) {
        if (isVisible) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

}