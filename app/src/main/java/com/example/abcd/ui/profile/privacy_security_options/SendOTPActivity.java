package com.example.abcd.ui.profile.privacy_security_options;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abcd.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {

    private String username;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_send);

        Objects.requireNonNull(getSupportActionBar()).hide();

        LinearLayout rootLayout = findViewById(R.id.otp_send_layout);
        EditText inputMobile = findViewById(R.id.inputMobile);
        TextView buttonGetOTP = findViewById(R.id.buttonGetOTP);

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        String initialMobileNumber = getIntent().getStringExtra("initialMobileNumber");

        username = getIntent().getStringExtra("username");

        // Add a touch listener to hide the keyboard when clicking outside the inputMobile EditText
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

        // get otp button
        buttonGetOTP.setOnClickListener(view -> {
            String newMobileNumber = inputMobile.getText().toString().trim();

            if(newMobileNumber.isEmpty()){
                Toast.makeText(SendOTPActivity.this,"Enter mobile number!", Toast.LENGTH_SHORT).show();
            }
            else if(initialMobileNumber.equals(newMobileNumber)){
                Toast.makeText(SendOTPActivity.this,"Enter new mobile number!", Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);
                buttonGetOTP.setVisibility(View.INVISIBLE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        newMobileNumber,
                        60,
                        TimeUnit.SECONDS,
                        SendOTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential){
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e){
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                                Toast.makeText(SendOTPActivity.this, "The country code or the mobile number is invalid!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken){
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);

                                Intent intent = new Intent(SendOTPActivity.this,VerifyOTPActivity.class);
                                intent.putExtra("mobileNumber", newMobileNumber);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("username", username);

                                startActivity(intent);
                                finish();
                            }
                        }
                );
            }
        });

        // back button
        findViewById(R.id.backBtnOTPSend).setOnClickListener(view -> finish());
    }
}