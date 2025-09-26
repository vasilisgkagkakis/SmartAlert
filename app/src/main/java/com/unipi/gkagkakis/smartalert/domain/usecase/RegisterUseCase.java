package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.gkagkakis.smartalert.domain.repository.AuthRepository;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

import java.util.Objects;

public class RegisterUseCase {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public RegisterUseCase(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public Task<Void> register(String email, String password, String fullName, String phone) {
        return authRepository.register(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        // Pass all fields to Firestore
                        return userRepository.saveUser(user, fullName, phone);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }
}