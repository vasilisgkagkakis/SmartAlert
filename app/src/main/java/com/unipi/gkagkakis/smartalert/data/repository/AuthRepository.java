package com.unipi.gkagkakis.smartalert.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;

public class AuthRepository {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public Task<AuthResult> registerUser(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
}