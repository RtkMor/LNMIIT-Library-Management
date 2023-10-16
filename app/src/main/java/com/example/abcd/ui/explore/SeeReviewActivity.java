package com.example.abcd.ui.explore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.abcd.ui.profile.OtherProfileActivity;
import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeeReviewActivity extends AppCompatActivity implements MyReviewAdapter.OnItemClickListener {

    private List<ReviewClass> reviewList;
    private MyReviewAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_review);
        Objects.requireNonNull(getSupportActionBar()).hide();

        RecyclerView recyclerView = findViewById(R.id.reviewsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize reviewList and adapter
        reviewList = new ArrayList<>();
        adapter = new MyReviewAdapter(reviewList, this);

        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String bookId = intent.getStringExtra("bookId");
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(bookId);

        // Attach a ValueEventListener to the database reference to fetch reviews
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewList.clear(); // Clear the existing list

                // Iterate through the children of the bookId node (each child corresponds to a user's review)
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Deserialize the user's review data
                    String userId = userSnapshot.getKey(); // Get the user's ID (username)
                    String reviewText = userSnapshot.child("userReview").getValue(String.class);
                    Float rating = userSnapshot.child("userRating").getValue(Float.class);

                    // Now, fetch the user's profile picture URL from the "Users" node
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                            if (userDataSnapshot.exists()) {
                                // Deserialize the user data and get the profile picture URL
                                String profilePictureUrl = userDataSnapshot.child("profileImage").getValue(String.class);

                                // Create a ReviewClass instance with the retrieved data
                                ReviewClass review = new ReviewClass(userId, reviewText, profilePictureUrl, rating);

                                // Add the review to the list
                                reviewList.add(review);

                                // Notify adapter of data change
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            Toast.makeText(SeeReviewActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(SeeReviewActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        findViewById(R.id.backBtnSeeReview).setOnClickListener(v -> finish());
    }

    @Override
    public void onItemClick(String username) {
        Intent intent = new Intent(this, OtherProfileActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }

}
