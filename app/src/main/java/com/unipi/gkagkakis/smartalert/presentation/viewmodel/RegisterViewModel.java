package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.RegisterUseCase;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepositoryImpl;

public class RegisterViewModel extends ViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<Boolean> _registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _registrationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);

    // Public read-only LiveData for UI observation
    public final LiveData<Boolean> registrationSuccess = _registrationSuccess;
    public final LiveData<String> registrationError = _registrationError;
    public final LiveData<Boolean> isLoading = _isLoading;

    private final RegisterUseCase registerUseCase;

    public RegisterViewModel() {
        this.registerUseCase = new RegisterUseCase(
                new AuthRepositoryImpl(),
                new UserRepositoryImpl()
        );
    }

    public void registerUser(String email, String password, String fullName, String phone) {
        // Clear previous error state
        _registrationError.setValue(null);
        _isLoading.setValue(true);

        registerUseCase.register(email, password, fullName, phone)
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false);

                    if (task.isSuccessful()) {
                        _registrationSuccess.setValue(true);
                    } else {
                        String errorMessage = task.getException() != null
                            ? task.getException().getMessage()
                            : "Registration failed. Please try again.";
                        _registrationError.setValue(errorMessage);
                    }
                });
    }

    public void clearRegistrationSuccess() {
        _registrationSuccess.setValue(false);
    }

    public void clearRegistrationError() {
        _registrationError.setValue(null);
    }
}