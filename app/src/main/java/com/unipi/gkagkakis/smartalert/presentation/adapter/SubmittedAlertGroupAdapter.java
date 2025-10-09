package com.unipi.gkagkakis.smartalert.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlertGroup;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SubmittedAlertGroupAdapter extends RecyclerView.Adapter<SubmittedAlertGroupAdapter.SubmittedAlertGroupViewHolder> {

    private final List<SubmittedAlertGroup> submittedAlertGroups;
    private final SimpleDateFormat dateFormat;
    private final OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onAcceptGroup(SubmittedAlertGroup group, int position);
        void onRejectGroup(SubmittedAlertGroup group, int position);
    }

    public SubmittedAlertGroupAdapter(List<SubmittedAlertGroup> submittedAlertGroups, OnGroupActionListener listener) {
        this.submittedAlertGroups = submittedAlertGroups;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public SubmittedAlertGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submitted_alert_group, parent, false);
        return new SubmittedAlertGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmittedAlertGroupViewHolder holder, int position) {
        SubmittedAlertGroup group = submittedAlertGroups.get(position);
        holder.bind(group, position);
    }

    @Override
    public int getItemCount() {
        return submittedAlertGroups.size();
    }

    class SubmittedAlertGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView textGroupTitle;
        private final TextView textGroupLocation;
        private final TextView textGroupCount;
        private final TextView textGroupStatus;
        private final ImageView imageExpandCollapse;
        private final LinearLayout layoutGroupHeader;
        private final LinearLayout layoutAlertsList;
        private final LinearLayout layoutActionButtons;
        private final Button btnAccept;
        private final Button btnReject;

        public SubmittedAlertGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupTitle = itemView.findViewById(R.id.textGroupTitle);
            textGroupLocation = itemView.findViewById(R.id.textGroupLocation);
            textGroupCount = itemView.findViewById(R.id.textGroupCount);
            textGroupStatus = itemView.findViewById(R.id.textGroupStatus);
            imageExpandCollapse = itemView.findViewById(R.id.imageExpandCollapse);
            layoutGroupHeader = itemView.findViewById(R.id.layoutGroupHeader);
            layoutAlertsList = itemView.findViewById(R.id.layoutAlertsList);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(SubmittedAlertGroup group, int position) {
            SubmittedAlert firstAlert = group.getFirstAlert();
            if (firstAlert != null) {
                textGroupTitle.setText(String.format("%s Alert", firstAlert.getType()));
                textGroupLocation.setText(group.getGroupLocation());

                if (group.getAlertCount() > 1) {
                    textGroupCount.setText(String.format("%d alerts", group.getAlertCount()));
                    textGroupCount.setVisibility(View.VISIBLE);
                } else {
                    textGroupCount.setVisibility(View.GONE);
                }

                // Set status
                textGroupStatus.setText(group.getStatus());
                textGroupStatus.setVisibility(View.VISIBLE);

                // Set status color
                switch (group.getStatus()) {
                    case "PENDING":
                        textGroupStatus.setTextColor(itemView.getContext().getColor(R.color.warning));
                        break;
                    case "ACCEPTED":
                        textGroupStatus.setTextColor(itemView.getContext().getColor(R.color.success));
                        break;
                    case "REJECTED":
                        textGroupStatus.setTextColor(itemView.getContext().getColor(R.color.error));
                        break;
                }

                // Show/hide action buttons based on status
                if (group.isPending()) {
                    layoutActionButtons.setVisibility(View.VISIBLE);
                } else {
                    layoutActionButtons.setVisibility(View.GONE);
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
                    for (SubmittedAlert alert : group.getSubmittedAlerts()) {
                        View alertView = createAlertView(alert);
                        layoutAlertsList.addView(alertView);
                    }
                }

                // Set click listeners
                layoutGroupHeader.setOnClickListener(v -> {
                    group.setExpanded(!group.isExpanded());
                    notifyItemChanged(getBindingAdapterPosition());
                });

                btnAccept.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAcceptGroup(group, position);
                    }
                });

                btnReject.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectGroup(group, position);
                    }
                });
            }
        }

        private View createAlertView(SubmittedAlert alert) {
            View alertView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_submitted_alert_detail, layoutAlertsList, false);

            TextView textType = alertView.findViewById(R.id.textAlertType);
            TextView textSeverity = alertView.findViewById(R.id.textAlertSeverity);
            TextView textDescription = alertView.findViewById(R.id.textAlertDescription);
            TextView textLocation = alertView.findViewById(R.id.textAlertLocation);
            TextView textDate = alertView.findViewById(R.id.textAlertDate);
            LinearLayout layoutAlertImage = alertView.findViewById(R.id.layoutAlertImage);
            ImageView imageAlertPhoto = alertView.findViewById(R.id.imageAlertPhoto);

            textType.setText(alert.getType());
            textSeverity.setText(String.format("Severity: %s", alert.getSeverity()));
            textDescription.setText(alert.getDescription());
            textLocation.setText(String.format("Location: %s", alert.getLocation()));

            if (alert.getCreatedAt() != null) {
                textDate.setText(String.format("Created: %s", dateFormat.format(alert.getCreatedAt())));
            }

            // Handle image loading
            if (alert.getImageUrl() != null && !alert.getImageUrl().isEmpty()) {
                layoutAlertImage.setVisibility(View.VISIBLE);

                // Load image using ImageLoader
                com.unipi.gkagkakis.smartalert.Utils.ImageLoader.loadImage(
                    itemView.getContext(),
                    alert.getImageUrl(),
                    imageAlertPhoto
                );

                // Set click listener to show full-size image
                imageAlertPhoto.setOnClickListener(v -> {
                    showFullSizeImage(alert.getImageUrl());
                });
            } else {
                layoutAlertImage.setVisibility(View.GONE);
            }

            return alertView;
        }

        private void showFullSizeImage(String imageUrl) {
            // Create and show full-size image dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(itemView.getContext());
            View dialogView = LayoutInflater.from(itemView.getContext())
                    .inflate(android.R.layout.select_dialog_item, null);

            ImageView fullSizeImageView = new ImageView(itemView.getContext());
            fullSizeImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            fullSizeImageView.setAdjustViewBounds(true);

            // Load full-size image
            com.unipi.gkagkakis.smartalert.Utils.ImageLoader.loadImage(
                itemView.getContext(),
                imageUrl,
                fullSizeImageView
            );

            builder.setView(fullSizeImageView)
                   .setTitle("Alert Image")
                   .setPositiveButton("Close", null)
                   .show();
        }
    }
}
