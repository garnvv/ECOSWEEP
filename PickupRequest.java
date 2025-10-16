package com.gaurav.ecosweep;

public class PickupRequest {
    private String requestId;
    private String userId;
    private String complaintId; // Link to the complaint
    private String date;
    private String time;
    private String address;
    private double weight;
    private String instructions;
    private String paymentMethod;
    private String status; // Scheduled, In Progress, Complete
    private String timestamp;

    public PickupRequest() {
        // Default constructor required for Firebase
    }

    public PickupRequest(String requestId, String userId, String complaintId, String date, String time, String address, double weight, String instructions, String paymentMethod, String status, String timestamp) {
        this.requestId = requestId;
        this.userId = userId;
        this.complaintId = complaintId;
        this.date = date;
        this.time = time;
        this.address = address;
        this.weight = weight;
        this.instructions = instructions;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public String getComplaintId() { return complaintId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getAddress() { return address; }
    public double getWeight() { return weight; }
    public String getInstructions() { return instructions; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}