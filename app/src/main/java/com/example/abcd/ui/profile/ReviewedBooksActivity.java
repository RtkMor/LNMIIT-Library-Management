package com.example.abcd.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReviewedBooksActivity extends AppCompatActivity {

    private List<Map<String, Object>> reviewList;
    private MyReviewedBookAdapter adapter;
    private LinearLayout zeroItemsCountLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewed_books);
        Objects.requireNonNull(getSupportActionBar()).hide();

        RecyclerView recyclerView = findViewById(R.id.reviewedBooksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        zeroItemsCountLayout = findViewById(R.id.zeroItemsCountReviewed);

        reviewList = new ArrayList<>();
        adapter = new MyReviewedBookAdapter(this,reviewList);
        recyclerView.setAdapter(adapter);

        String username = getIntent().getStringExtra("username");
        DatabaseReference userReviewsRef = FirebaseDatabase.getInstance().getReference("ReviewedBooks").child(username);

        // Attach a ValueEventListener to the user's reviews
        userReviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userReviewsSnapshot) {
                reviewList.clear();
                if (userReviewsSnapshot.exists() && userReviewsSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot bookIdSnapshot : userReviewsSnapshot.getChildren()) {
                        String bookId = bookIdSnapshot.getKey();
                        if (bookId != null) {
                            // Fetch book details (title and author) based on the book ID
                            DatabaseReference bookDetailsRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
                            bookDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot bookDataSnapshot) {
                                    if (bookDataSnapshot.exists()) {
                                        String bookTitle = bookDataSnapshot.child("title").getValue(String.class);
                                        String bookAuthor = bookDataSnapshot.child("author").getValue(String.class);

                                        // Now, fetch the user's review for this book
                                        DatabaseReference userReviewRef = FirebaseDatabase.getInstance().getReference("Reviews").child(bookId).child(username);
                                        userReviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @SuppressLint("NotifyDataSetChanged")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userReviewSnapshot) {
                                                if (userReviewSnapshot.exists()) {
                                                    Map<String, Object> reviewData = new HashMap<>();
                                                    reviewData.put("bookId", bookId);
                                                    reviewData.put("bookTitle", bookTitle);
                                                    reviewData.put("bookAuthor", bookAuthor);
                                                    reviewData.put("userRating", userReviewSnapshot.child("userRating").getValue(Float.class));
                                                    reviewData.put("userReview", userReviewSnapshot.child("userReview").getValue(String.class));
                                                    reviewList.add(reviewData);
                                                    adapter.notifyDataSetChanged();

                                                    // Show "No Books Found" layout when the list is empty
                                                    if (reviewData.isEmpty()) {
                                                        zeroItemsCountLayout.setVisibility(View.VISIBLE);
                                                    } else {
                                                        zeroItemsCountLayout.setVisibility(View.GONE);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle database error for user's review
                                                Toast.makeText(ReviewedBooksActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle database error for book details
                                    Toast.makeText(ReviewedBooksActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    // No data or child nodes exist under 'ReviewedBooks' for the given user
                    zeroItemsCountLayout.setVisibility(View.VISIBLE); // Show "No Books Found" layout
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReviewedBooksActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        findViewById(R.id.backBtnReviewedBooks).setOnClickListener(v -> finish());
    }
}
