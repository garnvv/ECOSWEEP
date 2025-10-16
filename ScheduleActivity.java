package com.gaurav.ecosweep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {
    private TextInputEditText etDate, etTime, etAddress, etWeight;
    private EditText etInstructions;
    private Button btnConfirm, btnCancel, btnUseCurrentLocation;
    private Switch switchSaveAddress;
    private RadioGroup radioPaymentMethod;
    private RadioButton radioCash, radioOnline;
    private Spinner spinnerComplaints;
    private TextView tvComplaintDetail;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private List<Complaint> pendingComplaints;
    private Complaint selectedComplaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initViews();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Set up time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Set up buttons
        setupButtons();

        // Load pending complaints
        loadPendingComplaints();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etAddress = findViewById(R.id.etAddress);
        etWeight = findViewById(R.id.etWeight);
        etInstructions = findViewById(R.id.etInstructions);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        switchSaveAddress = findViewById(R.id.switchSaveAddress);

        radioPaymentMethod = findViewById(R.id.radioPaymentMethod);
        radioCash = findViewById(R.id.radioCash);
        radioOnline = findViewById(R.id.radioOnline);

        spinnerComplaints = findViewById(R.id.spinnerComplaints);
        tvComplaintDetail = findViewById(R.id.tvComplaintDetail);
    }

    private void loadPendingComplaints() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference complaintsRef = mDatabase.child("complaints");

        // *** MODIFICATION: Force the reference to stay synchronized. ***
        // This helps bypass stale local cache immediately after a write from another activity.
        complaintsRef.keepSynced(true);

        complaintsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pendingComplaints = new ArrayList<>();
                        List<String> complaintTitles = new ArrayList<>();
                        complaintTitles.add("Select a Report (Complaint)"); // Default option

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Complaint complaint = snapshot.getValue(Complaint.class);
                            if (complaint != null && "Pending".equals(complaint.getStatus())) {
                                pendingComplaints.add(complaint);
                                // Display format: ID: XXXXX - WasteType (Date)
                                complaintTitles.add("ID: " + complaint.getComplaintId().substring(0, 5) + " - " + complaint.getWasteType() + " (" + complaint.getTimestamp().substring(5, 10) + ")");
                            }
                        }

                        if (pendingComplaints.isEmpty()) {
                            // No pending reports: Hide functional inputs, show message.
                            tvComplaintDetail.setText("No pending reports found. Please submit a new report first.");
                            tvComplaintDetail.setVisibility(View.VISIBLE);
                            spinnerComplaints.setVisibility(View.GONE);
                            btnConfirm.setEnabled(false);
                            // Optionally disable other inputs
                        } else {
                            // Pending reports found: Show spinner, hide initial message, enable inputs.
                            tvComplaintDetail.setVisibility(View.GONE);
                            spinnerComplaints.setVisibility(View.VISIBLE);
                            btnConfirm.setEnabled(true);
                            setupComplaintSpinner(complaintTitles);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ScheduleActivity.this, "Failed to load reports.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupComplaintSpinner(List<String> titles) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, titles);
        spinnerComplaints.setAdapter(adapter);

        spinnerComplaints.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedComplaint = pendingComplaints.get(position - 1);
                    displayComplaintDetails();
                    etAddress.setText(selectedComplaint.getAddress()); // Pre-fill address
                } else {
                    selectedComplaint = null;
                    tvComplaintDetail.setText("Select a report to schedule pickup.");
                    tvComplaintDetail.setVisibility(View.VISIBLE);
                    etAddress.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedComplaint = null;
            }
        });
    }

    private void displayComplaintDetails() {
        if (selectedComplaint != null) {

            String details = "Report ID: " + selectedComplaint.getComplaintId().substring(0, 8) +
                    "\nType: " + selectedComplaint.getWasteType() +
                    "\nDescription: " + selectedComplaint.getDescription() +
                    "\nReported: " + selectedComplaint.getTimestamp();
            tvComplaintDetail.setText(details);
            tvComplaintDetail.setVisibility(View.VISIBLE);
        } else {
            tvComplaintDetail.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDate.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String amPm;
                    if (selectedHour >= 12) {
                        amPm = "PM";
                        if (selectedHour > 12) {
                            selectedHour -= 12;
                        }
                    } else {
                        amPm = "AM";
                        if (selectedHour == 0) {
                            selectedHour = 12;
                        }
                    }
                    String selectedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, amPm);
                    etTime.setText(selectedTime);
                },
                hour, minute, false);

        timePickerDialog.show();
    }

    private void setupButtons() {
        btnConfirm.setOnClickListener(v -> {
            if (selectedComplaint == null) {
                Toast.makeText(this, "Please select a report to schedule.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (validateInputs()) {
                submitSchedule();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
        btnUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etDate.getText())) {
            etDate.setError("Please select a date");
            return false;
        }
        if (TextUtils.isEmpty(etTime.getText())) {
            etTime.setError("Please select a time");
            return false;
        }
        if (TextUtils.isEmpty(etAddress.getText())) {
            etAddress.setError("Please enter address");
            return false;
        }
        if (TextUtils.isEmpty(etWeight.getText())) {
            etWeight.setError("Please enter approximate weight");
            return false;
        }
        if (radioPaymentMethod.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void submitSchedule() {
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        String address = etAddress.getText().toString().trim();
        String weightText = etWeight.getText().toString().trim();
        double weight = Double.parseDouble(weightText);
        String instructions = etInstructions.getText().toString().trim();

        int selectedPaymentId = radioPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedPaymentId);
        String paymentMethod = selectedRadioButton.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        String complaintId = selectedComplaint.getComplaintId();

        String requestId = mDatabase.child("requests").push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        PickupRequest request = new PickupRequest(
                requestId,
                user.getUid(),
                complaintId,
                date,
                time,
                address,
                weight,
                instructions,
                paymentMethod,
                "Scheduled",
                timestamp
        );

        // Save request to Firebase and update complaint status
        mDatabase.child("requests").child(requestId).setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDatabase.child("complaints").child(complaintId).child("status").setValue("Scheduled")
                                .addOnCompleteListener(updateTask -> {
                                    if(updateTask.isSuccessful()){
                                        Toast.makeText(ScheduleActivity.this, "Pickup scheduled successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ScheduleActivity.this, "Schedule saved, but failed to update report status.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ScheduleActivity.this, "Failed to schedule pickup: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getAddressFromLocation(location);
                    } else {
                        Toast.makeText(ScheduleActivity.this,
                                "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressText.append(", ");
                    }
                }
                etAddress.setText(addressText.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting address from location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}