package com.example.mobiletechnolgygroupassignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;

public class EditResultActivity extends AppCompatActivity {

    private DatabaseReference dbRef;

    private TextView tvReader;
    private EditText etResult;
    private ImageView ivImage;
    private Button btnSave;

    private String reader;
    private String imageUriString;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance().getReference("Storage");

        tvReader = findViewById(R.id.tv_reader);
        etResult = findViewById(R.id.et_result);
        ivImage = findViewById(R.id.iv_image);
        btnSave = findViewById(R.id.btn_save);

        readIntentData();
        showDataOnScreen();

        btnSave.setOnClickListener(v -> saveItemToFirebase());
    }

    private void readIntentData() {
        Intent intent = getIntent();

        reader = intent.getStringExtra("reader");
        String text = intent.getStringExtra("text");
        imageUriString = intent.getStringExtra("imageUri");

        if (reader == null) {
            reader = "Unknown Reader";
        }

        if (text == null) {
            text = "";
        }

        if (imageUriString != null && !imageUriString.isEmpty()) {
            imageUri = Uri.parse(imageUriString);
        }

        etResult.setText(text);
    }

    private void showDataOnScreen() {
        tvReader.setText(reader);

        if (imageUri != null) {
            ivImage.setImageURI(imageUri);
        }
    }

    private void saveItemToFirebase() {
        String editedText = etResult.getText().toString().trim();

        if (editedText.isEmpty()) {
            Toast.makeText(this, "Please enter or edit the result text", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUriString == null || imageUriString.isEmpty()) {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemId = dbRef.push().getKey();

        if (itemId == null) {
            Toast.makeText(this, "Could not generate Firebase key", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> itemMap = new HashMap<>();
        itemMap.put("id", itemId);
        itemMap.put("filename", filename);
        itemMap.put("reader", reader);
        itemMap.put("text", editedText);
        itemMap.put("imageUri", imageUriString);
        itemMap.put("timestamp", System.currentTimeMillis());

        dbRef.child(itemId).setValue(itemMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditResultActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditResultActivity.this, SavedListActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditResultActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}