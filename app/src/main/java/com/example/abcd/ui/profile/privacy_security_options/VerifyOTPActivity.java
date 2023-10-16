package com.example.abcd.ui.profile.privacy_security_options;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.user_data.FirebaseAuthManager;
import com.example.abcd.user_data.LoadUserData;
import com.example.abcd.R;
import com.example.abcd.user_data.UserInfo;
import com.example.abcd.login.LoginActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    String mobileNumber, verificationId, username, email;
    String code;

    private ProgressDialog progressDialog;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verify);

        Objects.requireNonNull(getSupportActionBar()).hide();

        mobileNumber = getIntent().getStringExtra("mobileNumber");
        verificationId = getIntent().getStringExtra("verificationId");
        username = getIntent().getStringExtra("username");
        email = username + "@lnmiit.ac.in";

        LinearLayout rootLayout = findViewById(R.id.otp_verify_layout);
        TextView textMobile = findViewById(R.id.textMobileNumber);
        textMobile.setText(mobileNumber);

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        // Setup progress dialog
        progressDialog = new ProgressDialog(VerifyOTPActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        setUpOTPInputs();

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final TextView verifyBtn = findViewById(R.id.buttonVerify);

        // Add a touch listener to hide the keyboard when clicking outside the EditText fields
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // back button
        findViewById(R.id.backBtnVerify).setOnClickListener(view -> onBackPressed());

        verifyBtn.setOnClickListener(view -> {

            progressDialog.setMessage("Checking Verification Code...");
            progressDialog.show();

            String code1 = inputCode1.getText().toString().trim();
            String code2 = inputCode2.getText().toString().trim();
            String code3 = inputCode3.getText().toString().trim();
            String code4 = inputCode4.getText().toString().trim();
            String code5 = inputCode5.getText().toString().trim();
            String code6 = inputCode6.getText().toString().trim();

            code = code1+code2+code3+code4+code5+code6;

            if(code1.isEmpty() || code2.isEmpty() || code3.isEmpty() || code4.isEmpty() || code5.isEmpty() || code6.isEmpty()){
                Toast.makeText(VerifyOTPActivity.this, "Please enter valid code!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }

            PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationId,
                    code
            );
            FirebaseAuthManager.getInstance().signInWithCredential(phoneAuthCredential)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        verifyBtn.setVisibility(View.VISIBLE);
                        if(task.isSuccessful()){
                            // update the mobile number
                            progressDialog.dismiss();
                            showPasswordInputDialog();
                        }
                        else{
                            Toast.makeText(VerifyOTPActivity.this, "The verification code entered is wrong!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        });

        findViewById(R.id.textResendOTP).setOnClickListener(view -> PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber,
                60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential){

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e){
                        Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken){
                        verificationId = newVerificationId;
                        Toast.makeText(VerifyOTPActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                    }
                }
        ));

    }

    @Override
    public void onBackPressed() {
        // Call super.onBackPressed() to allow the default back button behavior
        super.onBackPressed();

        // Clear the activity stack and start the LoginActivity as the new task
        Intent intent = new Intent(VerifyOTPActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setUpOTPInputs(){
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable charSequence) {}
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable charSequence) {}
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable charSequence) {}
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable charSequence) {}
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable charSequence) {}
        });

    }

    private void showPasswordInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyOTPActivity.this);
        builder.setTitle("Confirm Password");

        final EditText input = new EditText(VerifyOTPActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString().trim();
            if (!TextUtils.isEmpty(password)) {
                reAuthenticateUser(password);
            } else {
                Toast.makeText(VerifyOTPActivity.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void reAuthenticateUser(String password) {
        FirebaseAuth.getInstance().signOut();

        // Show a ProgressDialog while re-authenticating
        progressDialog.setMessage("Re-authenticating...");
        progressDialog.show();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(signInTask -> {
                    if (signInTask.isSuccessful()) {
                        progressDialog.dismiss();
                        updateMobileNumber();
                    } else {
                        // Sign-in with new credentials failed, handle the error
                        Exception exception = signInTask.getException();
                        if (exception != null) {
                            Toast.makeText(VerifyOTPActivity.this, "Re-authentication failed : Wrong Password", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }
    private void updateMobileNumber() {
        String newPhoneNumber = mobileNumber;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("phoneNumber", newPhoneNumber);

        databaseReference.updateChildren(hashMap)
                .addOnCompleteListener(unused -> {
                    progressDialog.dismiss();

                    // Update the user's information in LoadUserData singleton
                    LoadUserData loadUserData = LoadUserData.getInstance();
                    loadUserData.updateUserPhoneNumber(newPhoneNumber);

                    // Update the user's information in UserInfo singleton
                    UserInfo userInfo = UserInfo.getInstance();
                    userInfo.setPhoneNumber(newPhoneNumber);

                    Intent intent = new Intent(VerifyOTPActivity.this, PrivacySecurity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }


}