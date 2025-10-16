package com.gaurav.ecosweep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Set up RecyclerView for notifications
        RecyclerView recyclerView = findViewById(R.id.rvNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create sample notifications
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification("Collection Scheduled", "Your waste collection is scheduled for tomorrow at 10 AM"));
        notifications.add(new Notification("Report Accepted", "Your waste report has been accepted"));
        notifications.add(new Notification("Reminder", "Don't forget to separate your recyclables"));

        recyclerView.setAdapter(new NotificationAdapter(notifications));
    }
}