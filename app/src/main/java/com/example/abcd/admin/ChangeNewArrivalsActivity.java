package com.example.abcd.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.abcd.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class ChangeNewArrivalsActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private ArrayList<Uri> chooseImageList;
    private ArrayList<String> imageUrlsList;

    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_new_arrivals);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Set UI elements
        Button uploadBtn = findViewById(R.id.uploadButton);
        RelativeLayout pickImageBtn = findViewById(R.id.chooseImageBtn);
        viewPager = findViewById(R.id.viewPager);

        // Set up array lists
        chooseImageList = new ArrayList<>();
        imageUrlsList = new ArrayList<>();

        // Setup progress dialog
        progressDialog = new ProgressDialog(ChangeNewArrivalsActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Pick Images
        pickImageBtn.setOnClickListener(view -> {
            checkPermissions();
            PickImageFromGallery();
        });

        // Upload Images
        uploadBtn.setOnClickListener(view -> uploadImages());

        // Back Button
        View backBtn = findViewById(R.id.backBtnChangeNewArrivals);
        backBtn.setOnClickListener(view -> finish());
    }

    private void uploadImages() {
        progressDialog.setMessage("Updating New Arrivals");
        progressDialog.show();

        clearFolder();

        final CountDownLatch latch = new CountDownLatch(chooseImageList.size());

        for (int i = 0; i < chooseImageList.size(); i++) {
            Uri individualImage = chooseImageList.get(i);
            if (individualImage != null) {
                StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("NewArrivals");
                final StorageReference imageName = imageFolder.child("Image" + i);
                imageName.putFile(individualImage).addOnSuccessListener(taskSnapshot -> imageName.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrlsList.add(uri.toString());
                    latch.countDown(); // Decrease the latch count
                    if (latch.getCount() == 0) {
                        // Store the links in Realtime Database
                        storeLinksInRealtimeDatabase(imageUrlsList);

                        // Reset ViewPager with an empty list
                        resetViewPager();

                        // Show a toast message
                        Toast.makeText(this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }));
            }
        }
    }

    private void storeLinksInRealtimeDatabase(ArrayList<String> imageUrlsList) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("NewArrivals");

        // Clear the existing data in the "NewArrivals" node
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Previous data removed successfully, now add the new data
                    databaseReference.setValue(imageUrlsList)
                            .addOnSuccessListener(aVoid1 -> {
                                // Handle successful storage of new image URLs in Realtime Database
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure to store new image URLs
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure to remove previous data
                });
    }


    private void clearFolder() {
        StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("NewArrivals");
        imageFolder.listAll()
                .addOnSuccessListener(listResult -> {
                    List<StorageReference> items = listResult.getItems();
                    for (StorageReference item : items) {
                        item.delete().addOnSuccessListener(aVoid -> {
                            // File deleted successfully
                        }).addOnFailureListener(exception -> {
                            // Handle any errors here
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                });
    }

    @SuppressLint("ObsoleteSdkInt")
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ChangeNewArrivalsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChangeNewArrivalsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            } else {
                PickImageFromGallery();
            }
        } else {
            PickImageFromGallery();
        }
    }

    private void PickImageFromGallery() {
        // Go to the gallery
        Intent intent = new Intent();
        intent.setType("image/*"); // Change "images/*" to "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri(); // Declare imageUri locally
                chooseImageList.add(imageUri);
                setAdapter(); // Corrected method name
            }
        }
    }

    private void setAdapter() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, chooseImageList);
        viewPager.setAdapter(adapter);
    }

    private void resetViewPager() {
        chooseImageList.clear();
        viewPager.setAdapter(null);
    }
}
