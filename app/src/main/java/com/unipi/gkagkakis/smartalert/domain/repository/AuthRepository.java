package com.unipi.gkagkakis.smartalert.domain.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public interface AuthRepository {
    Task<AuthResult> login(String email, String password);
    Task<AuthResult> register(String email, String password);
    Task<Void> sendPasswordResetEmail(String email);
}