package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.unipi.gkagkakis.smartalert.data.repository.UserRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.LoginUseCase;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    public final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public final MutableLiveData<String> loginError = new MutableLiveData<>();
    private final LoginUseCase loginUseCase;
    private final UserRepository userRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
        this.loginUseCase = new LoginUseCase(
                new AuthRepositoryImpl(),
                new UserRepositoryImpl()
        );
    }

    public void loginUser(String email, String password) {
        loginUseCase.login(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Preload user data before navigating
                        userRepository.preloadUserData(new UserRepository.UserDataCallback() {
                            @Override
                            public void onUserDataLoaded() {
                                loginSuccess.postValue(true);
                            }

                            @Override
                            public void onUserDataFailed() {
                                // Still navigate even if preload fails
                                loginSuccess.postValue(true);
                            }
                        });
                    } else {
                        loginError.setValue(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}