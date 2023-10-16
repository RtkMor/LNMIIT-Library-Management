package com.example.abcd.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.abcd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AddNewBookActivity extends AppCompatActivity {

    private EditText titleEt, authorEt, isbnEt, callNumberEt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_book);

        Objects.requireNonNull(getSupportActionBar()).hide();

        titleEt = findViewById(R.id.bookTitleEt);
        authorEt = findViewById(R.id.bookAuthorEt);
        isbnEt = findViewById(R.id.bookIsbnEt);
        callNumberEt = findViewById(R.id.bookCallNumberEt);

        // Submit Button
        Button submitBtn = findViewById(R.id.addNewBookBtn);
        submitBtn.setOnClickListener(view -> addNewBook());

        // Back Button
        View backBtn = findViewById(R.id.backBtnAddNewBook);
        backBtn.setOnClickListener(view -> finish());
    }

    private void addNewBook() {
        String title = titleEt.getText().toString().trim().toUpperCase(Locale.ROOT);
        String author = authorEt.getText().toString().trim().toUpperCase(Locale.ROOT);
        String isbn = isbnEt.getText().toString().trim().toUpperCase(Locale.ROOT);
        String callNumber = callNumberEt.getText().toString().trim().toUpperCase(Locale.ROOT);

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || callNumber.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");

        // Get a reference to the "Books" node to count existing books
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the count of existing books
                long count = dataSnapshot.getChildrenCount();
                count++;

                // Create a new book ID based on the count
                String newBookId = "book" + count;

                // Create a HashMap to store the book data
                HashMap<String, Object> bookData = new HashMap<>();
                bookData.put("bookId", newBookId);
                bookData.put("title", title);
                bookData.put("author", author);
                bookData.put("isbn", isbn);
                bookData.put("callNumber", callNumber);
                bookData.put("accountNumber", Long.toString(count));
                bookData.put("rating",0);
                bookData.put("ratingNumber",0);

                // Push the data to Firebase using the new book ID
                databaseReference.child(newBookId).setValue(bookData);

                Toast.makeText(AddNewBookActivity.this, "Book added successfully!", Toast.LENGTH_SHORT).show();
                clearFields();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if necessary
            }
        });
    }

    private void clearFields() {

        titleEt.setText("");
        authorEt.setText("");
        isbnEt.setText("");
        callNumberEt.setText("");

    }

}