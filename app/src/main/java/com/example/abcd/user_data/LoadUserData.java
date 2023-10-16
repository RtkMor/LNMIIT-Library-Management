package com.example.abcd.user_data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoadUserData {

    private final DatabaseReference databaseReference;

    private LoadUserData() {
        FirebaseAuth auth = FirebaseAuthManager.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                int atIndex = email.indexOf('@');
                String username = email.substring(0, atIndex);

                // Initialize the Firebase Realtime Database reference for the user based on username
                databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(username);
            } else {
                throw new IllegalArgumentException("Email is null");
            }
        } else {
            throw new IllegalStateException("Current user is null");
        }
    }

    // Singleton getInstance method
    public static LoadUserData getInstance() {
        return new LoadUserData();
    }

    public void loadUserInfo(UserInfoLoadListener listener) {
        // Callback with the loaded user information
        ValueEventListener userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String profileImage = dataSnapshot.child("profileImage").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                    UserInfo userInfo = UserInfo.getInstance();
                    userInfo.setEmail(email);
                    userInfo.setUsername(username);
                    userInfo.setName(name);
                    userInfo.setProfileImage(profileImage);
                    userInfo.setBio(bio);
                    userInfo.setPhoneNumber(phoneNumber);
                    // Callback with the loaded user information
                    listener.onUserInfoLoaded(userInfo);
                } else {
                    // Handle the case when no data exists for this user
                    listener.onError("No data exists for this user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error, if needed, and log the error message
                String errorMessage = databaseError.getMessage();
                listener.onError(errorMessage);
                Log.e("LoadUserData", "Database error: " + errorMessage);
            }
        };

        databaseReference.addValueEventListener(userValueEventListener);
    }

    public void updateUserPhoneNumber(String newPhoneNumber) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("phoneNumber", newPhoneNumber);

        databaseReference.updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> {
                    // Handle the successful update here if needed
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to update here and log the error message
                    String errorMessage = e.getMessage();
                    Log.e("LoadUserData", "Update error: " + errorMessage);
                });
    }

    public interface UserInfoLoadListener {
        void onUserInfoLoaded(UserInfo userInfo);
        void onError(String errorMessage);
    }
}
