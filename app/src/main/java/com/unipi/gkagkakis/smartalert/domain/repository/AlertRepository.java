package com.unipi.gkagkakis.smartalert.domain.repository;

import androidx.annotation.NonNull;

import com.unipi.gkagkakis.smartalert.model.Alert;

import java.util.List;

public interface AlertRepository {
    interface CreateAlertCallback {
        void onSuccess(@NonNull String alertId);
        void onError(@NonNull Exception e);
    }

    interface GetAllAlertsCallback {
        void onSuccess(@NonNull List<Alert> alerts);
        void onError(@NonNull Exception e);
    }

    void createAlert(@NonNull Alert alert, @NonNull CreateAlertCallback callback);
    void getAllAlerts(@NonNull GetAllAlertsCallback callback);
}
