package com.example.abcd.user_data;

public class UserInfo {
    private String email;
    private String username;
    private String name;
    private String profileImage;
    private String bio;
    private String phoneNumber;

    // Private static variable to hold the single instance of UserInfo
    private static UserInfo instance;

    // Private constructor to prevent external instantiation
    private UserInfo() {
        // Initialization code, if needed
    }

    // Public static method to provide access to the single instance
    public static UserInfo getInstance() {
        if (instance == null) {
            // If the instance doesn't exist, create it
            instance = new UserInfo();
        }
        return instance;
    }

    // email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // profileImage
    public String getProfileImage() {
        return profileImage;
    }
    public void setProfileImage(String uploadedImageUrl) {
        this.profileImage = uploadedImageUrl;
    }

    // bio
    public String getBio() {
        return bio;
    }
    public void setBio(String newBio) {
        this.bio = newBio;
    }

    // phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Clear the user data on log out
    public void clearUserData() {
        email = null;
        username = null;
        name = null;
        profileImage = null;
        bio = null;
        phoneNumber = null;
        instance = null;
    }

}
