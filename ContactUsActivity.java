package com.gaurav.ecosweep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ContactUsActivity extends AppCompatActivity {

    // Contact details as requested
    private static final String CONTACT_EMAIL = "gauravnpatil2005@gmail.com";
    private static final String CONTACT_PHONE = "94222583474"; // Note: This number has 11 digits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact Us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvEmail = findViewById(R.id.tvContactEmail);
        TextView tvPhone = findViewById(R.id.tvContactPhone);
        Button btnCall = findViewById(R.id.btnCall);
        Button btnEmail = findViewById(R.id.btnEmail);

        tvEmail.setText("Email: " + CONTACT_EMAIL);
        tvPhone.setText("Phone: " + CONTACT_PHONE);

        btnCall.setOnClickListener(v -> {
            // Open the phone dialer with the number pre-filled
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + CONTACT_PHONE));
            startActivity(intent);
        });

        btnEmail.setOnClickListener(v -> {
            // Open an email client
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + CONTACT_EMAIL)); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, "EcoSweep App Inquiry");
            try {
                startActivity(Intent.createChooser(intent, "Send email via..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ContactUsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}