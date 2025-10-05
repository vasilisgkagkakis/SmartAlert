package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import androidx.core.view.GravityCompat;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.HomepageViewModel;

public class HomepageActivity extends BaseActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;
    private HomepageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);

        viewModel = new ViewModelProvider(this).get(HomepageViewModel.class);

        initViews();
        setupClickListeners();
        observeViewModel();

        viewModel.checkUserAndLoadName();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        btnNewAlert = findViewById(R.id.btn_new_alert);
    }

    @Override
    protected void onFabClick() {
        // Override the FAB behavior for this activity
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
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