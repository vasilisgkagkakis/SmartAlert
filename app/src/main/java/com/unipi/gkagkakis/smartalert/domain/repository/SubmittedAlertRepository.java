package com.unipi.gkagkakis.smartalert.domain.repository;

import androidx.annotation.NonNull;

import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;

import java.util.List;

public interface SubmittedAlertRepository {
    interface CreateSubmittedAlertCallback {
        void onSuccess(@NonNull String alertId);
        void onError(@NonNull Exception e);
    }

    interface GetAllSubmittedAlertsCallback {
        void onSuccess(@NonNull List<SubmittedAlert> submittedAlerts);
        void onError(@NonNull Exception e);
    }

    interface DeleteSubmittedAlertCallback {
        void onSuccess();
        void onError(@NonNull Exception e);
    }

    void createSubmittedAlert(@NonNull SubmittedAlert submittedAlert, @NonNull CreateSubmittedAlertCallback callback);
    void getAllSubmittedAlerts(@NonNull GetAllSubmittedAlertsCallback callback);
    void deleteSubmittedAlert(@NonNull String alertId, @NonNull DeleteSubmittedAlertCallback callback);
}
