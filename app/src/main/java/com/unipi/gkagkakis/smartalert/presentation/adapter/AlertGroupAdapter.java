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
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.model.AlertGroup;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AlertGroupAdapter extends RecyclerView.Adapter<AlertGroupAdapter.AlertGroupViewHolder> {

    private List<AlertGroup> alertGroups;
    private SimpleDateFormat dateFormat;

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
        private TextView textGroupTitle;
        private TextView textGroupLocation;
        private TextView textGroupCount;
        private ImageView imageExpandCollapse;
        private LinearLayout layoutGroupHeader;
        private LinearLayout layoutAlertsList;

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
                textGroupTitle.setText(firstAlert.getType() + " Alert");
                textGroupLocation.setText(group.getGroupLocation());

                if (group.getAlertCount() > 1) {
                    textGroupCount.setText(group.getAlertCount() + " alerts");
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
                    notifyItemChanged(getAdapterPosition());
                });
            }
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
            textSeverity.setText("Severity: " + alert.getSeverity());
            textDescription.setText(alert.getDescription());
            textLocation.setText("Location: " + alert.getLocation());

            if (alert.getCreatedAt() != null) {
                textDate.setText("Created: " + dateFormat.format(alert.getCreatedAt()));
            }

            return alertView;
        }
    }
}
