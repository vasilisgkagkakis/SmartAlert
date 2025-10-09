package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.AnimationHelper;
import com.unipi.gkagkakis.smartalert.Utils.NotificationPermissionHelper;
import com.unipi.gkagkakis.smartalert.Utils.StatusBarHelper;
import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.hideStatusBar(this);


        initViews();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);

        userRepository = new UserRepositoryImpl(this);

        // Initialize FCM and request notification permission
        NotificationPermissionHelper.requestNotificationPermission(this);

        if (userRepository.isUserAuthenticated()) {
            checkUserTypeAndNavigate();
        } else {
            Toast.makeText(this, "Welcome! Please log in or register.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        NotificationPermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults);
    }

    private void checkUserTypeAndNavigate() {
        userRepository.checkIsAdmin(new UserRepository.IsAdminCallback() {
            @Override
            public void onIsAdminResult(boolean isAdmin) {
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, HomepageActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onIsAdminFailed() {
                // Default to regular homepage if admin check fails
                startActivity(new Intent(MainActivity.this, HomepageActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}