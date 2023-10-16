package com.example.abcd.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.example.abcd.ui.explore.BookDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class MyReviewedBookAdapter extends RecyclerView.Adapter<MyReviewedViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> reviewList;

    public MyReviewedBookAdapter(Context context, List<Map<String, Object>> reviewList) {
        this.reviewList = reviewList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyReviewedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_reviewed_book_item, parent, false);
        return new MyReviewedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewedViewHolder holder, int position) {
        Map<String, Object> reviewData = reviewList.get(position);

        String bookId = reviewData.get("bookId").toString();
        String bookTitle = reviewData.get("bookTitle").toString();
        String bookAuthor = reviewData.get("bookAuthor").toString();
        float userRating = Float.parseFloat(reviewData.get("userRating").toString());
        String userReview = reviewData.get("userReview").toString();

        holder.recTitle.setText(bookTitle);
        holder.recAuthor.setText(bookAuthor);
        holder.recUserRating.setRating(userRating);
        holder.recUserReview.setText(userReview);

        holder.recCard.setOnClickListener(view -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books").child(bookId);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the book details from the database
                        String isbn = dataSnapshot.child("isbn").getValue(String.class);
                        String accountNumber = dataSnapshot.child("accountNumber").getValue(String.class);
                        String callNumber = dataSnapshot.child("callNumber").getValue(String.class);
                        float rating = dataSnapshot.child("rating").getValue(Float.class);
                        int ratingNumber = dataSnapshot.child("ratingNumber").getValue(int.class);

                        // Create an intent and pass the fetched values as extras
                        Intent intent = new Intent(context, BookDetailsActivity.class);
                        intent.putExtra("bookId", bookId);
                        intent.putExtra("title", bookTitle);
                        intent.putExtra("author", bookAuthor);
                        intent.putExtra("isbn", isbn);
                        intent.putExtra("accountNumber", accountNumber);
                        intent.putExtra("callNumber", callNumber);
                        intent.putExtra("rating", rating);
                        intent.putExtra("ratingNumber", ratingNumber);
                        context.startActivity(intent);
                    } else {
                        // Handle the case when the book data doesn't exist
                        Toast.makeText(context, "Book data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that may occur during the database query
                    Toast.makeText(context, "Error fetching book data", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}


class MyReviewedViewHolder extends RecyclerView.ViewHolder {
    TextView recTitle, recAuthor, recUserReview;
    RatingBar recUserRating;
    View recCard;

    public MyReviewedViewHolder(@NonNull View itemView) {
        super(itemView);

        recCard = itemView.findViewById(R.id.recReviewedBooksCard);
        recTitle = itemView.findViewById(R.id.revTitle);
        recAuthor = itemView.findViewById(R.id.revAuthor);
        recUserRating = itemView.findViewById(R.id.revRating);
        recUserReview = itemView.findViewById(R.id.revReview);
    }
}


