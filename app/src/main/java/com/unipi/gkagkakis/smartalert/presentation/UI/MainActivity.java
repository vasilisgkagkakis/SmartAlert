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
import com.unipi.gkagkakis.smartalert.service.LocationTrackingService;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;
    private UserRepository userRepository;
    private LocationTrackingService locationTrackingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarHelper.hideStatusBar(this);


        initViews();
        setupClickListeners();
        AnimationHelper.startLogoAnimation(this, findViewById(R.id.logo), R.anim.logo_up_and_down);

        userRepository = new UserRepositoryImpl(this);
        locationTrackingService = LocationTrackingService.getInstance(this);

        // Initialize FCM and request notification permission
        NotificationPermissionHelper.requestNotificationPermission(this);

        // Request location permissions and start tracking
        requestLocationPermissionsAndStartTracking();

        if (userRepository.isUserAuthenticated()) {
            checkUserTypeAndNavigate();
        } else {
            Toast.makeText(this, "Welcome! Please log in or register.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermissionsAndStartTracking() {
        if (locationTrackingService.hasLocationPermissions()) {
            // Basic permissions already granted, check for background permission
            if (locationTrackingService.hasBackgroundLocationPermission()) {
                // All permissions granted, start continuous tracking
                locationTrackingService.startContinuousLocationTracking();
            } else {
                // Request background location permission
                locationTrackingService.requestBackgroundLocationPermission(this);
            }
        } else {
            // Request basic location permissions first
            locationTrackingService.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle notification permissions
        NotificationPermissionHelper.handlePermissionResult(this, requestCode, permissions, grantResults);

        // Handle location permissions
        locationTrackingService.handlePermissionResult(requestCode, permissions, grantResults);

        // After handling basic location permissions, request background permission
        if (requestCode == LocationTrackingService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationTrackingService.hasLocationPermissions()) {
                // Basic permissions granted, now request background permission
                locationTrackingService.requestBackgroundLocationPermission(this);
            }
        }
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