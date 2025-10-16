package com.gaurav.ecosweep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

// Removed all Google Location/Map related imports
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private ImageView ivWastePhoto;
    private TextInputEditText etLocationAddress;
    private EditText etDescription;
    // Removed SeekBar, FusedLocationProviderClient, and related buttons/variables
    private Button btnSubmit, btnTakePhoto;
    private RadioGroup radioGroup;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        ivWastePhoto = findViewById(R.id.ivWastePhoto);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        radioGroup = findViewById(R.id.radioGroup);
        etLocationAddress = findViewById(R.id.etLocationAddress);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        // Set up photo button
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ReportActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        // Set up submit button
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ivWastePhoto.setImageBitmap(photo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void submitReport() {
        if (ivWastePhoto.getDrawable() == null) {
            Toast.makeText(this, "Please take a photo of the waste", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationAddress = etLocationAddress.getText().toString().trim();
        if (locationAddress.isEmpty()) {
            etLocationAddress.setError("Please enter the location address");
            return;
        }

        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etDescription.setError("Please describe the waste issue");
            return;
        }

        String wasteType = "";
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.rbOrganic) {
            wasteType = "Organic";
        } else if (selectedId == R.id.rbPlastic) {
            wasteType = "Plastic";
        } else if (selectedId == R.id.rbMetal) {
            wasteType = "Metal";
        } else if (selectedId == R.id.rbOther) {
            wasteType = "Other";
        }
        if (wasteType.isEmpty()) {
            Toast.makeText(this, "Please select a waste type", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login to submit a report", Toast.LENGTH_SHORT).show();
            return;
        }

        String complaintId = mDatabase.child("complaints").push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Removed dynamic location capture, setting lat/lon to 0.0 or a default
        double latitude = 0.0;
        double longitude = 0.0;

        Complaint complaint = new Complaint(
                complaintId,
                user.getUid(),
                wasteType,
                description,
                latitude,
                longitude,
                locationAddress,
                "Pending", // Status must be 'Pending' for scheduling
                timestamp
        );

        mDatabase.child("complaints").child(complaintId).setValue(complaint)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ReportActivity.this, "Report submitted successfully!", Toast.LENGTH_SHORT).show();
                            // *** CHANGE HERE: Removed aggressive Intent flags. ***
                            // This allows the Dashboard to be resumed and re-query the scheduling status.
                            Intent intent = new Intent(ReportActivity.this, Dashboard.class);
                            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // REMOVED
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ReportActivity.this, "Failed to submit report: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}