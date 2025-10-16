package com.gaurav.ecosweep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvRequests;
    private LinearLayout emptyStateView;
    private Button btnRefresh;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RequestAdapter requestAdapter;
    private List<PickupRequest> allRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views from acitivty_track.xml
        tabLayout = findViewById(R.id.tabLayout);
        rvRequests = findViewById(R.id.rvRequests);
        emptyStateView = findViewById(R.id.emptyStateView);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Setup RecyclerView
        allRequests = new ArrayList<>();
        requestAdapter = new RequestAdapter(new ArrayList<>());
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(requestAdapter);

        // Setup Listeners
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    filterRequests(tab.getText().toString());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* No action */ }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    filterRequests(tab.getText().toString());
                }
            }
        });

        btnRefresh.setOnClickListener(v -> loadRequests());

        // Initial data load
        loadRequests();
    }

    private void loadRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference requestsRef = mDatabase.child("requests");
        // *** MODIFICATION 1: Force synchronization to prevent stale cache issues ***
        requestsRef.keepSynced(true);

        requestsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allRequests.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PickupRequest request = snapshot.getValue(PickupRequest.class);
                            if (request != null) {
                                allRequests.add(request);
                            }
                        }

                        // Default filter is 'All'
                        TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                        if (selectedTab != null && selectedTab.getText() != null) {
                            filterRequests(selectedTab.getText().toString());
                        } else {
                            // Fallback to "All" if tab selection fails
                            filterRequests("All");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TrackActivity.this, "Failed to load requests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        updateEmptyState(true);
                    }
                });
    }

    private void filterRequests(String filter) {
        List<PickupRequest> filteredList = new ArrayList<>();
        String normalizedFilter = filter.toLowerCase();

        if (normalizedFilter.contains("pending")) {
            // *** MODIFICATION 2: Use equalsIgnoreCase for robust status check ***
            // Scheduled is the initial pending state after submission from ScheduleActivity.
            for (PickupRequest request : allRequests) {
                if ("Scheduled".equalsIgnoreCase(request.getStatus()) || "In Progress".equalsIgnoreCase(request.getStatus())) {
                    filteredList.add(request);
                }
            }
        } else if (normalizedFilter.contains("completed")) {
            // Use equalsIgnoreCase for robust status check
            for (PickupRequest request : allRequests) {
                if ("Complete".equalsIgnoreCase(request.getStatus()) || "Completed".equalsIgnoreCase(request.getStatus())) {
                    filteredList.add(request);
                }
            }
        } else { // "All" or any other filter
            filteredList.addAll(allRequests);
        }

        requestAdapter.updateList(filteredList);
        updateEmptyState(filteredList.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvRequests.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            rvRequests.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }
}