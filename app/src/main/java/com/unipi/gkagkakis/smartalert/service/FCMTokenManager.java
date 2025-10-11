package com.unipi.gkagkakis.smartalert.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FCMTokenManager {
    private static final String TAG = "FCMTokenManager";
    private static final String PREF_NAME = "fcm_token_prefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_DEVICE_ID = "device_id";

    private static FCMTokenManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

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
        // First check if current user already has a valid token in Firestore
        if (auth.getCurrentUser() != null) {
            checkAndRegenerateTokenIfNeeded();
        }

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

    private void checkAndRegenerateTokenIfNeeded() {
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    String existingToken = document.getString("fcmToken");
                    String clearedReason = document.getString("tokenClearedReason");

                    if (existingToken == null && clearedReason != null) {
                        Log.i(TAG, "User " + userId + " lost token due to: " + clearedReason + ". Regenerating new token.");

                        // Force regenerate a new FCM token - skip deleteToken since token is already null
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String newToken = tokenTask.getResult();
                                        Log.i(TAG, "Generated new FCM token for user " + userId + ": " + newToken);

                                        saveToken(newToken);

                                        // Clear the conflict reason and save new token
                                        Map<String, Object> tokenData = new HashMap<>();
                                        tokenData.put("fcmToken", newToken);
                                        tokenData.put("timestamp", System.currentTimeMillis());
                                        tokenData.put("platform", "android");
                                        tokenData.put("deviceId", getDeviceId());
                                        tokenData.put("tokenClearedReason", null);
                                        tokenData.put("tokenClearedAt", null);
                                        tokenData.put("tokenRegeneratedAt", System.currentTimeMillis());

                                        firestore.collection("users")
                                                .document(userId)
                                                .update(tokenData)
                                                .addOnSuccessListener(aVoid ->
                                                        Log.i(TAG, "New FCM token saved for user " + userId))
                                                .addOnFailureListener(e ->
                                                        Log.e(TAG, "Failed to save new FCM token", e));
                                    } else {
                                        Log.e(TAG, "Failed to generate new FCM token", tokenTask.getException());
                                    }
                                });
                    } else if (existingToken != null) {
                        Log.d(TAG, "User " + userId + " already has valid token: " + existingToken.substring(0, Math.min(20, existingToken.length())) + "...");
                    } else {
                        Log.d(TAG, "User " + userId + " has no token, will get one through normal flow");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to check user token status", e));
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token saved locally");
    }

    public void sendTokenToServer(String token) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated, cannot save token to server");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // First check if this token is already used by another user
        checkForTokenConflict(token, userId);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("timestamp", System.currentTimeMillis());
        tokenData.put("platform", "android");
        tokenData.put("deviceId", getDeviceId()); // Add device identifier

        firestore.collection("users")
                .document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firestore"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save FCM token to Firestore", e);
                    // If update fails, try to set the token (in case document doesn't exist)
                    firestore.collection("users")
                            .document(userId)
                            .set(tokenData, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> Log.d(TAG, "FCM token set in Firestore"))
                            .addOnFailureListener(e2 -> Log.e(TAG, "Failed to set FCM token in Firestore", e2));
                });
    }

    private void checkForTokenConflict(String token, String currentUserId) {
        firestore.collection("users")
                .whereEqualTo("fcmToken", token)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String existingUserId = document.getId();
                        if (!existingUserId.equals(currentUserId)) {
                            Log.w(TAG, "FCM Token conflict detected! Token is shared between users: "
                                    + currentUserId + " and " + existingUserId);

                            // Clear the token from the other user
                            Map<String, Object> clearToken = new HashMap<>();
                            clearToken.put("fcmToken", null);
                            clearToken.put("tokenClearedReason", "Token conflict detected");
                            clearToken.put("tokenClearedAt", System.currentTimeMillis());

                            firestore.collection("users")
                                    .document(existingUserId)
                                    .update(clearToken)
                                    .addOnSuccessListener(aVoid ->
                                            Log.i(TAG, "Cleared conflicting token from user: " + existingUserId))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to clear conflicting token", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check for token conflicts", e));
    }

    private String getDeviceId() {
        // Use a UUID-based device identifier stored in SharedPreferences
        // This is more privacy-friendly and reliable than ANDROID_ID
        String deviceId = preferences.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            // Generate a new UUID for this device/app installation
            deviceId = UUID.randomUUID().toString();
            preferences.edit().putString(KEY_DEVICE_ID, deviceId).apply();
            Log.d(TAG, "Generated new device ID: " + deviceId);
        }

        return deviceId;
    }
}
