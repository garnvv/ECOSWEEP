package com.gaurav.ecosweep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tvWelcome, tvLocation, tvBinStatus;
    // Added cardFeedback and cardContactUs
    private CardView cardReport, cardSchedule, cardTrack, cardNotifications, cardLogout, cardFeedback, cardContactUs;

    // Flag to check if scheduling is enabled (Kept for potential UI updates, but ignored in click listener)
    private boolean isSchedulingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvLocation = findViewById(R.id.tvLocation);
        tvBinStatus = findViewById(R.id.tvBinStatus);
        cardReport = findViewById(R.id.cardReport);
        cardSchedule = findViewById(R.id.cardSchedule);
        cardTrack = findViewById(R.id.cardTrack);
        cardNotifications = findViewById(R.id.cardNotifications);
        cardLogout = findViewById(R.id.cardLogout);
        // Initialize new CardViews
        cardFeedback = findViewById(R.id.cardFeedback);
        cardContactUs = findViewById(R.id.cardContactUs);

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userName = currentUser.isAnonymous() ?
                    "Guest" : currentUser.getDisplayName();
            if (userName == null || userName.isEmpty()) {
                userName = "User";
            }
            tvWelcome.setText("Welcome back, " + userName + "!");

            loadUserData(currentUser.getUid());

            // Check if user has pending reports to enable scheduling functionality.
            checkComplaintsForScheduling(currentUser.getUid());
        } else {
            startActivity(new Intent(Dashboard.this, LoginPage.class));
            finish();
            return;
        }

        // Set click listeners
        cardReport.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ReportActivity.class));
        });

        // *** MODIFICATION HERE: Simplify Schedule click logic ***
        cardSchedule.setOnClickListener(v -> {
            // We launch ScheduleActivity regardless, as it contains its own robust
            // check (loadPendingComplaints) and displays a "No reports" message if none are found.
            // This prevents a race condition delay from the asynchronous isSchedulingEnabled flag.
            startActivity(new Intent(Dashboard.this, ScheduleActivity.class));
        });
        // *** END MODIFICATION ***

        cardTrack.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, TrackActivity.class));
        });

        cardNotifications.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, NotificationsActivity.class));
        });

        // New click listener for Feedback
        cardFeedback.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, FeedbackActivity.class));
        });

        // New click listener for Contact Us
        cardContactUs.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, ContactUsActivity.class));
        });

        cardLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(Dashboard.this, LoginPage.class));
            finish();
        });
    }

    private void loadUserData(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void  onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // NOTE: You'll need a 'User' class defined elsewhere for this to compile
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getAddress() != null) {
                        tvLocation.setText("Current Location: " + user.getAddress());
                    } else {
                        tvLocation.setText("Current Location: N/A");
                    }
                } else {
                    tvLocation.setText("Current Location: N/A");
                }
            }

            @Override
            public void  onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void checkComplaintsForScheduling(String userId) {
        // Assume disabled until check completes
        isSchedulingEnabled = false;

        mDatabase.child("complaints").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public  void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasPendingComplaint = false;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // NOTE: You'll need a 'Complaint' class defined elsewhere for this to compile
                            Complaint complaint = snapshot.getValue(Complaint.class);
                            // Check for 'Pending' complaints
                            if (complaint != null && "Pending".equals(complaint.getStatus())) {
                                hasPendingComplaint = true;
                                break;
                            }
                        }

                        if (hasPendingComplaint) {
                            isSchedulingEnabled = true;
                        } else {
                            isSchedulingEnabled = false;
                        }
                    }

                    @Override
                    public  void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Dashboard.this, "Failed to check reports.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}