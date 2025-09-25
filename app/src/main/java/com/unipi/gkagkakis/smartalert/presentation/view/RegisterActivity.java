package com.unipi.gkagkakis.smartalert.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("ActivityLifecycle", "onCreate: " + getClass().getSimpleName());

        setContentView(R.layout.activity_register);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.util.Log.d("ActivityLifecycle", "onDestroy: " + getClass().getSimpleName());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        android.util.Log.d("ActivityLifecycle", "onRestart: " + getClass().getSimpleName());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        android.util.Log.d("ActivityLifecycle", "onRestoreInstanceState: " + getClass().getSimpleName());
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email_register);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password_register);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
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

        // Check if passwords match
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            hasError = true;
        }

        return !hasError;
    }
}