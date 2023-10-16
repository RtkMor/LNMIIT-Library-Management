package com.example.abcd.ui.explore;

public class ReviewClass {
    String username, review, profilePictureUrl;
    Float rating;

    public ReviewClass(String username, String review, String profilePictureUrl, Float rating){
        this.username = username;
        this.review = review;
        this.profilePictureUrl = profilePictureUrl;
        this.rating = rating;
    }

    public ReviewClass(){
        // Default constructor
    }

    // Getters
    public String getUsername() {
        return username;
    }
    public String getReview() {
        return review;
    }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public float getRating() { return rating; }
}
