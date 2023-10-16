package com.example.abcd.ui.explore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.abcd.R;
import com.example.abcd.user_data.UserInfo;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BookDetailsActivity extends AppCompatActivity {

    String currentUserId;
    private ImageView bookCoverImageView;
    private TextView descriptionTextView;
    ImageView favoriteBtn;
    String bookId;
    String previewLink;
    TextView ratingTextView;
    TextView ratingNumberTextView;
    RatingBar ratingBar;
    float rating;
    int ratingNumber;
    DatabaseReference userFavoritesRef;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize UI elements
        bookCoverImageView = findViewById(R.id.imageView);
        descriptionTextView = findViewById(R.id.descriptionTV);
        LinearLayout rootLayout = findViewById(R.id.bookDetailsLayout);
        TextView titleTextView = findViewById(R.id.nameTV);
        TextView authorTextView = findViewById(R.id.authorTV);
        TextView isbnTextView = findViewById(R.id.isbnTV);
        TextView accountNumberTextView = findViewById(R.id.accountNumberTV);
        TextView callNumberTextView = findViewById(R.id.callNumberTV);
        ratingTextView = findViewById(R.id.ratingTV);
        ratingNumberTextView = findViewById(R.id.ratingNumberTV);
        ratingBar = findViewById(R.id.ratingBar);

        // Rating
        RatingBar reviewRatingBar = findViewById(R.id.reviewRatingBar);
        TextInputLayout addReviewEt = findViewById(R.id.addReviewEt);
        TextView addReviewBtn = findViewById(R.id.addReviewBtn);

        // Receive data from the previous activity
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String isbn = intent.getStringExtra("isbn");
        String accountNumber = intent.getStringExtra("accountNumber");
        String callNumber = intent.getStringExtra("callNumber");
        rating = intent.getFloatExtra("rating", 0.0f);
        ratingNumber = intent.getIntExtra("ratingNumber", 0);

        // Retrieve the user information that was loaded at login time
        currentUserId = UserInfo.getInstance().getUsername();

        // Populate UI elements with received data
        titleTextView.setText(title);
        authorTextView.setText(author);
        isbnTextView.setText(isbn);
        accountNumberTextView.setText(accountNumber);
        callNumberTextView.setText(callNumber);
        ratingTextView.setText(String.valueOf(rating));
        ratingNumberTextView.setText(String.valueOf(ratingNumber));
        ratingBar.setRating(rating);

        // Fetch book details using Google Books API
//        fetchBookDetails(isbn);

        // Preview Link Listener
        bookCoverImageView.setOnClickListener(v -> {
            if (previewLink.isEmpty()) {
                // below toast message is displayed when preview link is not present.
                Toast.makeText(BookDetailsActivity.this, "No preview Link present", Toast.LENGTH_SHORT).show();
                return;
            }
            // if the link is present we are opening
            // that link via an intent.
            Uri uri = Uri.parse(previewLink);
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
        });

        // Favorite Button
        favoriteBtn = findViewById(R.id.favoriteBtn);
        checkIfBookIsFavorite(); // You need to implement this function
        favoriteBtn.setOnClickListener(view -> favorite());

        // Add Review Button
        addReviewBtn.setOnClickListener(view -> {
            // Get the user's review text and rating
            String userReview = Objects.requireNonNull(addReviewEt.getEditText()).getText().toString();
            float userRating = reviewRatingBar.getRating();

            if (TextUtils.isEmpty(userReview)) {
                Toast.makeText(this, "Please enter a review!", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (userRating==0) {
                Toast.makeText(this, "Please select rating!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update the book's rating and rating number in the database
            updateBookRating(userRating, userReview);
            // Set the edit text to empty
            addReviewEt.getEditText().setText("");
            reviewRatingBar.setRating(0);
        });

        // See Review Button
        View reviewBtn = findViewById(R.id.seeReviewsBtn);
        reviewBtn.setOnClickListener(view -> {
            Intent intent2 = new Intent(this, SeeReviewActivity.class);
            intent2.putExtra("bookId", bookId);
            startActivity(intent2);
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

        // Back Button
        findViewById(R.id.backBtnBookDetails).setOnClickListener(view -> finish());
    }


    // Add Review Function
    private void updateBookRating(float userRating, String userReview) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(bookId);
        String userId = UserInfo.getInstance().getUsername();

        reviewsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has already reviewed the book, update the existing review
                    for (DataSnapshot reviewSnapshot : dataSnapshot.getChildren()) {
                        String reviewKey = reviewSnapshot.getKey();
                        Review existingReview = reviewSnapshot.getValue(Review.class);

                        // Calculate the new rating based on the updated review
                        float userOldRating = existingReview.getUserRating();
                        float newRating = calculateNewRating(userOldRating, userRating);

                        // Update the existing review in the database
                        assert existingReview != null;
                        existingReview.setUserRating(userRating);
                        existingReview.setUserReview(userReview);
                        reviewsRef.child(reviewKey).setValue(existingReview);

                        // Format the new rating to two decimal places
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        newRating = Float.parseFloat(decimalFormat.format(newRating));

                        // Update the UI to reflect the new rating
                        ratingTextView.setText(String.valueOf(newRating));
                        ratingBar.setRating(newRating);

                        // Update the book's rating in the database
                        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
                        Map<String, Object> bookUpdateData = new HashMap<>();
                        bookUpdateData.put("rating", newRating);
                        bookRef.updateChildren(bookUpdateData);

                        Toast.makeText(BookDetailsActivity.this,"Book Reviewed Successfully!", Toast.LENGTH_SHORT).show();

                        // Update User Node Reviewed Books
                        updateReviewedBook();

                        break; // No need to continue if we found an existing review
                    }
                } else {
                    // User is reviewing the book for the first time
                    DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);

                    // Fetch the current rating and rating number of the book
                    bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                float currentRating = dataSnapshot.child("rating").getValue(Float.class);
                                long currentRatingNumber = dataSnapshot.child("ratingNumber").getValue(Long.class);

                                // Calculate the new rating and rating number
                                float newRating = ((currentRating * currentRatingNumber) + userRating) / (currentRatingNumber + 1);
                                long newRatingNumber = currentRatingNumber + 1;

                                // Format the new rating to two decimal places
                                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                newRating = Float.parseFloat(decimalFormat.format(newRating));

                                // Update the book's rating and rating number in the database
                                Map<String, Object> bookUpdateData = new HashMap<>();
                                bookUpdateData.put("rating", newRating);
                                bookUpdateData.put("ratingNumber", newRatingNumber);
                                bookRef.updateChildren(bookUpdateData);

                                // Create a new review
                                Review newReview = new Review(userId, userReview, userRating);

                                // Use userId as the key for the review
                                reviewsRef.child(userId).setValue(newReview);

                                // Update the UI to reflect the new rating and rating number
                                ratingTextView.setText(String.valueOf(newRating));
                                ratingNumberTextView.setText(String.valueOf(newRatingNumber));
                                ratingBar.setRating(newRating);

                                Toast.makeText(BookDetailsActivity.this,"Book Reviewed Successfully!", Toast.LENGTH_SHORT).show();

                                // Update User Node Reviewed Books
                                updateReviewedBook();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                            Toast.makeText(BookDetailsActivity.this, "Error updating book rating", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(BookDetailsActivity.this, "Error checking existing review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReviewedBook() {
        DatabaseReference userReviewedRef = FirebaseDatabase.getInstance().getReference("ReviewedBooks").child(currentUserId);

        // Set the value of the child node with the bookId
        userReviewedRef.child(bookId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    } else {
                        // Handle the error
                        Toast.makeText(BookDetailsActivity.this, "Error adding review", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Calculate the new rating based on the user's rating update
    private float calculateNewRating(float oldUserRating, float newUserRating) {
        // Calculate the new rating using the provided formula
        float newRating = ((rating * ratingNumber) - oldUserRating + newUserRating) / ratingNumber;

        // Format the new rating to two decimal places
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Float.parseFloat(decimalFormat.format(newRating));
    }

    // Favorite Btn Functions
    private void favorite() {
        userFavoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(currentUserId).child(bookId);
        userFavoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The book is currently a favorite, so remove it
                    userFavoritesRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red_24);
                            Toast.makeText(BookDetailsActivity.this, "Removed From Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle removal failure
                            Toast.makeText(BookDetailsActivity.this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // The book is not a favorite, so add it
                    userFavoritesRef.setValue(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            favoriteBtn.setImageResource(R.drawable.baseline_favorite_red_24); // Change the favorite button icon
                        } else {
                            // Handle addition failure
                            Toast.makeText(BookDetailsActivity.this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(BookDetailsActivity.this, "Error checking favorite status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfBookIsFavorite() {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(currentUserId).child(bookId);
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The book is in favorites, now check its value
                    Boolean isBookFavorite = dataSnapshot.getValue(Boolean.class);
                    if (isBookFavorite != null && isBookFavorite) {
                        // The book is a favorite
                        favoriteBtn.setImageResource(R.drawable.baseline_favorite_red_24); // Set the favorite icon
                    } else {
                        // The book is not a favorite
                        favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red_24); // Set the non-favorite icon
                    }
                } else {
                    // The book is not in favorites
                    favoriteBtn.setImageResource(R.drawable.baseline_favorite_border_red_24); // Set the non-favorite icon
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Toast.makeText(BookDetailsActivity.this, "Error checking favorite status", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Book Details Fetching
    private void fetchBookDetails(String isbn) {
        // Construct the URL for the Google Books API using the ISBN number
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        // Create a JSON object request using Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {
                    try {
                        // Extract book details from the JSON response
                        JSONArray items = response.getJSONArray("items");
                        if (items.length() > 0) {
                            JSONObject bookInfo = items.getJSONObject(0);
                            JSONObject volumeInfo = bookInfo.getJSONObject("volumeInfo");

                            // Get the book cover image URL and description
                            JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
                            String thumbnail = imageLinks.optString("thumbnail");
                            String description = volumeInfo.optString("description");
                            previewLink = volumeInfo.optString("previewLink");

                            // Use Picasso to load the book cover image
                            Picasso.get().load(thumbnail).into(bookCoverImageView);
                            // Set the description in the TextView
                            descriptionTextView.setText(description);

                        } else {
                            // Handle the case where no book details are found
                            Toast.makeText(BookDetailsActivity.this, "Book Image Not Found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                        Toast.makeText(BookDetailsActivity.this, "Book Image Not Found", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(BookDetailsActivity.this, "Book Image Not Found", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue to start fetching data
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}
