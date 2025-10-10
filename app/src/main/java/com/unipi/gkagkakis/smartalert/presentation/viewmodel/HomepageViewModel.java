package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;
import com.unipi.gkagkakis.smartalert.domain.usecase.PermissionUseCase;
import com.unipi.gkagkakis.smartalert.service.LocationTrackingService;

/**
 * ViewModel for HomepageActivity following MVVM pattern
 * Handles user name loading and location tracking management
 */
public class HomepageViewModel extends AndroidViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<String> _userName = new MutableLiveData<>();
    private final MutableLiveData<String> _userNameOnly = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _shouldNavigateToLogin = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _locationPermissionRequired = new MutableLiveData<>();

    // Public read-only LiveData for UI observation
    public final LiveData<String> userName = _userName;
    public final LiveData<String> userNameOnly = _userNameOnly;
    public final LiveData<Boolean> shouldNavigateToLogin = _shouldNavigateToLogin;
    public final LiveData<String> error = _error;
    public final LiveData<Boolean> locationPermissionRequired = _locationPermissionRequired;

    private final UserRepository userRepository;
    private final PermissionUseCase permissionUseCase;
    private final LocationTrackingService locationTrackingService;

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
        this.permissionUseCase = new PermissionUseCase(application);
        this.locationTrackingService = LocationTrackingService.getInstance(application);
    }

    /**
     * Checks if user is authenticated and loads their name
     */
    public void checkUserAndLoadName() {
        if (!userRepository.isUserAuthenticated()) {
            _shouldNavigateToLogin.setValue(true);
            return;
        }

        userRepository.getUserName(new UserRepository.UserNameCallback() {
            @Override
            public void onUserNameLoaded(String name) {
                _userName.setValue("Welcome, " + name + "!");
                _userNameOnly.setValue(name); // Set the username without prefix
            }

            @Override
            public void onUserNotAuthenticated() {
                _shouldNavigateToLogin.setValue(true);
            }
        });
    }

    /**
     * Updates user location when homepage is opened/resumed
     */
    public void updateUserLocation(android.app.Activity activity) {
        if (locationTrackingService.hasLocationPermissions()) {
            // Start continuous tracking if we have permissions
            if (locationTrackingService.hasBackgroundLocationPermission()) {
                locationTrackingService.startContinuousLocationTracking();
            } else {
                // Request background permission and start basic tracking
                locationTrackingService.requestBackgroundLocationPermission(activity);
                locationTrackingService.updateLocationNow();
            }
        } else {
            // Signal that location permissions are needed
            _locationPermissionRequired.setValue(true);
        }
    }

    /**
     * Handles permission results through PermissionUseCase
     */
    public void handlePermissionResult(android.app.Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        permissionUseCase.handlePermissionResult(activity, requestCode, permissions, grantResults);
    }

    /**
     * Requests location permissions through PermissionUseCase
     */
    public void requestLocationPermissions(android.app.Activity activity) {
        permissionUseCase.requestLocationPermissionsAndStartTracking(activity);
    }

    /**
     * Logs out the current user
     */
    public void logout() {
        userRepository.logout();
    }

    // Getter methods for backward compatibility with existing code
    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<Boolean> getShouldNavigateToLogin() {
        return shouldNavigateToLogin;
    }

    /**
     * Clears navigation state
     */
    public void clearNavigationToLogin() {
        _shouldNavigateToLogin.setValue(false);
    }

    /**
     * Clears error state
     */
    public void clearError() {
        _error.setValue(null);
    }

    /**
     * Clears location permission required state
     */
    public void clearLocationPermissionRequired() {
        _locationPermissionRequired.setValue(false);
    }
}

