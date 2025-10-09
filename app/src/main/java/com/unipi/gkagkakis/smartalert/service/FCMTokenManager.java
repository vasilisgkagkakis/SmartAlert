package com.unipi.gkagkakis.smartalert.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static final String PREF_NAME = "fcm_token_prefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private static FCMTokenManager instance;
    private Context context;
    private SharedPreferences preferences;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public static synchronized FCMTokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new FCMTokenManager(context);
        }
        return instance;
    }

    public void initializeToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Registration Token: " + token);

                    saveToken(token);
                    sendTokenToServer(token);
                });
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token saved locally");
    }

    public String getToken() {
        return preferences.getString(KEY_FCM_TOKEN, null);
    }

    public void sendTokenToServer(String token) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated, cannot save token to server");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("timestamp", System.currentTimeMillis());
        tokenData.put("platform", "android");

        firestore.collection("users")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firestore"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save FCM token to Firestore", e);
                    // If update fails, try to set the token (in case document doesn't exist)
                    firestore.collection("users")
                            .document(userId)
                            .set(tokenData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> Log.d(TAG, "FCM token set in Firestore"))
                            .addOnFailureListener(e2 -> Log.e(TAG, "Failed to set FCM token in Firestore", e2));
                });
    }

    public void deleteToken() {
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        preferences.edit().remove(KEY_FCM_TOKEN).apply();
                        Log.d(TAG, "FCM token deleted");

                        // Also remove from server
                        if (auth.getCurrentUser() != null) {
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("fcmToken", null);

                            firestore.collection("users")
                                    .document(userId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token removed from server"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove FCM token from server", e));
                        }
                    } else {
                        Log.e(TAG, "Failed to delete FCM token", task.getException());
                    }
                });
    }
}
