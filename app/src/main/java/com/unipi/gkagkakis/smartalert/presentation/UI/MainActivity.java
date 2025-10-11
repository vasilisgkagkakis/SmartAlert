package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        setupViewModel();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);

        // Request initial permissions through ViewModel
        viewModel.requestInitialPermissions(this);

        // Check authentication through ViewModel
        viewModel.checkAuthenticationAndNavigate();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Observe authentication state
        viewModel.isUserAuthenticated.observe(this, authenticated -> {
            if (authenticated != null && !authenticated) {
                Toast.makeText(this, "Welcome! Please log in or register.", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe admin status for navigation
        viewModel.isAdmin.observe(this, isAdmin -> {
            if (isAdmin != null) {
                navigateToHomepage(isAdmin);
            }
        });

        // Observe navigation errors
        viewModel.navigationError.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                viewModel.clearNavigationError();
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(this, isLoading -> {
            // Can add loading indicator here if needed
            // For now, we'll keep the UI simple
        });
    }

    private void navigateToHomepage(boolean isAdmin) {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HomepageActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle permission results through ViewModel (following clean architecture)
        viewModel.handlePermissionResult(this, requestCode, permissions, grantResults);
    }


    private void initViews() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}