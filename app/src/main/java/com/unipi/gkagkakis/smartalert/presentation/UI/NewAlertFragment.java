package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.BlurHelper;
import com.unipi.gkagkakis.smartalert.Utils.CoordinatesUtil;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.AlertViewModel;

import android.location.Location;

public class NewAlertFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivAlertImage;
    private MaterialButton btnSelectImage;
    private Uri imageUri;
    private TextInputEditText etAlertDescription, etLocation;
    private AutoCompleteTextView actvAlertType, actvSeverityLevel;
    private MaterialButton btnCreateAlert, btnCancel;
    private MaterialButton btnDeleteImage;

    // Location: Fused client and permission launcher
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermsLauncher;

    // Guard to avoid recursive TextWatcher updates
    private boolean isUpdatingLocation = false;

    private AlertViewModel alertViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);
        locationPermsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fine || coarse) {
                        fetchCurrentLocation(fine);
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
        // Long‑press the location field to use current device location
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

    private void setupClickListeners() {
        btnCreateAlert.setOnClickListener(v -> {
            if (validateInputs()) {
                String alertType = actvAlertType.getText().toString().trim();
                String severity = actvSeverityLevel.getText().toString().trim();
                String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
                String description = etAlertDescription.getText() != null ? etAlertDescription.getText().toString().trim() : "";
                Uri imageUriToPass = (imageUri != null) ? imageUri : null;
                alertViewModel.createAlert(alertType, severity, location, description, imageUriToPass);
                Toast.makeText(requireContext(), "Alert created successfully!", Toast.LENGTH_SHORT).show();
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
            btnDeleteImage.setVisibility(View.GONE);
        });

        ivAlertImage.setOnClickListener(v -> {
            if (imageUri != null) {
                View rootView = requireActivity().getWindow().getDecorView().getRootView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap screenshot = Bitmap.createBitmap(rootView.getDrawingCache());
                rootView.setDrawingCacheEnabled(false);

                Bitmap blurred = BlurHelper.blur(requireContext(), screenshot, 15);
                ImagePreviewDialogFragment dialog = ImagePreviewDialogFragment.newInstance(imageUri, blurred);
                dialog.show(requireActivity().getSupportFragmentManager(), "image_preview");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            ivAlertImage.setImageURI(imageUri);
            ivAlertImage.setVisibility(View.VISIBLE);
            btnSelectImage.setText("Change Image");
            btnDeleteImage.setVisibility(View.VISIBLE);
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
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            fetchCurrentLocation(fineGranted);
        } else {
            locationPermsLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchCurrentLocation(boolean precise) {
        int priority = precise ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        CancellationTokenSource cts = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(priority, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fillLocationFromDevice(location);
                    } else {
                        Toast.makeText(requireContext(), "Could not get location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void fillLocationFromDevice(@NonNull Location loc) {
        String normalized = CoordinatesUtil.formatLatLng(loc.getLatitude(), loc.getLongitude());
        isUpdatingLocation = true;
        etLocation.setText(normalized);
        etLocation.setSelection(normalized.length());
        isUpdatingLocation = false;
        Toast.makeText(requireContext(), "Location filled from device", Toast.LENGTH_SHORT).show();
    }
}
