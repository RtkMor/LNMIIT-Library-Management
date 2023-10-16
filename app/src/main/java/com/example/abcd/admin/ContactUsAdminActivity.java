package com.example.abcd.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ContactUsAdminActivity extends AppCompatActivity implements CustomContactUsAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private CustomContactUsAdapter adapter;
    private List<DataSnapshot> contactUsSnapshots; // Store DataSnapshots for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us_admin);
        Objects.requireNonNull(getSupportActionBar()).hide();

        recyclerView = findViewById(R.id.recyclerViewAdminContactUs);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase Realtime Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ContactUs");

        // Query to retrieve data (change the query as needed)
        Query query = databaseReference.orderByKey();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactUsSnapshots = new ArrayList<>(); // Initialize the list
                List<HashMap<String, String>> contactUsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    contactUsSnapshots.add(snapshot); // Store the DataSnapshot for deletion
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("username", snapshot.child("user").getValue(String.class));
                    dataMap.put("title", snapshot.child("subject").getValue(String.class));
                    dataMap.put("description", snapshot.child("description").getValue(String.class));
                    contactUsList.add(dataMap);
                }
                adapter = new CustomContactUsAdapter(contactUsList);
                adapter.setDeleteClickListener(ContactUsAdminActivity.this); // Set the delete click listener
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < contactUsSnapshots.size()) {
            DataSnapshot itemToDeleteSnapshot = contactUsSnapshots.get(position);
            // Get the Firebase key of the item to delete
            String itemId = itemToDeleteSnapshot.getKey();

            if (itemId != null) {
                DatabaseReference nodeToDelete = FirebaseDatabase.getInstance().getReference().child("ContactUs").child(itemId);

                // Remove the node from the database
                nodeToDelete.removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Node deleted successfully
                                // You can also remove the item from the local list if needed
                                contactUsSnapshots.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, contactUsSnapshots.size());
                            } else {
                                // Handle the failure to delete the node
                                // This could be due to network issues or permissions
                                // You can display an error message to the user or retry the operation
                            }
                        });
            }
        }
    }

}
