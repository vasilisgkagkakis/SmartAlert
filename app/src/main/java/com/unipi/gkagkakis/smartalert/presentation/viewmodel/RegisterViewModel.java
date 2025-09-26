package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.RegisterUseCase;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepositoryImpl;

public class RegisterViewModel extends ViewModel {
    public final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> registrationError = new MutableLiveData<>();
    private final RegisterUseCase registerUseCase;

    public RegisterViewModel() {
        this.registerUseCase = new RegisterUseCase(
                new AuthRepositoryImpl(),
                new UserRepositoryImpl()
        );
    }

    public void registerUser(String email, String password, String fullName, String phone) {
        registerUseCase.register(email, password, fullName, phone)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        registrationSuccess.setValue(true);
                    } else {
                        registrationError.setValue(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                    }
                });
    }
}