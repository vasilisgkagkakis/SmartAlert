package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.AdminHomepageViewModel;

public class AdminHomepageActivity extends BaseActivity {
    private TextView tvUserName;
    private TextView tvLogout;
    private Button btnViewAllAlerts;
    private AdminHomepageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_admin_homepage);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        // Check user authentication and load admin data through ViewModel
        viewModel.checkUserAndLoadName();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvLogout = findViewById(R.id.tv_logout);
        btnViewAllAlerts = findViewById(R.id.btn_view_all_alerts);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AdminHomepageViewModel.class);
    }

    private void setupClickListeners() {
        // Logout through ViewModel (following clean architecture)
        tvLogout.setOnClickListener(v -> viewModel.logout());

        btnViewAllAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminViewAlertsActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Observe admin user name
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

        // Observe loading state (for future enhancements)
        viewModel.isLoading.observe(this, isLoading -> {
            // Can add loading indicators here if needed
            // For now, we'll keep it simple
        });
    }
}