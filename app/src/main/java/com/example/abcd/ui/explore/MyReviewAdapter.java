package com.example.abcd.ui.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewViewHolder> {

    private final List<ReviewClass> reviewList;
    private final OnItemClickListener listener;

    public MyReviewAdapter(List<ReviewClass> reviewList, OnItemClickListener listener) {
        this.reviewList = reviewList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_review, parent, false);
        return new MyReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        ReviewClass review = reviewList.get(position);

        holder.recUsername.setText(review.getUsername());
        holder.recReview.setText(review.getReview());
        holder.recRatingBar.setRating(review.getRating());

        // Check if the profile picture URL is not null before loading it
        if (review.getProfilePictureUrl() != null && !review.getProfilePictureUrl().isEmpty()) {
            // Load user profile picture using Picasso
            Picasso.get().load(review.getProfilePictureUrl()).into(holder.recImageView);
        } else {
            // Load a default profile image when the URL is null or empty
            Picasso.get().load(R.drawable.profile).into(holder.recImageView);
        }

        // Set click listeners for profile image and username
        holder.recImageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(review.getUsername());
            }
        });

        holder.recUsername.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(review.getUsername());
            }
        });
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String username);
    }
}

class MyReviewViewHolder extends RecyclerView.ViewHolder {
    ImageView recImageView;
    TextView recUsername, recReview;
    RatingBar recRatingBar;

    public MyReviewViewHolder(@NonNull View itemView) {
        super(itemView);

        recUsername = itemView.findViewById(R.id.recUsername);
        recReview = itemView.findViewById(R.id.recReview);
        recImageView = itemView.findViewById(R.id.recImageView);
        recRatingBar = itemView.findViewById(R.id.recRatingBar);
    }
}
