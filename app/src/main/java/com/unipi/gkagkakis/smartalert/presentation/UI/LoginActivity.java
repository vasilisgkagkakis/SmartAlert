package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.LoginViewModel;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private LoginViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarHelper.hideStatusBar(this);
        initViews();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.loginSuccess.observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Login successful! Redirecting...", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    handleLoginSuccess();
                }, 2000);
            }
        });

        viewModel.loginError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if(!validateInputs()) {
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
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                            Log.d("LoginActivity", FirebaseAuth.getInstance().toString());
                            Log.d("LoginActivity", "Password reset email sent to: " + email);
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                            Log.w("LoginActivity", "Failed to send password reset email.", task.getException());
                        }
                    });
        });
    }

    private boolean validateInputs() {
        TextInputEditText[] fields = {etEmail, etPassword};
        String[] errorMessages = {"Email required", "Password required"
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

    private void handleLoginSuccess() {
        // Initialize FCM token for the logged-in user (handles regeneration if needed)
        com.unipi.gkagkakis.smartalert.service.FCMTokenManager.getInstance(this).initializeToken();

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