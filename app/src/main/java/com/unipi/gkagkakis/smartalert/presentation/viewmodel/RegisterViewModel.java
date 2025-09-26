package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.unipi.gkagkakis.smartalert.data.repository.AuthRepository;
import com.unipi.gkagkakis.smartalert.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository authRepository = new AuthRepository();
    private final UserRepository userRepository = new UserRepository();

    public MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    public MutableLiveData<String> registrationError = new MutableLiveData<>();

    public void registerUser(String email, String password, String fullName, String phone) {
        authRepository.registerUser(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            userRepository.saveUser(user, fullName, phone)
                                    .addOnSuccessListener(aVoid -> registrationSuccess.postValue(true))
                                    .addOnFailureListener(e -> registrationError.postValue("Failed to save user data: " + e.getMessage()));
                        } else {
                            registrationError.postValue("User is null after registration.");
                        }
                    } else {
                        registrationError.postValue(task.getException() != null ? task.getException().getMessage() : "Registration failed.");
                    }
                });
    }
}