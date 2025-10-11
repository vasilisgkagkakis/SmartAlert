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
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.LoginViewModel;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;
import com.unipi.gkagkakis.smartalert.service.FCMTokenManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private LoginViewModel viewModel;
    private boolean isRedirecting = false; // Add flag to track redirect state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarHelper.hideStatusBar(this);

        initViews();
        setupViewModel();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observe login success
        viewModel.loginSuccess.observe(this, success -> {
            if (success != null && success) {
                // Set redirecting flag to prevent isLoading observer from interfering
                isRedirecting = true;

                // Keep button disabled during redirect
                btnLogin.setEnabled(false);
                btnLogin.setText(R.string.logging_in);

                Toast.makeText(this, "Login successful! Redirecting...", Toast.LENGTH_SHORT).show();
                new Handler(getMainLooper()).postDelayed(this::handleLoginSuccess, 2000);
                // Clear success state to prevent repeated navigation
                viewModel.clearLoginSuccess();
            }
        });

        // Observe login errors
        viewModel.loginError.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                // Clear error after showing it
                viewModel.clearLoginError();
            }
        });

        // Observe password reset success
        viewModel.passwordResetSuccess.observe(this, success -> {
            if (success != null && success) {
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                // Clear success state
                viewModel.clearPasswordResetSuccess();
            }
        });

        // Observe password reset errors
        viewModel.passwordResetError.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Failed to send reset email: " + error, Toast.LENGTH_LONG).show();
                // Clear error after showing it
                viewModel.clearPasswordResetError();
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading != null && !isRedirecting) { // Check redirecting flag
                btnLogin.setEnabled(!isLoading);
                btnLogin.setText(isLoading ? R.string.logging_in : R.string.login);
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (!validateInputs()) {
                return;
            }
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            viewModel.loginUser(email, password);
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Enter your email to reset password.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.resetPassword(email);
        });
    }

    private boolean validateInputs() {
        TextInputEditText[] fields = {etEmail, etPassword};
        String[] errorMessages = {"Email required", "Password required"};
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

        return !hasError;
    }

    private void handleLoginSuccess() {
        isRedirecting = true; // Set redirecting flag

        // Initialize FCM token for the logged-in user (handles regeneration if needed)
        FCMTokenManager.getInstance(this).initializeToken();

        viewModel.getUserRepository().checkIsAdmin(new UserRepository.IsAdminCallback() {
            @Override
            public void onIsAdminResult(boolean isAdmin) {
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(LoginActivity.this, AdminHomepageActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomepageActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onIsAdminFailed() {
                // Default to regular homepage if admin check fails
                startActivity(new Intent(LoginActivity.this, HomepageActivity.class));
                finish();
            }
        });
    }
}