package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;
import com.unipi.gkagkakis.smartalert.domain.usecase.InitializeLocationUseCase;
import com.unipi.gkagkakis.smartalert.domain.usecase.PermissionUseCase;

/**
 * ViewModel for MainActivity following MVVM pattern
 * Handles user authentication checking and navigation logic
 */
public class MainViewModel extends AndroidViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<Boolean> _isUserAuthenticated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isAdmin = new MutableLiveData<>();
    private final MutableLiveData<String> _navigationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    // Public read-only LiveData for UI observation
    public final LiveData<Boolean> isUserAuthenticated = _isUserAuthenticated;
    public final LiveData<Boolean> isAdmin = _isAdmin;
    public final LiveData<String> navigationError = _navigationError;
    public final LiveData<Boolean> isLoading = _isLoading;

    private final UserRepository userRepository;
    private final InitializeLocationUseCase initializeLocationUseCase;
    private final PermissionUseCase permissionUseCase;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
        this.initializeLocationUseCase = new InitializeLocationUseCase(application);
        this.permissionUseCase = new PermissionUseCase(application);
    }

    /**
     * Checks if user is authenticated and determines navigation
     */
    public void checkAuthenticationAndNavigate() {
        _isLoading.setValue(true);
        _navigationError.setValue(null);

        boolean authenticated = userRepository.isUserAuthenticated();
        _isUserAuthenticated.setValue(authenticated);

        if (authenticated) {
            // Initialize location tracking for authenticated user
            initializeLocationUseCase.initializeUserLocation();

            // Check admin status
            userRepository.checkIsAdmin(new UserRepository.IsAdminCallback() {
                @Override
                public void onIsAdminResult(boolean isAdmin) {
                    _isLoading.setValue(false);
                    _isAdmin.setValue(isAdmin);
                }

                @Override
                public void onIsAdminFailed() {
                    _isLoading.setValue(false);
                    _isAdmin.setValue(false); // Default to regular user
                }
            });
        } else {
            _isLoading.setValue(false);
        }
    }

    /**
     * Requests initial permissions through use case
     */
    public void requestInitialPermissions(android.app.Activity activity) {
        permissionUseCase.requestInitialPermissions(activity);
    }

    /**
     * Handles permission results through use case
     */
    public void handlePermissionResult(android.app.Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        permissionUseCase.handlePermissionResult(activity, requestCode, permissions, grantResults);
    }

    /**
     * Clears navigation error state
     */
    public void clearNavigationError() {
        _navigationError.setValue(null);
    }
}
