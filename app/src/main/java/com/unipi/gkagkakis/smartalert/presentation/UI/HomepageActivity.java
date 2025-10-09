package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import androidx.core.view.GravityCompat;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.HomepageViewModel;
import com.unipi.gkagkakis.smartalert.service.LocationTrackingService;

public class HomepageActivity extends BaseActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;
    private TextView tvLogout;
    private HomepageViewModel viewModel;
    private LocationTrackingService locationTrackingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);

        viewModel = new ViewModelProvider(this).get(HomepageViewModel.class);
        locationTrackingService = LocationTrackingService.getInstance(this);

        initViews();
        setupClickListeners();
        observeViewModel();

        viewModel.checkUserAndLoadName();

        // Update user location when they open the homepage
        updateUserLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update location when user returns to the app
        updateUserLocation();
    }

    private void updateUserLocation() {
        if (locationTrackingService.hasLocationPermissions()) {
            locationTrackingService.updateLocationNow();
        } else {
            // Request permissions if not granted
            locationTrackingService.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationTrackingService.handlePermissionResult(requestCode, permissions, grantResults);
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        btnNewAlert = findViewById(R.id.btn_new_alert);
    }

    private void setupClickListeners() {
        btnNewAlert.setOnClickListener(v -> {
            NewAlertFragment fragment = new NewAlertFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void observeViewModel() {
        viewModel.getUserName().observe(this, name -> tvUserName.setText(name));

        viewModel.getShouldNavigateToLogin().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }
}