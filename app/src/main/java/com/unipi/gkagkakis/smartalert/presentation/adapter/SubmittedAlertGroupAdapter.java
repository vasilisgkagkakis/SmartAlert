package com.unipi.gkagkakis.smartalert.presentation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.BlurHelper;
import com.unipi.gkagkakis.smartalert.Utils.ImageLoader;
import com.unipi.gkagkakis.smartalert.Utils.LocationUtils;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlertGroup;
import com.unipi.gkagkakis.smartalert.presentation.UI.ImagePreviewDialogFragment;

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
        holder.bind(group, listener, dateFormat, this);
    }

    @Override
    public int getItemCount() {
        return submittedAlertGroups.size();
    }

    public static class SubmittedAlertGroupViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(SubmittedAlertGroup group, OnGroupActionListener listener, SimpleDateFormat dateFormat, SubmittedAlertGroupAdapter adapter) {
            SubmittedAlert firstAlert = group.getFirstAlert();
            if (firstAlert != null) {
                textGroupTitle.setText(String.format(Locale.getDefault(), "%s Alert", firstAlert.getType()));

                // Parse and display human-readable location
                displayLocationForGroup(group);

                if (group.getAlertCount() > 1) {
                    textGroupCount.setText(String.format(Locale.getDefault(), "%d alerts", group.getAlertCount()));
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
                        View alertView = createAlertView(alert, dateFormat);
                        layoutAlertsList.addView(alertView);
                    }
                }

                // Set click listeners
                layoutGroupHeader.setOnClickListener(v -> {
                    group.setExpanded(!group.isExpanded());
                    adapter.notifyItemChanged(getBindingAdapterPosition());
                });

                btnAccept.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAcceptGroup(group, getBindingAdapterPosition());
                    }
                });

                btnReject.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectGroup(group, getBindingAdapterPosition());
                    }
                });
            }
        }

        private void displayLocationForGroup(SubmittedAlertGroup group) {
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

        private View createAlertView(SubmittedAlert alert, SimpleDateFormat dateFormat) {
            View alertView = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.item_submitted_alert_detail, layoutAlertsList, false);

            TextView textType = alertView.findViewById(R.id.textAlertType);
            TextView textSeverity = alertView.findViewById(R.id.textAlertSeverity);
            TextView textDescription = alertView.findViewById(R.id.textAlertDescription);
            TextView textLocation = alertView.findViewById(R.id.textAlertLocation);
            TextView textDate = alertView.findViewById(R.id.textAlertDate);

            // Add image handling
            LinearLayout layoutAlertImage = alertView.findViewById(R.id.layoutAlertImage);
            ImageView imageAlertPhoto = alertView.findViewById(R.id.imageAlertPhoto);

            textType.setText(alert.getType());
            textSeverity.setText(String.format(Locale.getDefault(), "Severity: %s", alert.getSeverity()));
            textDescription.setText(alert.getDescription());

            // Handle image display
            handleAlertImage(alert, layoutAlertImage, imageAlertPhoto);

            // Parse and display human-readable location for individual alerts
            displayLocationForAlert(textLocation, alert.getLocation());

            if (alert.getCreatedAt() != null) {
                textDate.setText(String.format(Locale.getDefault(), "Created: %s", dateFormat.format(alert.getCreatedAt())));
            }

            return alertView;
        }

        private void handleAlertImage(SubmittedAlert alert, LinearLayout layoutAlertImage, ImageView imageAlertPhoto) {
            String imageUrl = alert.getImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Show the image container
                layoutAlertImage.setVisibility(View.VISIBLE);

                // Load image using ImageLoader utility
                ImageLoader.loadImage(
                    imageUrl,
                    imageAlertPhoto,
                    new ImageLoader.ImageLoadCallback() {
                        @Override
                        public void onImageLoaded(@NonNull Bitmap bitmap) {
                            // Image loaded successfully
                            imageAlertPhoto.setImageBitmap(bitmap);

                            // Add click listener for full-screen preview with blur effect
                            imageAlertPhoto.setOnClickListener(v -> showImagePreview(bitmap));
                        }

                        @Override
                        public void onError(@NonNull Exception e) {
                            // Hide image container if loading fails
                            layoutAlertImage.setVisibility(View.GONE);
                        }
                    }
                );
            } else {
                // Hide image container if no image URL
                layoutAlertImage.setVisibility(View.GONE);
            }
        }

        private void showImagePreview(Bitmap bitmap) {
            // Get the activity to access the window and fragment manager
            Activity activity = null;
            Context context = itemView.getContext();
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else if (context instanceof ContextThemeWrapper) {
                activity = (Activity) ((ContextThemeWrapper) context).getBaseContext();
            }

            if (activity instanceof FragmentActivity) {
                FragmentActivity fragmentActivity = (FragmentActivity) activity;

                // Capture screenshot using modern approach
                View rootView = fragmentActivity.getWindow().getDecorView().getRootView();
                Bitmap screenshot = createBitmapFromView(rootView);

                Bitmap blurred = BlurHelper.blur(context, screenshot, 15);
                ImagePreviewDialogFragment dialog =
                    ImagePreviewDialogFragment.newInstance(bitmap, blurred);
                dialog.show(fragmentActivity.getSupportFragmentManager(), "image_preview");
            } else {
                // Fallback to simple dialog if we can't get FragmentActivity
                Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.dialog_image_preview);

                ImageView previewImage = dialog.findViewById(R.id.iv_preview);
                previewImage.setImageBitmap(bitmap);
                previewImage.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            }
        }

        @SuppressLint("ObsoleteSdkInt")
        private Bitmap createBitmapFromView(View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Modern approach for API 26+
                Bitmap bitmap = Bitmap.createBitmap(
                    view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
                return bitmap;
            } else {
                // Legacy approach for older versions
                return createBitmapFromViewLegacy(view);
            }
        }

        @SuppressWarnings("deprecation")
        private Bitmap createBitmapFromViewLegacy(View view) {
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;
        }

        private void displayLocationForAlert(TextView textLocation, String rawLocation) {
            // Set initial text with "Location: " prefix
            textLocation.setText(String.format(Locale.getDefault(), "Location: %s", rawLocation != null ? rawLocation : "Unknown"));

            // Try to get human-readable address
            LocationUtils.parseLocationAndGetAddress(itemView.getContext(), rawLocation, new LocationUtils.GeocodeCallback() {
                @Override
                public void onSuccess(String address) {
                    // Update with human-readable address
                    textLocation.setText(String.format(Locale.getDefault(), "Location: %s", address));
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
