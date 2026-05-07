package com.example.mobiletechnolgygroupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SavedListActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvNoMessage;
    private Button btnAdd;
    private ListAdapter adapter;
    private ArrayList<SavedItem> savedItems;
    private DatabaseReference dbRef;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_list);

        listView = findViewById(R.id.saved_item_list);
        tvNoMessage = findViewById(R.id.no_image_message);
        btnAdd = findViewById(R.id.btn_main_activity);

        dbRef = FirebaseDatabase.getInstance().getReference("Storage");

        savedItems = new ArrayList<>();
        adapter = new ListAdapter(this, savedItems);
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(SavedListActivity.this, MainActivity.class);
            startActivity(intent);
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SavedItem selectedItem = savedItems.get(position);

                Intent intent = new Intent(SavedListActivity.this, SavedItemDetailActivity.class);
                intent.putExtra("id", selectedItem.getId());
                intent.putExtra("reader", selectedItem.getReader());
                intent.putExtra("filename", selectedItem.getFilename());
                intent.putExtra("timestamp", selectedItem.getTimestamp());
                intent.putExtra("imageUri", selectedItem.getImageUri());
                intent.putExtra("text", selectedItem.getText());
                startActivity(intent);
            }
        });

        updateEmptyState();
        loadSavedItems();
    }

    private void loadSavedItems() {
        childEventListener = dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String id = snapshot.getKey();
                String reader = snapshot.child("reader").getValue(String.class);
                String text = snapshot.child("text").getValue(String.class);
                String imageUri = snapshot.child("imageUri").getValue(String.class);
                String filename = snapshot.child("filename").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                if (id == null) id = "";
                if (reader == null) reader = "";
                if (text == null) text = "";
                if (imageUri == null) imageUri = "";
                if (filename == null) filename = "";
                if (timestamp == null) timestamp = 0L;

                SavedItem item = new SavedItem(id, filename, reader, text, imageUri, timestamp);
                savedItems.add(item);

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String id = snapshot.getKey();
                String reader = snapshot.child("reader").getValue(String.class);
                String text = snapshot.child("text").getValue(String.class);
                String imageUri = snapshot.child("imageUri").getValue(String.class);
                String filename = snapshot.child("filename").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                for (int i = 0; i < savedItems.size(); i++) {
                    SavedItem item = savedItems.get(i);
                    if (item.getId() != null && item.getId().equals(id)) {
                        item.setReader(reader != null ? reader : "");
                        item.setText(text != null ? text : "");
                        item.setImageUri(imageUri != null ? imageUri : "");
                        item.setFilename(filename != null ? filename : "");
                        item.setTimestamp(timestamp != null ? timestamp : 0L);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String id = snapshot.getKey();

                for (int i = 0; i < savedItems.size(); i++) {
                    SavedItem item = savedItems.get(i);
                    if (item.getId() != null && item.getId().equals(id)) {
                        savedItems.remove(i);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedListActivity.this,
                        "Data load failed: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childEventListener != null) {
            dbRef.removeEventListener(childEventListener);
        }
    }

    private void updateEmptyState() {
        if (savedItems.isEmpty()) {
            tvNoMessage.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvNoMessage.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}
