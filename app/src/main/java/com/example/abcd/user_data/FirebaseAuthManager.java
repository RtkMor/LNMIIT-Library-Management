package com.example.abcd.user_data;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthManager {
    private static FirebaseAuth instance;

    public static FirebaseAuth getInstance() {
        if (instance == null) {
            instance = FirebaseAuth.getInstance();
        }
        return instance;
    }

    public static void logOut() {
        instance.signOut();
        instance = null; // Clear the instance
    }
}
