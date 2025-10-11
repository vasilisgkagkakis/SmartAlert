package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.content.Context;
import android.net.Uri;
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

        // Validation
        if (type == null || type.trim().isEmpty()) {
            callback.onError("Type is required.");
            return;
        }
        if (severity == null || severity.trim().isEmpty()) {
            callback.onError("Severity is required.");
            return;
        }
        if (location == null || location.trim().isEmpty()) {
            callback.onError("Location is required.");
            return;
        }
        if (description == null || description.trim().isEmpty()) {
            callback.onError("Description is required.");
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
                createAlertWithImageUrl(type, severity, location, description, downloadUrl, userId, callback);
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Firebase Storage failed, try Base64 fallback
                android.util.Log.d("CreateAlertUseCase", "Firebase Storage failed, using Base64 fallback: " + e.getMessage());

                base64ImageService.convertImageToBase64(context, imageUri, new Base64ImageService.ImageConversionCallback() {
                    @Override
                    public void onSuccess(@NonNull String base64Image) {
                        android.util.Log.d("CreateAlertUseCase", "Base64 conversion successful, creating alert");
                        createAlertWithImageUrl(type, severity, location, description, base64Image, userId, callback);
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        // Both methods failed, create without image
                        android.util.Log.e("CreateAlertUseCase", "Both Firebase Storage and Base64 failed, creating without image", e);
                        callback.onError("Image processing failed. Creating alert without image...");

                        // Wait a moment then create without image
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> createAlertWithImageUrl(type, severity, location, description, null, userId, callback), 1000);
                    }

                    @Override
                    public void onProgress(int progress) {
                        callback.onProgress(progress);
                    }
                });
            }

            @Override
            public void onProgress(int progress) {
                callback.onProgress(progress);
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
