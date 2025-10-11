package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.unipi.gkagkakis.smartalert.domain.usecase.CreateAlertUseCase;

/**
 * ViewModel for Alert creation following MVVM pattern
 * Handles alert creation logic through use cases
 */
public class AlertViewModel extends ViewModel {

    // Private MutableLiveData for internal state management
    private final MutableLiveData<Boolean> _saving = new MutableLiveData<>(false);
    private final MutableLiveData<String> _createdId = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<Integer> _uploadProgress = new MutableLiveData<>(0);

    // Public read-only LiveData for UI observation
    public final LiveData<Boolean> saving = _saving;
    public final LiveData<String> createdId = _createdId;
    public final LiveData<String> error = _error;
    public final LiveData<Integer> uploadProgress = _uploadProgress;

    private final CreateAlertUseCase createAlertUseCase;

    public AlertViewModel() {
        this.createAlertUseCase = new CreateAlertUseCase();
    }

    // Getter methods for backward compatibility
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<String> getCreatedId() { return createdId; }
    public LiveData<String> getError() { return error; }
    public LiveData<Integer> getUploadProgress() { return uploadProgress; }

    public void createAlert(Context context, String type, String severity, String location, String description, @Nullable Uri imageUri) {
        _error.setValue(null);
        _saving.setValue(true);
        _uploadProgress.setValue(0);

        // Delegate to use case following clean architecture
        createAlertUseCase.createAlert(
            context,
            type,
            severity,
            location,
            description,
            imageUri,
            new CreateAlertUseCase.CreateAlertCallback() {
                @Override
                public void onSuccess(@NonNull String alertId) {
                    _saving.setValue(false);
                    _uploadProgress.setValue(100);
                    _createdId.setValue(alertId);
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    _saving.setValue(false);
                    _error.setValue(errorMessage);
                }

                @Override
                public void onProgress(int progress) {
                    _uploadProgress.setValue(progress);
                }
            }
        );
    }

    public void clearResult() {
        _createdId.setValue(null);
        _error.setValue(null);
    }
}
