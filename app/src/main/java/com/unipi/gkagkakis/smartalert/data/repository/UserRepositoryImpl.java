package com.unipi.gkagkakis.smartalert.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.gkagkakis.smartalert.Utils.SharedPreferencesHelper;
import com.unipi.gkagkakis.smartalert.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class UserRepositoryImpl implements UserRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final SharedPreferences prefs;

    public UserRepositoryImpl() {
        this.prefs = null;
    }

    public UserRepositoryImpl(Context context) {
        this.prefs = SharedPreferencesHelper.getSharedPreferences(context, "user_prefs");
    }

    @Override
    public Task<Void> saveUser(FirebaseUser user, String fullName, String phone) {
        return saveUser(user, fullName, phone, false);
    }

    @Override
    public Task<Void> saveUser(FirebaseUser user, String fullName, String phone, boolean isAdmin) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("phone", phone);
        userData.put("isAdmin", isAdmin);

        return firestore.collection("users")
                .document(user.getUid())
                .set(userData);
    }

    @Override
    public void getUserName(UserNameCallback callback) {
        String cachedName = "";
        if (!isUserAuthenticated()) {
            callback.onUserNotAuthenticated();
            return;
        }

        if (prefs != null) {
            cachedName = prefs.getString("fullName", "User");
            callback.onUserNameLoaded(cachedName);
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onUserNameLoaded(cachedName);
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.w("Firestore", "User doc not found id=" + userId);
                        callback.onUserNameLoaded("User");
                        return;
                    }
                    String fullName = doc.getString("fullName");
                    if (fullName == null || fullName.trim().isEmpty()) {
                        callback.onUserNameLoaded("User");
                        Log.w("Firestore", "fullName missing");
                    } else {
                        if (prefs != null) {
                            prefs.edit().putString("fullName", fullName).apply();
                        }
                        callback.onUserNameLoaded(fullName);
                        Log.d("Firestore", "Full Name: " + fullName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Fetch failed", e);
                    callback.onUserNameLoaded("User");
                });
    }

    @Override
    public void preloadUserData(UserDataCallback callback) {
        if (!isUserAuthenticated()) {
            callback.onUserDataFailed();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onUserDataFailed();
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("fullName");
                        String phone = doc.getString("phone");
                        if (prefs != null) {
                            SharedPreferences.Editor editor = prefs.edit();
                            if (fullName != null) {
                                editor.putString("fullName", fullName);
                            }
                            if (phone != null) {
                                editor.putString("phone", phone);
                            }
                            editor.apply();
                        }
                    }
                    callback.onUserDataLoaded();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Preload failed", e);
                    callback.onUserDataFailed();
                });
    }

    @Override
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    @Override
    public void checkIsAdmin(IsAdminCallback callback) {
        if (!isUserAuthenticated()) {
            callback.onIsAdminFailed();
            return;
        }

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onIsAdminFailed();
            return;
        }

        String userId = currentUser.getUid();

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean isAdmin = doc.getBoolean("isAdmin");
                        boolean adminStatus = isAdmin != null ? isAdmin : false;
                        callback.onIsAdminResult(adminStatus);
                    } else {
                        callback.onIsAdminResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Admin check failed", e);
                    callback.onIsAdminFailed();
                });
    }

    @Override
    public void logout() {
        firebaseAuth.signOut();
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}