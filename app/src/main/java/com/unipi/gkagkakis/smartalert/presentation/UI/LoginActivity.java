package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;

import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ActivityLifecycle", "onCreate: " + getClass().getSimpleName());

        setContentView(R.layout.activity_login);
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
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            // Add login logic here
            startActivity(new Intent(this, MainActivity.class));
            finish();
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
}