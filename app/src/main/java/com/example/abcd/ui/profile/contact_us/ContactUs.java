package com.example.abcd.ui.profile.contact_us;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abcd.R;
import com.example.abcd.databinding.ActivityContactUsBinding;
import com.example.abcd.user_data.UserInfo;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContactUs extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityContactUsBinding binding = ActivityContactUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        LinearLayout rootLayout = findViewById(R.id.contactUsLayout);
        TextInputLayout subjectEt = findViewById(R.id.subjectEt);
        TextInputLayout descriptionEt = findViewById(R.id.descriptionEt);
        TextView instagramLink = findViewById(R.id.instagramLink);

        instagramLink.setOnClickListener(v -> {
            // URL of the Instagram profile
            String instagramUrl = "https://www.instagram.com/rtk.mor/";

            // Create an Intent to open the URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));

            // Check if the Instagram app is installed
            intent.setPackage("com.instagram.android");

            try {
                startActivity(intent);
            } catch (Exception e) {
                // If Instagram app is not installed, open in a web browser
                intent.setPackage(null);
                startActivity(intent);
            }
        });

        // Contact Us Form Send Button
        findViewById(R.id.buttonSend).setOnClickListener(v -> {

            String userSubject = Objects.requireNonNull(subjectEt.getEditText()).getText().toString();
            String userDescription = Objects.requireNonNull(descriptionEt.getEditText()).getText().toString();

            if (userSubject.isEmpty()){
                Toast.makeText(ContactUs.this,"Please fill subject!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDescription.isEmpty()){
                Toast.makeText(ContactUs.this,"Please fill description!",Toast.LENGTH_SHORT).show();
                return;
            }

            // Save data in the database
            submitContactUs(userSubject,userDescription);

            // Set text to empty
            subjectEt.getEditText().setText("");
            descriptionEt.getEditText().setText("");
        });

        // Set an OnTouchListener to the root layout to hide the keyboard
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
        findViewById(R.id.backBtnContactUs).setOnClickListener(v -> finish());
    }

    private void submitContactUs(String userSubject, String userDescription) {
        DatabaseReference contactUsRef = FirebaseDatabase.getInstance().getReference("ContactUs");
        String userId = UserInfo.getInstance().getUsername();

        // Generate a unique key for the new submission
        String submissionKey = contactUsRef.push().getKey();

        // Create a new node with the unique key for this submission
        DatabaseReference newSubmissionRef = contactUsRef.child(submissionKey);

        Map<String, Object> contactUsData = new HashMap<>();
        contactUsData.put("user", userId);
        contactUsData.put("subject", userSubject);
        contactUsData.put("description", userDescription);

        // Set the data for the new submission
        newSubmissionRef.setValue(contactUsData)
                .addOnSuccessListener(aVoid -> Toast.makeText(ContactUs.this, "Request submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(ContactUs.this, "Error while sending the request!", Toast.LENGTH_SHORT).show();
                });
    }

}