package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.core.view.GravityCompat;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.AdminHomepageViewModel;

public class AdminHomepageActivity extends BaseActivity {

    private TextView tvAdminTitle;
    private TextView tvUserName;
    private TextView tvLogout;
    private Button btnViewAllAlerts;
    private AdminHomepageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_admin_homepage);
        StatusBarHelper.hideStatusBar(this);

        viewModel = new ViewModelProvider(this).get(AdminHomepageViewModel.class);

        initViews();
        setupClickListeners();
        observeViewModel();

        viewModel.checkUserAndLoadName();
    }

    private void initViews() {
        tvAdminTitle = findViewById(R.id.tv_admin_title);
        tvUserName = findViewById(R.id.tv_user_name);
        tvLogout = findViewById(R.id.tv_logout);
        btnViewAllAlerts = findViewById(R.id.btn_view_all_alerts);
    }


    private void setupClickListeners() {
        tvLogout.setOnClickListener(v -> viewModel.logout());
        btnViewAllAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminViewAlertsActivity.class);
            startActivity(intent);
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