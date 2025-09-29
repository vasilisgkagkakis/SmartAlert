package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.LoginUseCase;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepositoryImpl;

public class LoginViewModel extends ViewModel {
    public final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> loginError = new MutableLiveData<>();
    private final LoginUseCase loginUseCase;

    public LoginViewModel() {
        this.loginUseCase = new LoginUseCase(
                new AuthRepositoryImpl(),
                new UserRepositoryImpl()
        );
    }

    public void loginUser(String email, String password) {
        loginUseCase.login(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginSuccess.setValue(true);
                    } else {
                        loginError.setValue(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }
}