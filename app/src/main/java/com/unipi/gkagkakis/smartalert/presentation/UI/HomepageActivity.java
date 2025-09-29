package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

public class HomepageActivity extends AppCompatActivity {

    private TextView tvUserName;
    private MaterialButton btnNewAlert;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();
        Log.d("firebaseAuth", "mAuth: " + firebaseAuth);
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
}