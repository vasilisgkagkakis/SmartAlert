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
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.registrationSuccess.observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Registration successful! Redirecting...", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }, 2000);
            }
        });

        viewModel.registrationError.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
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
                String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

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

        for (int i = 0; i < fields.length; i++) {
            if (TextUtils.isEmpty(fields[i].getText())) {
                fields[i].setError(errorMessages[i]);
                hasError = true;
            } else {
                fields[i].setError(null);
            }
        }

        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            hasError = true;
        }

        return !hasError;
    }
}