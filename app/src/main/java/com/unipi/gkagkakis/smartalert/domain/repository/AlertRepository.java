package com.unipi.gkagkakis.smartalert.domain.repository;

import androidx.annotation.NonNull;

import com.unipi.gkagkakis.smartalert.model.Alert;

public interface AlertRepository {
    interface CreateAlertCallback {
        void onSuccess(@NonNull String alertId);
        void onError(@NonNull Exception e);
    }

    void createAlert(@NonNull Alert alert, @NonNull CreateAlertCallback callback);
}
