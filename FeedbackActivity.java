package com.gaurav.ecosweep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity {

    private EditText etFeedback;
    private RatingBar ratingBar;
    private Button btnSubmitFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Optional: Add a back button to the toolbar if you use an ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Submit Feedback");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etFeedback = findViewById(R.id.etFeedback);
        ratingBar = findViewById(R.id.ratingBar);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        btnSubmitFeedback.setOnClickListener(v -> {
            submitFeedback();
        });
    }

    private void submitFeedback() {
        String feedbackText = etFeedback.getText().toString().trim();
        float ratingValue = ratingBar.getRating();

        if (feedbackText.isEmpty() && ratingValue == 0.0f) {
            Toast.makeText(this, "Please provide some feedback or a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, you would send this data (feedbackText and ratingValue) to Firebase or your server here.
        // Example:
        // FirebaseDatabase.getInstance().getReference("feedback").push().setValue(new Feedback(feedbackText, ratingValue));

        // Display the Toast and navigate back to the Dashboard
        Toast.makeText(this, "Feedback submitted successfully! Thank you for your input.", Toast.LENGTH_LONG).show();

        // Navigate back to the Dashboard
        Intent intent = new Intent(FeedbackActivity.this, Dashboard.class);
        // Flags to clear the stack and make Dashboard the new root
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Optional: Handle the back button press in the toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}