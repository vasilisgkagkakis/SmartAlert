package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        setupViewModel();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);
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

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Observe registration success
        viewModel.registrationSuccess.observe(this, success -> {
            if (success != null && success) {

                // Keep button disabled during redirect
                btnRegister.setEnabled(false);
                btnRegister.setText(R.string.registering);

                Toast.makeText(this, "Registration successful! Redirecting...", Toast.LENGTH_SHORT).show();
                new Handler(getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(this, HomepageActivity.class));
                    finish();
                }, 2000);
                // Clear success state to prevent repeated navigation
                viewModel.clearRegistrationSuccess();
            }
        });

        // Observe registration errors
        viewModel.registrationError.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                // Clear error after showing it
                viewModel.clearRegistrationError();
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null) {
                btnRegister.setEnabled(!isLoading);
                btnRegister.setText(isLoading ? R.string.registering : R.string.register);
            }
        });
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

                viewModel.registerUser(email, password, fullName, phone);
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs() {
        TextInputEditText[] fields = {etFullName, etEmail, etPhone, etPassword, etConfirmPassword};
        String[] errorMessages = {
                "Full name required", "Email required", "Phone required", "Password required", "Confirm password required"
        };
        boolean hasError = false;

        // Validate all fields are not empty
        for (int i = 0; i < fields.length; i++) {
            if (TextUtils.isEmpty(fields[i].getText())) {
                fields[i].setError(errorMessages[i]);
                hasError = true;
            } else {
                fields[i].setError(null);
            }
        }

        // Validate email format
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            hasError = true;
        }

        // Validate password length
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            hasError = true;
        }

        // Validate passwords match
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            hasError = true;
        }

        return !hasError;
    }
}