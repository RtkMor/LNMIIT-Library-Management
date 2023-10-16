package com.example.abcd.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.abcd.user_data.FirebaseAuthManager;
import com.example.abcd.user_data.LoadUserInformation;
import com.example.abcd.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class splash_video extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_video);

        Objects.requireNonNull(getSupportActionBar()).hide();

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoUri);

        // start video
        videoView.start();

        // init firebase auth
        firebaseAuth = FirebaseAuthManager.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // Remove background color from the layout
        findViewById(R.id.videoView).setBackground(null);

        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            assert email != null;
            // Add a listener to detect when the video finishes playing
            videoView.setOnCompletionListener(mediaPlayer -> checkUser());
        } else {
            videoView.setOnCompletionListener(mediaPlayer -> {
                // Start the login activity after the video finishes playing
                startActivity(new Intent(splash_video.this, LoginActivity.class));
                finish();
            });
        }

    }
    private void checkUser() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        assert firebaseUser != null;
        if(firebaseUser.isEmailVerified()) {
            Intent intent = new Intent(splash_video.this, LoadUserInformation.class);
            startActivity(intent);
            finish();
        }
        else{
            startActivity(new Intent(splash_video.this, LoginActivity.class));
            finish();
        }

    }
}




