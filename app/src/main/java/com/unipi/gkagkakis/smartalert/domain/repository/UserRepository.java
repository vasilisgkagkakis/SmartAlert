package com.unipi.gkagkakis.smartalert.domain.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public interface UserRepository {
    Task<Void> saveUser(FirebaseUser user, String fullName, String phone);
}