package com.unipi.gkagkakis.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ImageView logoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar for all activities
        // Since it's gonna be used everywhere, maybe transfer it into a helper class ?
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_register);

        initViews();
        setupClickListeners();
        startAnimation();
    }

    private void startAnimation() {
        AnimationHelper.startLogoAnimation(this, logoView, R.anim.logo_up_and_down);
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email_register);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password_register);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        logoView = findViewById(R.id.logo);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            // else, errors are shown and navigation is blocked
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Go back to login
        });
    }

    private boolean validateInputs() {
        TextInputEditText[] fields = {etFullName, etEmail, etPhone, etPassword, etConfirmPassword};
        String[] errorMessages = {
                "Full name required", "Email required", "Phone required", "Password required", "Confirm password required"
        };
        boolean hasError = false;

        for (int i = 0; i < fields.length; i++) {
            if (TextUtils.isEmpty(fields[i].getText())) {
                fields[i].setError(errorMessages[i]);
                hasError = true;
            } else {
                fields[i].setError(null);
            }
        }
        return !hasError;
    }
}