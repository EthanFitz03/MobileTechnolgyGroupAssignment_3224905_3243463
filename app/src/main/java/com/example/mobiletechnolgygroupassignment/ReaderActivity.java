package com.example.mobiletechnolgygroupassignment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class ReaderActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 3000;
    private static final String TAG = "ReaderActivity";
    private Uri imageFileUri;
    private ImageView imageView;
    private TextView textViewOutput;
    private String readerType;
    private Button btnEditResult;
    private String analysisResult = "";

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null && result.getData().getData() != null) {
                            imageFileUri = result.getData().getData();
                        }
                        imageView.setImageURI(imageFileUri);
                    }

                    if (imageFileUri !=null) {
                        imageView.setImageURI(imageFileUri);
                        analyzeSelectedImage();
                    } else {
                        textViewOutput.setText("Image could not be loaded");
                        Log.d(TAG, "ImageFileUri is null after activity result");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        imageView = findViewById(R.id.image_reader_target);
        textViewOutput = findViewById(R.id.text_reader_instructions);
        Button btnCamera = findViewById(R.id.btn_open_camera);
        Button btnLoad = findViewById(R.id.btn_load_image);
        btnEditResult = findViewById(R.id.btn_edit_result);

        btnEditResult.setEnabled(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            readerType = extras.getString("reader");
            if ("barcode".equals(readerType)) imageView.setImageResource(R.drawable.barcode);
            else if ("content".equals(readerType)) imageView.setImageResource(R.drawable.content);
            else if ("text".equals(readerType)) imageView.setImageResource(R.drawable.text);
        }

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera(v);
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage(v);
            }
        });

        btnEditResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditResult();
            }
        });
    }

    private boolean checkPermission() {
        String permission = android.Manifest.permission.CAMERA;
        boolean grantCamera = ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
        if (!grantCamera) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION);
        }
        return grantCamera;
    }

    public void openCamera(View view) {
        if (checkPermission() == false) return;

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        activityResultLauncher.launch(takePhotoIntent);
    }

    public void loadImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(galleryIntent);
    }

    private void analyzeSelectedImage() {
        if (imageFileUri == null) {
            textViewOutput.setText("Please select or capture an image first");
            btnEditResult.setEnabled(false);
            return;
        }
        textViewOutput.setText("Analysing Image");
        btnEditResult.setEnabled(false);

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageFileUri);
            Log.d(TAG, "analyzeSelectedImage called with URI = " + imageFileUri);

            if ("barcode".equals(readerType)) {
                runBarcodeAnalysis(inputImage);
            } else if ("content".equals(readerType)) {
                runImageLabelAnalysis(inputImage);
            } else if ("text".equals(readerType)) {
                runTextRecognition(inputImage);
            } else {
                textViewOutput.setText("Unknown reader type");
                Log.d(TAG, "Unknown readerType = " + readerType);
            }
        } catch (IOException e) {
            textViewOutput.setText("Analysis failed: " + e.getMessage());
            Log.e(TAG, "InputImage creation failed", e);
        }
    }

    private void runBarcodeAnalysis(InputImage inputImage) {
        BarcodeScanner scanner = BarcodeScanning.getClient();

        scanner.process(inputImage).addOnSuccessListener(barcodes -> {
            Log.d(TAG, "Barcode Analysis Success");

            if (barcodes == null || barcodes.isEmpty()) {
                setAnalysisResult("No barcode detected");
                return;
            }

            StringBuilder resultText = new StringBuilder("Detected Barcode:\n");

            for (Barcode barcode : barcodes) {
                String rawValue = barcode.getRawValue();
                if (rawValue != null && !rawValue.isEmpty()) {
                    resultText.append(rawValue).append("\n");
                }
            }

            setAnalysisResult(resultText.toString().trim());
        })
                .addOnFailureListener(e -> {
                    textViewOutput.setText("Barcode analysis failed: " + e.getMessage());
                    btnEditResult.setEnabled(false);
                    Log.e(TAG, "Barcode analysis failed", e);
                });
    }

    private void runImageLabelAnalysis(InputImage inputImage) {
        ImageLabeler labeler = ImageLabeling.getClient(com.google.mlkit.vision.label.defaults.ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(inputImage)
                .addOnSuccessListener(labels -> {
                    Log.d(TAG, "Image labeling success");

                    if (labels == null || labels.isEmpty()) {
                        setAnalysisResult("No image labels detected");
                        return;
                    }

                    StringBuilder resultText = new StringBuilder("Recognised image content:\n");

                    for (ImageLabel label : labels) {
                        resultText.append(label.getText())
                                .append(" (")
                                .append(String.format(Locale.getDefault(), "%.2f", label.getConfidence() * 100))
                                .append("% confidence)")
                                .append("%n");
                    }

                    setAnalysisResult(resultText.toString().trim());
                })
                .addOnFailureListener(e -> {
                    textViewOutput.setText("Image labeling failure: " + e.getMessage());
                    btnEditResult.setEnabled(false);
                    Log.e(TAG, "Image labeling failed", e);
                });
    }

    private void runTextRecognition(InputImage inputImage) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    Log.d(TAG, "Text recognition success");

                    String extractedText = visionText.getText().trim();

                    if (extractedText.isEmpty()) {
                        setAnalysisResult("No text detected");
                    } else {
                        setAnalysisResult("Extracted text:\n" + extractedText);
                    }
                })
                .addOnFailureListener(e -> {
                    textViewOutput.setText("Text recognition failed: " + e.getMessage());
                    btnEditResult.setEnabled(false);
                    Log.e(TAG, "Text recognition failed", e);
                });
    }

    private void setAnalysisResult(String result) {
        analysisResult = result;
        textViewOutput.setText(result);
        btnEditResult.setEnabled(true);
        Log.d(TAG, "analysisResult = " + result);
    }

    public void openEditResult() {
        if (imageFileUri == null) {
            textViewOutput.setText("Please select or capture an image first");
            return;
        }

        String textToEdit = analysisResult;
        if (textToEdit == null || textToEdit.isEmpty()) {
            textToEdit = textViewOutput.getText().toString();
        }

        Intent intent = new Intent(ReaderActivity.this, EditResultActivity.class);
        intent.putExtra("reader", getReaderLabel());
        intent.putExtra("text", textToEdit);
        intent.putExtra("imageUri", imageFileUri.toString());
        startActivity(intent);
    }

    private String getReaderLabel() {
        if ("barcode".equals(readerType)) return "Barcode Reader";
        if ("content".equals(readerType)) return "Content Reader";
        if ("text".equals(readerType)) return "Text Reader";
        return "Unknown Reader";
    }
}