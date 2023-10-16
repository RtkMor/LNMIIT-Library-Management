package com.example.abcd.ui.explore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ExploreFragment extends Fragment {

    private LinearLayout searchOptions;
    private boolean searchOptionsVisible = false;
    private boolean title = true;
    private boolean author = false;
    private boolean isbn = false;
    private TextView titleButton, authorButton, isbnButton;
    private ImageView dropBtn;
    private EditText searchEditText;
    private List<BookClass> bookList;
    private MyBookAdapter adapter;
    private DatabaseReference databaseReference;
    private final HashSet<String> bookIdsSet = new HashSet<>();
    private LinearLayout zeroItemsCountLayout;

    @SuppressLint({"ClickableViewAccessibility", "MissingInflatedId"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).hide();

        // UI elements
        searchEditText = rootView.findViewById(R.id.searchEditText);
        searchOptions = rootView.findViewById(R.id.searchOptions);
        searchOptions.setVisibility(View.GONE);
        titleButton = rootView.findViewById(R.id.titleButton);
        authorButton = rootView.findViewById(R.id.authorButton);
        isbnButton = rootView.findViewById(R.id.isbnButton);
        dropBtn = rootView.findViewById(R.id.dropBtn);
        ImageView searchBtn = rootView.findViewById(R.id.searchButton);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        zeroItemsCountLayout = rootView.findViewById(R.id.zeroItemsCount);


        // search bar implementation to hide and show the options + keyboard
        setDropBtnEditTextClickListener();
        setRootViewTouchListener(rootView);
        setTitleButtonClickListener();
        setAuthorButtonClickListener();
        setIsbnButtonClickListener();

        // recycler view and adapter
        bookList = new ArrayList<>();

        // Set the initial state of the adapter with an empty list
        adapter = new MyBookAdapter(getContext(),bookList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Books");

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString().trim().toUpperCase();
                // Check if the search input is empty
                if (searchText.isEmpty()) {
                    // Clear the search and update the RecyclerView with an empty list
                    updateRecyclerViewWithEmptyList();
                } else {
                    // Perform a search query based on the selected option
                    performSearch(searchText);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        searchBtn.setOnClickListener(view -> {
            if(!searchEditText.getText().toString().isEmpty())
                performSearch(searchEditText.getText().toString().toUpperCase());
        });

        return rootView;
    }


    // Search Bar Related Implementation
    @SuppressLint("ClickableViewAccessibility")
    private void setRootViewTouchListener(View rootView) {
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }
    private void setDropBtnEditTextClickListener() {
        dropBtn.setOnClickListener(v -> {
            if (!searchOptionsVisible) {
                showSearchOptions();
                dropBtn.setImageResource(R.drawable.baseline_arrow_drop_up_white_24);
            } else {
                // If you want to change the image when hiding the search options
                hideSearchOptions();
                dropBtn.setImageResource(R.drawable.baseline_arrow_drop_down_white_24);
            }
        });
    }
    private void setTitleButtonClickListener() {
        titleButton.setOnClickListener(v -> {
            title = true;
            author = false;
            isbn = false;
            updateSearchOptionsVisibility();
        });
    }
    private void setAuthorButtonClickListener() {
        authorButton.setOnClickListener(v -> {
            title = false;
            author = true;
            isbn = false;
            updateSearchOptionsVisibility();
        });
    }
    private void setIsbnButtonClickListener() {
        isbnButton.setOnClickListener(v -> {
            title = false;
            author = false;
            isbn = true;
            updateSearchOptionsVisibility();
        });
    }
    private void showSearchOptions() {
        searchOptions.setVisibility(View.VISIBLE);
        searchOptionsVisible = true;
    }
    private void hideSearchOptions() {
        searchOptions.setVisibility(View.GONE);
        searchOptionsVisible = false;
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }
    private void updateSearchOptionsVisibility() {
        updateButtonUI(title, titleButton);
        updateButtonUI(author, authorButton);
        updateButtonUI(isbn, isbnButton);
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


    // perform Search
    private void performSearch(String searchText) {
        bookList.clear(); // Clear the existing list
        bookIdsSet.clear(); // Clear the existing set

        // Build the query based on the selected search option (e.g., title)
        Query query;
        if (title) {
            query = databaseReference.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff");
        } else if (author) {
            query = databaseReference.orderByChild("author").startAt(searchText).endAt(searchText + "\uf8ff");
        } else if (isbn) {
            query = databaseReference.orderByChild("isbn").equalTo(searchText);
        } else {
            // Default to searching by title if none of the options are selected
            query = databaseReference.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff");
        }

        // Execute the query
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String bookKey = itemSnapshot.getKey(); // Get the unique Firebase key
                    // Check if the book key is not in the set to avoid duplicates
                    if (!bookIdsSet.contains(bookKey)) {
                        BookClass bookClass = itemSnapshot.getValue(BookClass.class);
                        bookList.add(bookClass);
                        bookIdsSet.add(bookKey);
                    }
                }
                adapter.notifyDataSetChanged();

                // Show "No Books Found" layout when the list is empty
                if (bookList.isEmpty()) {
                    zeroItemsCountLayout.setVisibility(View.VISIBLE);
                } else {
                    zeroItemsCountLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRecyclerViewWithEmptyList() {
        bookList.clear();
        adapter.notifyDataSetChanged();
    }

    // On Destroy
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}