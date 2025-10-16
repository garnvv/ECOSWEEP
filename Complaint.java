package com.gaurav.ecosweep;

public class Complaint {
    private String complaintId;
    private String userId;
    private String wasteType;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
    private String status; // Pending, Scheduled, Completed
    private String timestamp;

    public Complaint() {
        // Default constructor required for Firebase
    }

    // Updated constructor without 'urgency'
    public Complaint(String complaintId, String userId, String wasteType, String description, double latitude, double longitude, String address, String status, String timestamp) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.wasteType = wasteType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public String getComplaintId() { return complaintId; }
    public String getUserId() { return userId; }
    public String getWasteType() { return wasteType; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}