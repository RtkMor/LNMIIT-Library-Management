package com.example.abcd.login;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.admin.AdminActivity;
import com.example.abcd.user_data.FirebaseAuthManager;
import com.example.abcd.user_data.LoadUserInformation;
import com.example.abcd.R;
import com.example.abcd.databinding.ActivityLoginBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    // firebase auth
    private FirebaseAuth firebaseAuth;
    // progress Dialog
    ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        // The root layout
        View rootLayout = findViewById(R.id.rootLayout);

        // init firebase auth
        firebaseAuth = FirebaseAuthManager.getInstance();

        // handle click, new user
        binding.registerTV.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this,RegisterActivity.class)));

        // handle click, begin login
        binding.loginBtn.setOnClickListener(v -> validateData());

        // handle forgot password
        binding.forgotTv.setOnClickListener(v -> forgotPassword());

        // Set an OnTouchListener to the root layout to hide the keyboard
        rootLayout.setOnTouchListener((v, event) -> {
            // Check if an EditText has focus
            if (binding.emailEt.isFocused() || binding.passwordEt.isFocused()) {
                // Clear focus from the EditText fields
                binding.emailEt.clearFocus();
                binding.passwordEt.clearFocus();

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(binding.emailEt.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(binding.passwordEt.getWindowToken(), 0);
            }
            return false; // Return false to allow other touch events to be handled
        });

    }

    String email, password;
    int ind;
    private void validateData() {
        // Before login, lets do some data validation
        email = binding.emailEt.getText().toString().trim();
        email.toLowerCase();
        password = binding.passwordEt.getText().toString().trim();
        ind = email.indexOf('@');

        // validate data
        if(TextUtils.isEmpty(email)){
            binding.emailEt.setError("Enter username!");
            binding.emailEt.requestFocus();
        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordEt.setError("Enter password!");
            binding.passwordEt.requestFocus();
        }
        else{
            if(ind==-1) email += "@lnmiit.ac.in";
            loginUser(email, password);
        }
    }
    private void forgotPassword() {

        email = binding.emailEt.getText().toString().trim();
        email.toLowerCase();
        ind = email.indexOf('@');

        if(TextUtils.isEmpty(email)){
            binding.emailEt.setError("Enter username to reset password!");
            binding.emailEt.requestFocus();
            return;
        }

        if(ind==-1) email += "@lnmiit.ac.in";

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,"Check your email!", Toast.LENGTH_SHORT).show();
                    } else {
                        binding.emailEt.setError("User does not exist!");
                        binding.emailEt.requestFocus();
                    }
                });
    }

    private void loginUser(String email, String pwd) {

        // show progress
        progressDialog.setMessage("Logging In...!");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(LoginActivity.this, task -> {
            if(task.isSuccessful()){
                // Get instance of current user
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                // check if the user is admin or not
                if(email.equals("admin@lnmiit.ac.in")){
                    progressDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                    return;
                }

                // Check if email is verified or not
                assert firebaseUser != null;
                if(firebaseUser.isEmailVerified()){
                    // Load user data using the LoadUserData class
//                    LoadUserData loadUserData = new LoadUserData();
                    Intent intent = new Intent(LoginActivity.this, LoadUserInformation.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    firebaseUser.sendEmailVerification();
                    firebaseAuth.signOut();
                    showAlertDialog();
                }
            }
            else{
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e){
                    binding.emailEt.setError("User does not exist!");
                    binding.emailEt.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e){
                    binding.emailEt.setError("Invalid Credentials!");
                    binding.passwordEt.setError("Invalid Credentials!");
                    binding.emailEt.requestFocus();
                    binding.passwordEt.requestFocus();
                } catch (Exception e){
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            progressDialog.dismiss();
        });

    }

    // Check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null){
            if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                startActivity(new Intent(LoginActivity.this, LoadUserInformation.class));
                finish();
            }
        }
    }

    private void showAlertDialog() {
        // Setup the Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified!");
        builder.setMessage("Please verify your email before you login");

        // Open email app
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Create AlertDialog
        AlertDialog alertDialog = builder.create();

        // Show AlertDialog
        alertDialog.show();
    }

}
