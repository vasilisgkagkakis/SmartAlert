package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

/**
 * ViewModel for AdminHomepageActivity following MVVM pattern
 * Handles admin user authentication and navigation logic
 */
public class AdminHomepageViewModel extends AndroidViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<String> _userName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _shouldNavigateToLogin = new MutableLiveData<>(false);
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    // Public read-only LiveData for UI observation
    public final LiveData<String> userName = _userName;
    public final LiveData<Boolean> shouldNavigateToLogin = _shouldNavigateToLogin;
    public final LiveData<String> error = _error;
    public final LiveData<Boolean> isLoading = _isLoading;

    private final UserRepository userRepository;

    public AdminHomepageViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
    }

    /**
     * Checks if user is authenticated and loads their name
     */
    public void checkUserAndLoadName() {
        if (!userRepository.isUserAuthenticated()) {
            _shouldNavigateToLogin.setValue(true);
            return;
        }

        // First verify user is still an admin
        userRepository.checkIsAdmin(new UserRepository.IsAdminCallback() {
            @Override
            public void onIsAdminResult(boolean isAdmin) {
                if (!isAdmin) {
                    // User is no longer admin, redirect to login
                    _shouldNavigateToLogin.setValue(true);
                    return;
                }

                // User is admin, load their name
                loadUserName();
            }

            @Override
            public void onIsAdminFailed() {
                // Admin check failed, redirect to login for safety
                _shouldNavigateToLogin.setValue(true);
            }
        });
    }

    private void loadUserName() {
        userRepository.getUserName(new UserRepository.UserNameCallback() {
            @Override
            public void onUserNameLoaded(String name) {
                // Check if the name already contains "Admin" to avoid duplication
                String displayName;

                if (name.toLowerCase().contains("admin")) {
                    // Name already contains "Admin", use as is
                    displayName = "Welcome, " + name + "!";
                } else {
                    // Add "Admin" prefix to the name
                    displayName = "Welcome, Admin " + name + "!";
                }

                _userName.setValue(displayName);
            }

            @Override
            public void onUserNotAuthenticated() {
                _shouldNavigateToLogin.setValue(true);
            }
        });
    }

    /**
     * Logs out the current admin user
     */
    public void logout() {
        userRepository.logout();
        _shouldNavigateToLogin.setValue(true);
    }

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
}