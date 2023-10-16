package com.example.abcd.ui.explore;

public class Review {
    private String userId;
    private String userReview;
    private float userRating;

    public Review() {
        // Default constructor required for Firebase
    }

    public Review(String userId, String userReview, float userRating) {
        this.userId = userId;
        this.userReview = userReview;
        this.userRating = userRating;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserReview() {
        return userReview;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserReview(String userReview) {
        this.userReview = userReview;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }
}

