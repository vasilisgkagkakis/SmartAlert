package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.LoginUseCase;
import com.unipi.gkagkakis.smartalert.domain.usecase.InitializeLocationUseCase;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _loginError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _passwordResetSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _passwordResetError = new MutableLiveData<>();

    // Public read-only LiveData for UI observation
    public final LiveData<Boolean> loginSuccess = _loginSuccess;
    public final LiveData<String> loginError = _loginError;
    public final LiveData<Boolean> isLoading = _isLoading;
    public final LiveData<Boolean> passwordResetSuccess = _passwordResetSuccess;
    public final LiveData<String> passwordResetError = _passwordResetError;

    private final LoginUseCase loginUseCase;
    private final UserRepository userRepository;
    private final InitializeLocationUseCase initializeLocationUseCase;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
        this.loginUseCase = new LoginUseCase(new AuthRepositoryImpl());
        this.initializeLocationUseCase = new InitializeLocationUseCase(application);
    }

    public void loginUser(String email, String password) {
        // Clear previous error state
        _loginError.setValue(null);
        _isLoading.setValue(true);

        loginUseCase.login(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Initialize location tracking for the logged-in user
                        initializeLocationUseCase.initializeUserLocation();

                        // Preload user data before navigating
                        userRepository.preloadUserData(new UserRepository.UserDataCallback() {
                            @Override
                            public void onUserDataLoaded() {

                                _isLoading.setValue(false);
                                _loginSuccess.postValue(true);
                            }

                            @Override
                            public void onUserDataFailed() {
                                // Still navigate even if preload fails
                                _isLoading.setValue(false);
                                _loginSuccess.postValue(true);
                            }
                        });
                    } else {
                        _isLoading.setValue(false);
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed. Please try again.";
                        _loginError.setValue(errorMessage);
                    }
                });
    }

    public void resetPassword(String email) {
        _passwordResetError.setValue(null);
        _passwordResetSuccess.setValue(false);
        _isLoading.setValue(true);

        // Simulate password reset logic
        loginUseCase.resetPassword(email)
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        _passwordResetSuccess.setValue(true);
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Password reset failed. Please try again.";
                        _passwordResetError.setValue(errorMessage);
                    }
                });
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    /**
     * Clears the login success state
     * Useful for preventing repeated navigation
     */
    public void clearLoginSuccess() {
        _loginSuccess.setValue(false);
    }

    /**
     * Clears the login error state
     */
    public void clearLoginError() {
        _loginError.setValue(null);
    }

    /**
     * Clears the password reset success state
     */
    public void clearPasswordResetSuccess() {
        _passwordResetSuccess.setValue(false);
    }

    /**
     * Clears the password reset error state
     */
    public void clearPasswordResetError() {
        _passwordResetError.setValue(null);
    }
}