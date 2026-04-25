package com.example.mobiletechnolgygroupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the UI elements from Activity 1
        ImageView imageBarcode = findViewById(R.id.image_barcode);
        ImageView imageContent = findViewById(R.id.image_content);
        ImageView imageOcr = findViewById(R.id.image_ocr);
        Button btnList = findViewById(R.id.btn_list_analysed_images);

        // Click listeners to open Activity 2
        imageBarcode.setOnClickListener(v -> openReader("barcode"));
        imageContent.setOnClickListener(v -> openReader("content"));
        imageOcr.setOnClickListener(v -> openReader("text"));

        // Opens Activity 6
        btnList.setOnClickListener(v -> {
            Intent intent = new Intent(this, SavedListActivity.class);
            startActivity(intent);
        });
    }

    private void openReader(String mode) {
        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra("reader", mode); // Pass to activity 2
        startActivity(intent);
    }
}