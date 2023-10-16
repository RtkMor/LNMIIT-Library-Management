package com.example.abcd.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.abcd.ui.explore.BookClass;
import com.example.abcd.ui.explore.MyBookAdapter;
import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavoriteBookActivity extends AppCompatActivity {

    private List<BookClass> bookList;
    private MyBookAdapter adapter;

    private LinearLayout zeroItemsCountLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_book);

        Objects.requireNonNull(this.getSupportActionBar()).hide();

        RecyclerView recyclerView = findViewById(R.id.favoriteRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        zeroItemsCountLayout = findViewById(R.id.zeroItemsCountFavorite);

        // Initialize bookList and adapter
        bookList = new ArrayList<>();
        adapter = new MyBookAdapter(this, bookList);
        recyclerView.setAdapter(adapter);

        String username = getIntent().getStringExtra("username");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Favorites").child(username);

        // Attach a ValueEventListener to the 'Favorites' node
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookList.clear(); // Clear the existing list
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    // Data exists under the 'Favorites' node for the given user
                    for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                        String bookId = bookSnapshot.getKey();
                        if (bookId != null) {
                            // Fetch book details from the 'Books' node based on the book ID
                            DatabaseReference bookDetailsRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId);
                            bookDetailsRef.addValueEventListener(new ValueEventListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot bookDataSnapshot) {
                                    if (bookDataSnapshot.exists()) {
                                        BookClass book = bookDataSnapshot.getValue(BookClass.class);
                                        if (book != null) {
                                            bookList.add(book);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle database error
                                    Toast.makeText(FavoriteBookActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    // No data or child nodes exist under 'Favorites' for the given user
                    zeroItemsCountLayout.setVisibility(View.VISIBLE); // Show "No Books Found" layout
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(FavoriteBookActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });

        // back button
        findViewById(R.id.backBtnFavoriteBooks).setOnClickListener(v -> finish());
    }
}
