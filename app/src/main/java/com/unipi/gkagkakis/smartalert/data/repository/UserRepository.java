package com.unipi.gkagkakis.smartalert.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> saveUser(FirebaseUser user, String fullName, String phone) {
        String uid = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", user.getEmail());
        userData.put("phone", phone);
        userData.put("createdAt", System.currentTimeMillis());
        return db.collection("users").document(uid).set(userData);
    }
}