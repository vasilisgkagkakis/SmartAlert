package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.unipi.gkagkakis.smartalert.domain.repository.AuthRepository;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

import java.util.Objects;

public class LoginUseCase {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public LoginUseCase(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }

    public Task<Void> login(String email, String password) {
        return authRepository.login(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(Objects.requireNonNull(task.getException()));
                    }
                    return Tasks.forResult(null);
                });
    }
}
