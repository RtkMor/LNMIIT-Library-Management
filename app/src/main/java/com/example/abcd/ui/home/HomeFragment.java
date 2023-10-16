package com.example.abcd.ui.home;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.abcd.R;
import com.example.abcd.ui.explore.BookClass;
import com.example.abcd.ui.explore.MyBookAdapter;
import com.example.abcd.user_data.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private TextView popularButton, alphabeticalButton, randomButton;
    private Boolean popular = true;
    private Boolean alphabetical = false;
    private Boolean random = false;
    private List<BookClass> bookList;
    private MyBookAdapter adapter;

    private List<String> imageUrls; // List to store image URLs
    private ImageAdapter imageAdapter; // Adapter for images
    private LinearLayout customIndicator;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ViewModel
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set up buttons
        popularButton = rootView.findViewById(R.id.popularButton);
        alphabeticalButton = rootView.findViewById(R.id.alphabeticalButton);
        randomButton = rootView.findViewById(R.id.randomButton);

        // Set up click listeners
        setPopularButtonClickListener();
        setAlphabeticalButtonClickListener();
        setRandomButtonClickListener();

        // Set User Name
        TextView homeNameTv = rootView.findViewById(R.id.homeName);
        String name = UserInfo.getInstance().getName();
        homeNameTv.setText(name);

        // Initialize Recycler View
        RecyclerView recyclerView = rootView.findViewById(R.id.homeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize bookList and adapter
        bookList = new ArrayList<>();
        adapter = new MyBookAdapter(requireContext(), bookList);
        recyclerView.setAdapter(adapter);

        // Initialize Database Reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");

        // Initialize RecyclerView for images
        RecyclerView imageRecyclerView = rootView.findViewById(R.id.newArrivalsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        imageRecyclerView.setLayoutManager(layoutManager);

        // Add a SnapHelper for snapping behavior
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(imageRecyclerView);

        // Initialize imageList and adapter
        imageUrls = new ArrayList<>();
        imageAdapter = new ImageAdapter(requireContext(), imageUrls, position -> {
            // Handle image click event, if needed
        });
        imageRecyclerView.setAdapter(imageAdapter);

        // Initialize the custom indicator
        customIndicator = rootView.findViewById(R.id.customIndicator);

        // Fetch image URLs from Firebase Storage
        fetchImageUrls();

        // Set an OnScrollListener to update the custom indicator based on the current visible item
        imageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                // Update the custom indicator based on the first visible item
                updateCustomIndicator(customIndicator, firstVisibleItem);
            }
        });

        // Attach a ValueEventListener to the 'Favorites' node
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookList.clear(); // Clear the existing list
                for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                    String bookId = bookSnapshot.getKey();
                    if (bookId != null) {
                        // Fetch book details from the 'Books' node based on the book ID
                        DatabaseReference bookDetailsRef = databaseReference.child(bookId);
                        bookDetailsRef.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot bookDataSnapshot) {
                                if (bookDataSnapshot.exists()) {
                                    BookClass book = bookDataSnapshot.getValue(BookClass.class);
                                    if (book != null) {
                                        bookList.add(book);
                                        // Notify the adapter when an item is added
                                        adapter.notifyDataSetChanged();
                                        // Reorder the dataset by popularity
                                        reorderDatasetByPopularity();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                                Toast.makeText(requireActivity(), "Database error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(requireActivity(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial sorting by popularity
        reorderDatasetByPopularity();

        return rootView;
    }

    private void setPopularButtonClickListener() {
        popularButton.setOnClickListener(v -> {
            popular = true;
            alphabetical = false;
            random = false;
            updateSearchOptionsVisibility();
        });
    }

    private void setAlphabeticalButtonClickListener() {
        alphabeticalButton.setOnClickListener(v -> {
            popular = false;
            alphabetical = true;
            random = false;
            updateSearchOptionsVisibility();
        });
    }

    private void setRandomButtonClickListener() {
        randomButton.setOnClickListener(v -> {
            popular = false;
            alphabetical = false;
            random = true;
            updateSearchOptionsVisibility();
        });
    }

    private void updateSearchOptionsVisibility() {
        updateButtonUI(popular, popularButton);
        updateButtonUI(alphabetical, alphabeticalButton);
        updateButtonUI(random, randomButton);

        // Reorder the bookList based on the selected sorting criteria
        if (popular) {
            reorderDatasetByPopularity();
        } else if (alphabetical) {
            reorderDatasetAlphabetically();
        } else if (random) {
            reorderDatasetRandomly();
        }
    }

    private void updateButtonUI(boolean selected, TextView button) {
        int backgroundColor;
        int textColor;

        if (selected) {
            backgroundColor = R.color.black;
            textColor = R.color.white;
        } else {
            backgroundColor = R.color.white;
            textColor = R.color.black;
        }

        button.setBackgroundResource(backgroundColor);
        button.setTextColor(ContextCompat.getColor(requireContext(), textColor));
    }

    @SuppressLint("NotifyDataSetChanged")
    // Modify fetchImageUrls to call initCustomIndicator with the correct itemCount
    private void fetchImageUrls() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("NewArrivals");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                // Get download URL for each item
                item.getDownloadUrl().addOnCompleteListener(task -> {
                    if (isAdded()) { // Check if the fragment is still attached
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if (downloadUri != null) {
                                imageUrls.add(downloadUri.toString());
                                imageAdapter.notifyDataSetChanged();

                                if (imageUrls != null) {
                                    // Update ViewModel with imageUrls
                                    homeViewModel.setImageUrls(imageUrls);
                                    // Initialize the custom indicator with the actual itemCount
                                    initCustomIndicator(imageUrls.size());
                                }
                            }
                        } else {
                            // Handle errors
                            Exception exception = task.getException();
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }


    // Function to initialize the custom indicator with the actual itemCount
    private void initCustomIndicator(int itemCount) {
        // Clear existing views
        customIndicator.removeAllViews();

        for (int i = 0; i < itemCount; i++) {
            View dot = new View(requireContext());
            int dotSize = getResources().getDimensionPixelSize(R.dimen.dot_size); // Define dot size in resources
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(8, 0, 8, 0); // Adjust margin as needed
            dot.setLayoutParams(params);
            if(i==0){
                dot.setBackgroundResource(R.drawable.dot_active); // Create a drawable for the dot
            }
            else{
                dot.setBackgroundResource(R.drawable.dot_inactive); // Create a drawable for the dot
            }

            customIndicator.addView(dot);
        }
    }

    // Function to update the custom indicator based on the current visible item
    private void updateCustomIndicator(LinearLayout customIndicator, int currentPosition) {
        for (int i = 0; i < customIndicator.getChildCount(); i++) {
            View dot = customIndicator.getChildAt(i);
            // Set the background resource for the current visible item
            dot.setBackgroundResource(i == currentPosition ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    // Function to reorder the dataset by popularity
    @SuppressLint("NotifyDataSetChanged")
    private void reorderDatasetByPopularity() {
        bookList.sort((book1, book2) -> {
            // Compare books by their ratings in descending order
            return Double.compare(book2.getRating(), book1.getRating());
        });
        adapter.notifyDataSetChanged();
    }

    // Function to reorder the dataset alphabetically
    @SuppressLint("NotifyDataSetChanged")
    private void reorderDatasetAlphabetically() {
        bookList.sort((book1, book2) -> {
            // Compare books by their titles alphabetically
            return book1.getTitle().compareToIgnoreCase(book2.getTitle());
        });
        adapter.notifyDataSetChanged();
    }

    // Function to reorder the dataset randomly
    @SuppressLint("NotifyDataSetChanged")
    private void reorderDatasetRandomly() {
        Collections.shuffle(bookList);
        adapter.notifyDataSetChanged();
    }

    // On Destroy
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}