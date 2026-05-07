package com.example.mobiletechnolgygroupassignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class SavedItemDetailActivity extends AppCompatActivity {

    private TextView detail_reader;
    private TextView detail_text;
    private ImageView detail_image;
    private Button button_edit;
    private Button button_delete;
    private Button button_cancel;

    private DatabaseReference dbRef;

    private String itemId;
    private String reader;
    private String text;
    private String filename;
    private String imageUriString;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_item_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        detail_reader = findViewById(R.id.detail_reader);
        detail_text = findViewById(R.id.detail_text);
        detail_image = findViewById(R.id.detail_image);
        button_cancel = findViewById(R.id.button_cancel);
        button_delete = findViewById(R.id.button_delete);
        button_edit = findViewById(R.id.button_edit);

        dbRef = FirebaseDatabase.getInstance().getReference("Storage");

        readIntentData();
        showItemData();

        button_edit.setOnClickListener(v -> openEditResultActivity());
        button_delete.setOnClickListener(v -> deleteCurrentItem());
        button_cancel.setOnClickListener(v -> returnToSavedList());
    }

    private void readIntentData() {
        Intent intent = getIntent();

        itemId = intent.getStringExtra("id");
        reader = intent.getStringExtra("reader");
        text = intent.getStringExtra("text");
        filename = intent.getStringExtra("filename");
        imageUriString = intent.getStringExtra("imageUri");
        timestamp = intent.getLongExtra("timestamp", 0L);

        if (itemId == null) itemId = "";
        if (reader == null) reader = "";
        if (text == null) text = "";
        if (filename == null) filename = "";
        if (imageUriString == null) imageUriString = "";
    }

    private void showItemData() {
        detail_reader.setText(reader);
        detail_text.setText(text);

        if (!imageUriString.isEmpty()) {
            try {
                detail_image.setImageURI(Uri.parse(imageUriString));
            } catch (Exception e) {
                detail_image.setImageResource(R.drawable.barcode);
            }
        } else {
            detail_image.setImageResource(R.drawable.barcode);
        }
    }

    private void openEditResultActivity() {
        Intent intent = new Intent(SavedItemDetailActivity.this, EditResultActivity.class);
        intent.putExtra("id", itemId);
        intent.putExtra("reader", reader);
        intent.putExtra("text", text);
        intent.putExtra("filename", filename);
        intent.putExtra("imageUri", imageUriString);
        intent.putExtra("timestamp", timestamp);
        startActivity(intent);
    }

    private void deleteCurrentItem() {
        if (itemId.isEmpty()) {
            Toast.makeText(this, "Item ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child(itemId).removeValue()
                .addOnSuccessListener(unused -> {
                    deleteLocalImageIfExists();
                    Toast.makeText(SavedItemDetailActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SavedItemDetailActivity.this,
                            "Failed Delete: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void deleteLocalImageIfExists() {
        if (imageUriString != null && imageUriString.startsWith("file:")) {
            try {
                File imageFile = new File(Uri.parse(imageUriString).getPath());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
            }
        }

    }

    private void returnToSavedList() {
        finish();
    }
}