package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.BlurHelper;

public class NewAlertFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivAlertImage;
    private MaterialButton btnSelectImage;
    private Uri imageUri;
    private TextInputEditText etAlertDescription, etLocation;
    private AutoCompleteTextView actvAlertType, actvSeverityLevel;
    private MaterialButton btnCreateAlert, btnCancel;
    private MaterialButton btnDeleteImage; // Add this field

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_alert, container, false);
        initViews(view);
        setupDropdowns();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etAlertDescription = view.findViewById(R.id.et_alert_description);
        etLocation = view.findViewById(R.id.et_location);
        actvAlertType = view.findViewById(R.id.actv_alert_type);
        actvSeverityLevel = view.findViewById(R.id.actv_severity_level);
        btnCreateAlert = view.findViewById(R.id.btn_create_alert);
        btnCancel = view.findViewById(R.id.btn_cancel);
        ivAlertImage = view.findViewById(R.id.iv_alert_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnDeleteImage = view.findViewById(R.id.btn_delete_image);
    }

    private void setupDropdowns() {
        // Alert types
        String[] alertTypes = {"Fire", "Flood", "Earthquake", "Storm", "Medical Emergency", "Crime", "Traffic Accident"};
        ArrayAdapter<String> alertAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, alertTypes);
        actvAlertType.setAdapter(alertAdapter);

        // Severity levels
        String[] severityLevels = {"Low", "Medium", "High", "Critical"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, severityLevels);
        actvSeverityLevel.setAdapter(severityAdapter);

        setupOnItemClickListeners();
    }

    private void setupOnItemClickListeners() {
        // clear error if item selected
        actvAlertType.setOnItemClickListener((parent, view, position, id) -> actvAlertType.setError(null));
        // clear error if dropdown closes and text is not empty (so item is selected)
        actvAlertType.setOnDismissListener(() -> {
            if (!actvAlertType.getText().toString().trim().isEmpty()) {
                actvAlertType.setError(null);
            }
        });

        actvSeverityLevel.setOnItemClickListener((parent, view, position, id) -> actvSeverityLevel.setError(null));
        actvSeverityLevel.setOnDismissListener(() -> {
            if (!actvSeverityLevel.getText().toString().trim().isEmpty()) {
                actvSeverityLevel.setError(null);
            }
        });
    }

    private void setupClickListeners() {
        btnCreateAlert.setOnClickListener(v -> {
            if (validateInputs()) {
                // TODO: Add alert creation logic

                //just for debugging purposes
                debugLogInfos();

                Toast.makeText(getContext(), "Alert created successfully!", Toast.LENGTH_SHORT).show();
                // Close the fragment
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnDeleteImage.setOnClickListener(v -> {
            imageUri = null;
            ivAlertImage.setImageDrawable(null);
            ivAlertImage.setVisibility(View.GONE);
            btnSelectImage.setText("Add Image");
            btnDeleteImage.setVisibility(View.GONE); // Hide delete button
        });

        ivAlertImage.setOnClickListener(v -> {
            if (imageUri != null) {
                // Capture and blur the fragment background
                View rootView = requireActivity().getWindow().getDecorView().getRootView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
                rootView.setDrawingCacheEnabled(false);

                Bitmap blurred = BlurHelper.blur(requireContext(), screenshot, 15); // Use a blur utility

                ImagePreviewDialogFragment dialog = ImagePreviewDialogFragment.newInstance(imageUri, blurred);
                dialog.show(requireActivity().getSupportFragmentManager(), "image_preview");
            }
        });
    }

    private void debugLogInfos() {
        String alertType = actvAlertType.getText().toString();
        String severity = actvSeverityLevel.getText().toString();
        String location = etLocation.getText() != null ? etLocation.getText().toString() : "";
        String description = etAlertDescription.getText() != null ? etAlertDescription.getText().toString() : "";
        String image = (imageUri != null) ? imageUri.toString() : "No image";

        String message = "Type: " + alertType +
                "\nSeverity: " + severity +
                "\nLocation: " + location +
                "\nDescription: " + description +
                "\nImage: " + image;

        TextView textView = new TextView(getContext());
        textView.setText(message);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextIsSelectable(true);

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Alert Details")
                .setView(textView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            ivAlertImage.setImageURI(imageUri);
            ivAlertImage.setVisibility(View.VISIBLE);
            btnSelectImage.setText("Change Image");
            btnDeleteImage.setVisibility(View.VISIBLE); // Show delete button
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Description
        if (etAlertDescription.getText() == null || etAlertDescription.getText().toString().trim().isEmpty()) {
            etAlertDescription.setError("Description is required");
            isValid = false;
        } else {
            etAlertDescription.setError(null);
        }

        // Location
        if (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        } else {
            etLocation.setError(null);
        }

        // Alert type
        if (actvAlertType.getText().toString().trim().isEmpty()) {
            actvAlertType.setError("Alert type is required");
            isValid = false;
        } else {
            actvAlertType.setError(null);
        }

        // Severity level
        if (actvSeverityLevel.getText().toString().trim().isEmpty()) {
            actvSeverityLevel.setError("Severity level is required");
            isValid = false;
        } else {
            actvSeverityLevel.setError(null);
        }

        return isValid;
    }
}