package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.CoordinatesUtil;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.AlertViewModel;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.NewAlertViewModel;

public class NewAlertFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivAlertImage;
    private MaterialButton btnSelectImage;
    private Uri imageUri;
    private TextInputEditText etAlertDescription, etLocation;
    private AutoCompleteTextView actvAlertType, actvSeverityLevel;
    private MaterialButton btnCreateAlert, btnCancel;
    private MaterialButton btnDeleteImage;

    // ViewModels following MVVM pattern
    private AlertViewModel alertViewModel;
    private NewAlertViewModel newAlertViewModel;

    // Location permission launcher
    private ActivityResultLauncher<String[]> locationPermsLauncher;

    // Guard to avoid recursive TextWatcher updates
    private boolean isUpdatingLocation = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModels
        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);
        newAlertViewModel = new ViewModelProvider(this).get(NewAlertViewModel.class);

        // Setup location permission launcher
        locationPermsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) {
                        // Permissions granted, request location through ViewModel
                        newAlertViewModel.requestCurrentLocation();
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_alert, container, false);
        initViews(view);
        setupDropdowns();
        setupLocationAutoParse();
        setupClickListeners();
        observeViewModels();

        // Long-press the location field to use current device location
        etLocation.setOnLongClickListener(v -> {
            requestLocationThenFill();
            return true;
        });
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
        String[] alertTypes = {"Fire", "Flood", "Earthquake", "Storm", "Medical Emergency", "Crime", "Traffic Accident"};
        ArrayAdapter<String> alertAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, alertTypes);
        actvAlertType.setAdapter(alertAdapter);

        String[] severityLevels = {"Low", "Medium", "High", "Critical"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, severityLevels);
        actvSeverityLevel.setAdapter(severityAdapter);

        setupOnItemClickListeners();
    }

    private void setupOnItemClickListeners() {
        actvAlertType.setOnItemClickListener((parent, view, position, id) -> actvAlertType.setError(null));
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

    private void setupLocationAutoParse() {
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingLocation) return;
                String input = s.toString();
                String normalized = CoordinatesUtil.tryParseCoordinates(input);
                if (normalized != null && !normalized.equals(input.trim())) {
                    isUpdatingLocation = true;
                    etLocation.setText(normalized);
                    etLocation.setSelection(normalized.length());
                    isUpdatingLocation = false;
                }
            }
        });
    }

    private void observeViewModels() {
        // Observe AlertViewModel (existing alert creation logic)
        alertViewModel.getSaving().observe(getViewLifecycleOwner(), isSaving -> {
            if (isSaving != null) {
                btnCreateAlert.setEnabled(!isSaving);
                btnCreateAlert.setText(isSaving ? "Creating Alert..." : "Create Alert");
            }
        });

        alertViewModel.getUploadProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null && progress > 0 && progress < 100) {
                btnCreateAlert.setText(R.string.uploading + progress + R.string.percent);
            }
        });

        alertViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        alertViewModel.getCreatedId().observe(getViewLifecycleOwner(), alertId -> {
            if (alertId != null && !alertId.isEmpty()) {
                Toast.makeText(requireContext(), "Alert created successfully!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
                alertViewModel.clearResult();
            }
        });

        // Observe NewAlertViewModel for location operations
        newAlertViewModel.currentLocation.observe(getViewLifecycleOwner(), location -> {
            if (location != null && !location.isEmpty()) {
                isUpdatingLocation = true;
                etLocation.setText(location);
                etLocation.setSelection(location.length());
                isUpdatingLocation = false;
                Toast.makeText(requireContext(), "Location filled from device", Toast.LENGTH_SHORT).show();
                newAlertViewModel.clearCurrentLocation();
            }
        });

        newAlertViewModel.locationError.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                newAlertViewModel.clearLocationError();
            }
        });

        newAlertViewModel.locationPermissionRequired.observe(getViewLifecycleOwner(), required -> {
            if (required != null && required) {
                requestLocationPermissions();
                newAlertViewModel.clearLocationPermissionRequired();
            }
        });
    }

    private void setupClickListeners() {
        btnCreateAlert.setOnClickListener(v -> {
            if (validateInputs()) {
                String alertType = actvAlertType.getText().toString().trim();
                String severity = actvSeverityLevel.getText().toString().trim();
                String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
                String description = etAlertDescription.getText() != null ? etAlertDescription.getText().toString().trim() : "";
                Uri imageUriToPass = (imageUri != null) ? imageUri : null;
                alertViewModel.createAlert(requireContext(), alertType, severity, location, description, imageUriToPass);
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
            btnSelectImage.setText(R.string.add_image1);
            btnDeleteImage.setVisibility(View.GONE);
        });

        ivAlertImage.setOnClickListener(v -> {
            if (imageUri != null) {
                showImagePreview();
            }
        });
    }

    private void showImagePreview() {
        // Use ViewModel to create blurred screenshot (following clean architecture)
        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        Bitmap blurred = newAlertViewModel.createBlurredScreenshot(rootView);

        ImagePreviewDialogFragment dialog = ImagePreviewDialogFragment.newInstance(imageUri, blurred);
        dialog.show(requireActivity().getSupportFragmentManager(), "image_preview");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null) {
                imageUri = data.getData();
                ivAlertImage.setImageURI(imageUri);
                ivAlertImage.setVisibility(View.VISIBLE);
                btnSelectImage.setText(R.string.change_image);
                btnDeleteImage.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (etAlertDescription.getText() == null || etAlertDescription.getText().toString().trim().isEmpty()) {
            etAlertDescription.setError("Description is required");
            isValid = false;
        } else {
            etAlertDescription.setError(null);
        }

        if (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        } else {
            etLocation.setError(null);
        }

        if (actvAlertType.getText().toString().trim().isEmpty()) {
            actvAlertType.setError("Alert type is required");
            isValid = false;
        } else {
            actvAlertType.setError(null);
        }

        if (actvSeverityLevel.getText().toString().trim().isEmpty()) {
            actvSeverityLevel.setError("Severity level is required");
            isValid = false;
        } else {
            actvSeverityLevel.setError(null);
        }

        return isValid;
    }

    private void requestLocationThenFill() {
        if (newAlertViewModel.hasLocationPermissions()) {
            // Permissions already granted, request location through ViewModel
            newAlertViewModel.requestCurrentLocation();
        } else {
            // Request permissions first
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        locationPermsLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
}
