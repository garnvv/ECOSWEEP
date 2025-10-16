package com.gaurav.ecosweep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable offline persistence
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            // Persistence already enabled
        }

        startActivity(new Intent(this, LoginPage.class));
        finish();
    }
}