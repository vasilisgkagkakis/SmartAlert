package com.unipi.gkagkakis.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ImageView logoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        // Hide status bar for all activities
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
        startAnimation();
    }

    private void startAnimation() {
        AnimationHelper.startLogoAnimation(this, logoView, R.anim.logo_up_and_down);
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        logoView = findViewById(R.id.logo);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            // Add login logic here
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            // Add forgot password logic here
        });
    }
}