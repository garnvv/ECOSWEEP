package com.gaurav.ecosweep;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<PickupRequest> requests;

    public RequestAdapter(List<PickupRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        PickupRequest request = requests.get(position);
        holder.status.setText("Status: " + request.getStatus());
        holder.details.setText("Pickup on " + request.getDate() + " at " + request.getTime() +
                " (Report ID: " + request.getComplaintId().substring(0, 5) + ")");
        holder.address.setText("Address: " + request.getAddress());

        // Optional: Change status text color based on status
        int color;
        if("Scheduled".equals(request.getStatus())) {
            color = 0xFF2E7D32; // Green
        } else if ("Completed".equals(request.getStatus())) {
            color = 0xFF4CAF50; // Lighter Green
        } else {
            color = 0xFFD32F2F; // Red
        }
        holder.status.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateList(List<PickupRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView status, details, address;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.tvRequestStatus);
            details = itemView.findViewById(R.id.tvRequestDetails);
            address = itemView.findViewById(R.id.tvRequestAddress);
        }
    }
}