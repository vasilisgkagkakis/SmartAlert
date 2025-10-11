package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.gkagkakis.smartalert.data.repository.SubmittedAlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.data.service.ImageStorageService;
import com.unipi.gkagkakis.smartalert.data.service.Base64ImageService;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;

/**
 * Use case for creating alerts following clean architecture
 * Handles the business logic for alert creation with image processing
 */
public class CreateAlertUseCase {

    public interface CreateAlertCallback {
        void onSuccess(@NonNull String alertId);
        void onError(@NonNull String errorMessage);
        void onProgress(int progress);
    }

    private final SubmittedAlertRepository repository;
    private final ImageStorageService imageStorageService;
    private final Base64ImageService base64ImageService;

    public CreateAlertUseCase() {
        this.repository = SubmittedAlertRepositoryImpl.getInstance();
        this.imageStorageService = ImageStorageService.getInstance();
        this.base64ImageService = Base64ImageService.getInstance();
    }

    public void createAlert(Context context, String type, String severity, String location,
                           String description, @Nullable Uri imageUri, CreateAlertCallback callback) {

        // Validation with better error messages
        if (type == null || type.trim().isEmpty()) {
            callback.onError("Alert type is required.");
            return;
        }
        if (severity == null || severity.trim().isEmpty()) {
            callback.onError("Alert severity is required.");
            return;
        }
        if (location == null || location.trim().isEmpty()) {
            callback.onError("Alert location is required.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            callback.onError("Alert description is required.");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("User not authenticated.");
            return;
        }

        callback.onProgress(0);

        if (imageUri != null) {
            // Try Firebase Storage first, fallback to Base64 if it fails
            tryFirebaseStorageUpload(context, imageUri, type, severity, location, description, user.getUid(), callback);
        } else {
            // Create alert without image
            createAlertWithImageUrl(type, severity, location, description, null, user.getUid(), callback);
        }
    }

    private void tryFirebaseStorageUpload(Context context, Uri imageUri, String type, String severity,
                                        String location, String description, String userId, CreateAlertCallback callback) {

        imageStorageService.uploadImage(context, imageUri, new ImageStorageService.ImageUploadCallback() {
            @Override
            public void onSuccess(@NonNull String downloadUrl) {
                Log.d("CreateAlertUseCase", "Firebase Storage upload successful");
                createAlertWithImageUrl(type, severity, location, description, downloadUrl, userId, callback);
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Firebase Storage failed, try Base64 fallback
                Log.w("CreateAlertUseCase", "Firebase Storage failed, using Base64 fallback: " + e.getMessage());

                tryBase64Fallback(context, imageUri, type, severity, location, description, userId, callback);
            }

            @Override
            public void onProgress(int progress) {
                // Only report up to 80% for Firebase Storage phase
                callback.onProgress(Math.min(progress * 80 / 100, 80));
            }
        });
    }

    private void tryBase64Fallback(Context context, Uri imageUri, String type, String severity,
                                 String location, String description, String userId, CreateAlertCallback callback) {

        base64ImageService.convertImageToBase64(context, imageUri, new Base64ImageService.ImageConversionCallback() {
            @Override
            public void onSuccess(@NonNull String base64Image) {
                Log.d("CreateAlertUseCase", "Base64 conversion successful, creating alert");
                createAlertWithImageUrl(type, severity, location, description, base64Image, userId, callback);
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Both methods failed, offer user choice
                Log.e("CreateAlertUseCase", "Both Firebase Storage and Base64 failed: " + e.getMessage(), e);

                // Use main thread handler to show user-friendly error
                new Handler(Looper.getMainLooper()).post(() -> {
                    String errorMessage = "Image upload failed. Would you like to submit the alert without the image?";

                    // For now, automatically create without image after showing error
                    callback.onError(errorMessage);

                    // After 2 seconds, create the alert without image
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.d("CreateAlertUseCase", "Creating alert without image due to upload failures");
                        createAlertWithImageUrl(type, severity, location, description, null, userId, callback);
                    }, 2000);
                });
            }

            @Override
            public void onProgress(int progress) {
                // Base64 conversion is from 80% to 95%
                callback.onProgress(80 + (progress * 15 / 100));
            }
        });
    }

    private void createAlertWithImageUrl(String type, String severity, String location,
                                       String description, @Nullable String imageUrl, String userId, CreateAlertCallback callback) {

        SubmittedAlert submittedAlert = new SubmittedAlert(
                null,
                type.trim(),
                severity.trim(),
                location.trim(),
                description.trim(),
                imageUrl,
                userId,
                null
        );

        repository.createSubmittedAlert(submittedAlert, new SubmittedAlertRepository.CreateSubmittedAlertCallback() {
            @Override
            public void onSuccess(@NonNull String alertId) {
                callback.onProgress(100);
                callback.onSuccess(alertId);
            }

            @Override
            public void onError(@NonNull Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to submit alert for review.";
                callback.onError(errorMessage);
            }
        });
    }
}
