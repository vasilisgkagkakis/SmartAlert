package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.HomepageViewModel;

public class HomepageActivity extends BaseActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;
    private HomepageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        // Load user data and update location through ViewModel
        viewModel.checkUserAndLoadName();
        viewModel.updateUserLocation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update location when user returns to the app through ViewModel
        viewModel.updateUserLocation(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle permission results through ViewModel (following clean architecture)
        viewModel.handlePermissionResult(this, requestCode, permissions, grantResults);
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        btnNewAlert = findViewById(R.id.btn_new_alert);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomepageViewModel.class);
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
        // Observe user name
        viewModel.getUserName().observe(this, name -> {
            if (name != null) {
                tvUserName.setText(name);
            }
        });

        // Observe navigation to login
        viewModel.getShouldNavigateToLogin().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                // Clear navigation state to prevent repeated navigation
                viewModel.clearNavigationToLogin();
            }
        });

        // Observe errors
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        // Observe location permission requirements
        viewModel.locationPermissionRequired.observe(this, required -> {
            if (required != null && required) {
                // Request location permissions through ViewModel
                viewModel.requestLocationPermissions(this);
                viewModel.clearLocationPermissionRequired();
            }
        });
    }
}