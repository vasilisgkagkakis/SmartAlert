package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.data.repository.AlertRepositoryImpl;

public class AlertViewModel extends ViewModel {

    private final AlertRepositoryImpl repository;

    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> createdId = new MutableLiveData<>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public AlertViewModel() {
        this.repository = AlertRepositoryImpl.getInstance();
    }

    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<String> getCreatedId() { return createdId; }
    public LiveData<String> getError() { return error; }

    public void createAlert(String type, String severity, String location, String description, @Nullable Uri imageUri) {
        error.postValue(null);

        if (type == null || type.trim().isEmpty()) { error.postValue("Type is required."); return; }
        if (severity == null || severity.trim().isEmpty()) { error.postValue("Severity is required."); return; }
        if (location == null || location.trim().isEmpty()) { error.postValue("Location is required."); return; }
        if (description == null || description.trim().isEmpty()) { error.postValue("Description is required."); return; }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { error.postValue("User not authenticated."); return; }

        saving.postValue(true);

        Alert alert = new Alert(
                null,
                type.trim(),
                severity.trim(),
                location.trim(),
                description.trim(),
                imageUri != null ? imageUri.toString() : null,
                user.getUid(),
                null
        );

        repository.createAlert(alert, new AlertRepositoryImpl.CreateAlertCallback() {
            @Override
            public void onSuccess(@NonNull String alertId) {
                saving.postValue(false);
                createdId.postValue(alertId);
            }

            @Override
            public void onError(@NonNull Exception e) {
                saving.postValue(false);
                error.postValue(e.getMessage() != null ? e.getMessage() : "Failed to create alert.");
            }
        });
    }

    public void clearResult() {
        createdId.postValue(null);
        error.postValue(null);
    }
}
