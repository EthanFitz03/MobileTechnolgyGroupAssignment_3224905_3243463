package com.example.mobiletechnolgygroupassignment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditResultActivity extends AppCompatActivity {

    private DatabaseReference dbRef;

    private TextView tvReader;
    private EditText etResult;
    private ImageView ivImage;
    private Button btnSave;

    private String reader;
    private String imageUriString;
    private Uri imageUri;

    // Used when editing an existing saved item
    private String existingId;
    private String existingFilename;
    private long existingTimestamp;
    private boolean isEditMode = false;

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

        existingId = intent.getStringExtra("id");
        reader = intent.getStringExtra("reader");
        String text = intent.getStringExtra("text");
        existingFilename = intent.getStringExtra("filename");
        imageUriString = intent.getStringExtra("imageUri");
        existingTimestamp = intent.getLongExtra("timestamp", 0L);

        if (reader == null) {
            reader = "Unknown Reader";
        }

        if (text == null) {
            text = "";
        }

        if (existingFilename == null) {
            existingFilename = "";
        }

        if (imageUriString == null) {
            imageUriString = "";
        }

        if (existingId != null && !existingId.isEmpty()) {
            isEditMode = true;
        }

        if (!imageUriString.isEmpty()) {
            imageUri = Uri.parse(imageUriString);
        }

        etResult.setText(text);
    }

    private void showDataOnScreen() {
        tvReader.setText(reader);

        if (imageUri != null) {
            ivImage.setImageURI(imageUri);
        } else {
            ivImage.setImageResource(R.drawable.barcode);
        }
    }

    private boolean isLocalFileUri(String uriString) {
        return uriString != null && uriString.startsWith("file:");
    }

    private String getImageExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);

        if (mimeType != null) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null && !extension.isEmpty()) {
                return extension;
            }
        }

        return "jpg";
    }

    private String copyImageToInternalStorage(Uri sourceUri, String baseFilename) throws IOException {
        File imageDir = new File(getFilesDir(), "saved_images");

        if (!imageDir.exists() && !imageDir.mkdirs()) {
            throw new IOException("Could not create image folder");
        }

        String extension = getImageExtension(sourceUri);
        File destinationFile = new File(imageDir, baseFilename + "." + extension);

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            if (inputStream == null) {
                throw new IOException("Could not open image input stream");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }

        return Uri.fromFile(destinationFile).toString();
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

        String itemId;
        String filename;
        long timestamp;

        if (isEditMode) {
            itemId = existingId;
            filename = (existingFilename == null || existingFilename.isEmpty())
                    ? String.valueOf(System.currentTimeMillis())
                    : existingFilename;
            timestamp = (existingTimestamp == 0L)
                    ? System.currentTimeMillis()
                    : existingTimestamp;
        } else {
            itemId = dbRef.push().getKey();
            filename = String.valueOf(System.currentTimeMillis());
            timestamp = System.currentTimeMillis();
        }

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Firebase key could not be created", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalImageUri;

        try {
            if (isLocalFileUri(imageUriString)) {
                finalImageUri = imageUriString;

                File localFile = new File(Uri.parse(finalImageUri).getPath());
                if (!localFile.exists()) {
                    Toast.makeText(this, "Saved image file is missing", Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                finalImageUri = copyImageToInternalStorage(Uri.parse(imageUriString), filename);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Image save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        SavedItem item = new SavedItem(itemId, filename, reader, editedText, finalImageUri, timestamp);

        dbRef.child(itemId).setValue(item)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditResultActivity.this, "Successfully Saved", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditResultActivity.this, SavedListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditResultActivity.this,
                            "Save Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}