package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

public class HomepageActivity extends AppCompatActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("ActivityLifecycle", "onCreate: " + getClass().getSimpleName());

        setContentView(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        btnNewAlert = findViewById(R.id.btn_new_alert);
    }

    private void setupClickListeners() {
        btnNewAlert.setOnClickListener(v -> {
            NewAlertFragment fragment = new NewAlertFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(android.R.id.content, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d("ActivityLifecycle", "onDestroy: " + getClass().getSimpleName());
    }
}