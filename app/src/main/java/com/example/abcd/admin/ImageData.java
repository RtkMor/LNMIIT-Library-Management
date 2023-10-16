package com.example.abcd.admin;

import android.net.Uri;

public class ImageData {
    private final Uri imageUri;

    public ImageData(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}

