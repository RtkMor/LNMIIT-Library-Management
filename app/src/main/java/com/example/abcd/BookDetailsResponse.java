package com.example.abcd;

import com.google.gson.annotations.SerializedName;

public class BookDetailsResponse {
    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public static class VolumeInfo {
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;

        @SerializedName("description")
        private String description;

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;

        public String getThumbnail() {
            return thumbnail;
        }
    }
}
