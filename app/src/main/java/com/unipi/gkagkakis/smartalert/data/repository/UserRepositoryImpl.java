package com.unipi.gkagkakis.smartalert.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class UserRepositoryImpl implements UserRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public Task<Void> saveUser(FirebaseUser user, String fullName, String phone) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", user.getEmail());
        userData.put("phone", phone);

        return firestore.collection("users")
                .document(user.getUid())
                .set(userData);
    }
}