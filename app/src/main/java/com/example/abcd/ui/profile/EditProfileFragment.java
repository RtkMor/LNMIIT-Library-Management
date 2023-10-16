package com.example.abcd.ui.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.abcd.MainActivity;
import com.example.abcd.R;
import com.example.abcd.user_data.UserInfo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditProfileFragment extends Fragment {

    private EditText myProfileName;
    private EditText myProfileBio;
    private ImageView myProfileImage;
    private Uri imageUri = null;
    private ProgressDialog progressDialog;
    private String newName;
    private String newBio;
    private String username = "";
    private final YourActivityResultContract galleryActivityResultContract = new YourActivityResultContract();
    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher =
            registerForActivityResult(galleryActivityResultContract, this::onImageActivityResult);

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Add an OnTouchListener to the root view
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Check if the touch event is outside of the current fragment's view
                if (!isPointInsideView(event.getRawX(), event.getRawY(), rootView)) {
                    hideKeyboard();
                }
            }
            return false;

        });

        // Initialize UI elements
        myProfileName = rootView.findViewById(R.id.settings_edit_text_name);
        myProfileBio = rootView.findViewById(R.id.edit_text_bio);
        TextView myProfileEmail = rootView.findViewById(R.id.settings_edit_email_address);
        myProfileImage = rootView.findViewById(R.id.settings_edit_profile_image);

        // Update UI elements with user data from the UserInfo singleton
        UserInfo userInfo = UserInfo.getInstance();
        myProfileName.setText(userInfo.getName());
        myProfileBio.setText(userInfo.getBio());
        myProfileEmail.setText(userInfo.getEmail());

        String email = userInfo.getEmail();
        int ind = email.indexOf('@');
        username = email.substring(0,ind);

        if (!TextUtils.isEmpty(userInfo.getProfileImage())) {
            Glide.with(requireContext())
                    .load(userInfo.getProfileImage())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(myProfileImage);
        }

        // Setup progress dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Add click listener to nameEditIcon
        ImageView nameEditIcon = rootView.findViewById(R.id.nameEditIcon);
        nameEditIcon.setOnClickListener(v -> {
            myProfileName.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(myProfileName, InputMethodManager.SHOW_IMPLICIT);
        });

        // Add click listener to bioEditIcon
        ImageView bioEditIcon = rootView.findViewById(R.id.bioEditIcon);
        bioEditIcon.setOnClickListener(v -> {
            myProfileBio.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(myProfileBio, InputMethodManager.SHOW_IMPLICIT);
        });

        // Handle save button
        ImageView saveButton = rootView.findViewById(R.id.settings_save_profile_button);
        saveButton.setOnClickListener(v -> updateData());

        // Handle image click
        myProfileImage.setOnClickListener(v -> pickImageGallery());

        // Add an OnTouchListener to the root view
        rootView.setOnTouchListener((v, event) -> {
            // Check if the event is a touch event outside of an EditText
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focusedView = requireActivity().getCurrentFocus();
                if (focusedView instanceof EditText) {
                    Rect outRect = new Rect();
                    focusedView.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        // The touch is outside of the EditText, hide the keyboard
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                        focusedView.clearFocus(); // Clear focus from the EditText
                    }
                }
            }
            return false;
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (requireActivity() instanceof SettingsActivity) {
            ((SettingsActivity) requireActivity()).showProfileSections();
        }
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private void hideKeyboard() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void onImageActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                uploadImage();
                myProfileImage.setImageURI(imageUri);
            }
        }
    }

    public static class YourActivityResultContract extends ActivityResultContract<Intent, ActivityResult> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Intent input) {
            // Create and return the intent to start the activity
            return input;
        }

        @Override
        public ActivityResult parseResult(int resultCode, @Nullable Intent intent) {
            // Process the activity result and return an ActivityResult object
            return new ActivityResult(resultCode, intent);
        }
    }

    private void updateData() {
        newName = myProfileName.getText().toString().trim();
        newBio = myProfileBio.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getActivity(), "Enter Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(newBio)) {
            Toast.makeText(getActivity(), "Enter Bio", Toast.LENGTH_SHORT).show();
        } else {
            updateProfile();
        }
    }

    private void uploadImage() {
        progressDialog.setMessage("Updating Profile Image");
        progressDialog.show();

        String filePath = "ProfileImages/" + username;

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePath);
        reference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnSuccessListener(uri -> {
                        String uploadedImageUrl = uri.toString();

                        // Update the user's profile image URL in the database
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(username);
                        databaseReference.child("profileImage").setValue(uploadedImageUrl);

                        // Update the user's profile image URL in the UserInfo singleton
                        UserInfo userInfo = UserInfo.getInstance();
                        userInfo.setProfileImage(uploadedImageUrl);

                        progressDialog.dismiss();

                        // Create an Intent with flags to clear the back stack and start a new task
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("profileUpdated", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    });
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private void updateProfile() {
        progressDialog.setMessage("Updating user profile...");
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", newName);
        hashMap.put("bio", newBio);

        // Update user information in the database
        databaseReference.child(username)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    // Update the user's information in UserInfo singleton
                    UserInfo userInfo = UserInfo.getInstance();
                    userInfo.setName(newName);
                    userInfo.setBio(newBio);
                    progressDialog.dismiss();

                    // Create an Intent with flags to clear the back stack and start a new task
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("profileUpdated", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

}
