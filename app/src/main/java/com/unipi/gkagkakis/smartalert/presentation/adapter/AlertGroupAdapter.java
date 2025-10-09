package com.unipi.gkagkakis.smartalert.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.LocationUtils;
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.model.AlertGroup;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AlertGroupAdapter extends RecyclerView.Adapter<AlertGroupAdapter.AlertGroupViewHolder> {

    private final List<AlertGroup> alertGroups;
    private final SimpleDateFormat dateFormat;

    public AlertGroupAdapter(List<AlertGroup> alertGroups) {
        this.alertGroups = alertGroups;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public AlertGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_group, parent, false);
        return new AlertGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertGroupViewHolder holder, int position) {
        AlertGroup group = alertGroups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return alertGroups.size();
    }

    class AlertGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView textGroupTitle;
        private final TextView textGroupLocation;
        private final TextView textGroupCount;
        private final ImageView imageExpandCollapse;
        private final LinearLayout layoutGroupHeader;
        private final LinearLayout layoutAlertsList;

        public AlertGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupTitle = itemView.findViewById(R.id.textGroupTitle);
            textGroupLocation = itemView.findViewById(R.id.textGroupLocation);
            textGroupCount = itemView.findViewById(R.id.textGroupCount);
            imageExpandCollapse = itemView.findViewById(R.id.imageExpandCollapse);
            layoutGroupHeader = itemView.findViewById(R.id.layoutGroupHeader);
            layoutAlertsList = itemView.findViewById(R.id.layoutAlertsList);
        }

        public void bind(AlertGroup group) {
            Alert firstAlert = group.getFirstAlert();
            if (firstAlert != null) {
                textGroupTitle.setText(String.format("%s Alert", firstAlert.getType()));

                // Parse and display human-readable location
                displayLocationForGroup(group);

                if (group.getAlertCount() > 1) {
                    textGroupCount.setText(String.format("%d alerts", group.getAlertCount()));
                    textGroupCount.setVisibility(View.VISIBLE);
                } else {
                    textGroupCount.setVisibility(View.GONE);
                }

                // Set expand/collapse icon
                imageExpandCollapse.setImageResource(
                        group.isExpanded() ? R.drawable.ic_expand_less : R.drawable.ic_expand_more
                );

                // Show/hide alerts list
                layoutAlertsList.setVisibility(group.isExpanded() ? View.VISIBLE : View.GONE);

                // Clear previous alerts
                layoutAlertsList.removeAllViews();

                // Add alerts to the list if expanded
                if (group.isExpanded()) {
                    for (Alert alert : group.getAlerts()) {
                        View alertView = createAlertView(alert);
                        layoutAlertsList.addView(alertView);
                    }
                }

                // Set click listener for expand/collapse
                layoutGroupHeader.setOnClickListener(v -> {
                    group.setExpanded(!group.isExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                });
            }
        }

        private void displayLocationForGroup(AlertGroup group) {
            String rawLocation = group.getGroupLocation();

            // Set initial text (coordinates or raw location)
            textGroupLocation.setText(rawLocation != null ? rawLocation : "Unknown location");

            // Try to get human-readable address
            LocationUtils.parseLocationAndGetAddress(itemView.getContext(), rawLocation, new LocationUtils.GeocodeCallback() {
                @Override
                public void onSuccess(String address) {
                    // Update with human-readable address
                    textGroupLocation.setText(address);
                }

                @Override
                public void onError(String error) {
                    // Keep the original location text if geocoding fails
                    // textGroupLocation already shows the raw location
                }
            });
        }

        private View createAlertView(Alert alert) {
            View alertView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_alert_detail, layoutAlertsList, false);

            TextView textType = alertView.findViewById(R.id.textAlertType);
            TextView textSeverity = alertView.findViewById(R.id.textAlertSeverity);
            TextView textDescription = alertView.findViewById(R.id.textAlertDescription);
            TextView textLocation = alertView.findViewById(R.id.textAlertLocation);
            TextView textDate = alertView.findViewById(R.id.textAlertDate);

            textType.setText(alert.getType());
            textSeverity.setText(String.format("Severity: %s", alert.getSeverity()));
            textDescription.setText(alert.getDescription());

            // Parse and display human-readable location for individual alerts
            displayLocationForAlert(textLocation, alert.getLocation());

            if (alert.getCreatedAt() != null) {
                textDate.setText(String.format("Created: %s", dateFormat.format(alert.getCreatedAt())));
            }

            return alertView;
        }

        private void displayLocationForAlert(TextView textLocation, String rawLocation) {
            // Set initial text with "Location: " prefix
            textLocation.setText(String.format("Location: %s", rawLocation != null ? rawLocation : "Unknown"));

            // Try to get human-readable address
            LocationUtils.parseLocationAndGetAddress(itemView.getContext(), rawLocation, new LocationUtils.GeocodeCallback() {
                @Override
                public void onSuccess(String address) {
                    // Update with human-readable address
                    textLocation.setText(String.format("Location: %s", address));
                }

                @Override
                public void onError(String error) {
                    // Keep the original location text if geocoding fails
                    // textLocation already shows the raw location
                }
            });
        }
    }
}
