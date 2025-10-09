package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.data.repository.SubmittedAlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.data.service.ImageStorageService;
import com.unipi.gkagkakis.smartalert.data.service.Base64ImageService;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;

public class AlertViewModel extends ViewModel {

    private final SubmittedAlertRepositoryImpl repository;
    private final ImageStorageService imageStorageService;
    private final Base64ImageService base64ImageService;

    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> createdId = new MutableLiveData<>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>(0);

    public AlertViewModel() {
        this.repository = SubmittedAlertRepositoryImpl.getInstance();
        this.imageStorageService = ImageStorageService.getInstance();
        this.base64ImageService = Base64ImageService.getInstance();
    }

    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<String> getCreatedId() { return createdId; }
    public LiveData<String> getError() { return error; }
    public LiveData<Integer> getUploadProgress() { return uploadProgress; }

    public void createAlert(Context context, String type, String severity, String location, String description, @Nullable Uri imageUri) {
        error.postValue(null);

        if (type == null || type.trim().isEmpty()) { error.postValue("Type is required."); return; }
        if (severity == null || severity.trim().isEmpty()) { error.postValue("Severity is required."); return; }
        if (location == null || location.trim().isEmpty()) { error.postValue("Location is required."); return; }
        if (description == null || description.trim().isEmpty()) { error.postValue("Description is required."); return; }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { error.postValue("User not authenticated."); return; }

        saving.postValue(true);
        uploadProgress.postValue(0);

        if (imageUri != null) {
            // Try Firebase Storage first, fallback to Base64 if it fails
            tryFirebaseStorageUpload(context, imageUri, type, severity, location, description, user.getUid());
        } else {
            // Create alert without image
            createAlertWithImageUrl(type, severity, location, description, null, user.getUid());
        }
    }

    private void tryFirebaseStorageUpload(Context context, Uri imageUri, String type, String severity, String location, String description, String userId) {
        imageStorageService.uploadImage(context, imageUri, new ImageStorageService.ImageUploadCallback() {
            @Override
            public void onSuccess(@NonNull String downloadUrl) {
                createAlertWithImageUrl(type, severity, location, description, downloadUrl, userId);
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Firebase Storage failed, try Base64 fallback
                android.util.Log.d("AlertViewModel", "Firebase Storage failed, using Base64 fallback: " + e.getMessage());

                base64ImageService.convertImageToBase64(context, imageUri, new Base64ImageService.ImageConversionCallback() {
                    @Override
                    public void onSuccess(@NonNull String base64Image) {
                        android.util.Log.d("AlertViewModel", "Base64 conversion successful, creating alert");
                        createAlertWithImageUrl(type, severity, location, description, base64Image, userId);
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        // Both methods failed, create without image
                        android.util.Log.e("AlertViewModel", "Both Firebase Storage and Base64 failed, creating without image", e);
                        error.postValue("Image processing failed. Creating alert without image...");

                        // Wait a moment then create without image
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            createAlertWithImageUrl(type, severity, location, description, null, userId);
                        }, 1000);
                    }

                    @Override
                    public void onProgress(int progress) {
                        uploadProgress.postValue(progress);
                    }
                });
            }

            @Override
            public void onProgress(int progress) {
                uploadProgress.postValue(progress);
            }
        });
    }

    private void createAlertWithImageUrl(String type, String severity, String location, String description, @Nullable String imageUrl, String userId) {
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
                saving.postValue(false);
                uploadProgress.postValue(100);
                createdId.postValue(alertId);
            }

            @Override
            public void onError(@NonNull Exception e) {
                saving.postValue(false);
                error.postValue(e.getMessage() != null ? e.getMessage() : "Failed to submit alert for review.");
            }
        });
    }

    public void clearResult() {
        createdId.postValue(null);
        error.postValue(null);
    }
}
