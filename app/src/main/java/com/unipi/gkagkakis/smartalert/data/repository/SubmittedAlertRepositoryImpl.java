package com.unipi.gkagkakis.smartalert.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;

import java.util.ArrayList;
import java.util.List;

public class SubmittedAlertRepositoryImpl implements SubmittedAlertRepository {

    private static final String COLLECTION_SUBMITTED_ALERTS = "submitted_alerts";
    private final CollectionReference submittedAlertsRef;

    private static volatile SubmittedAlertRepository INSTANCE;

    private SubmittedAlertRepositoryImpl() {
        this.submittedAlertsRef = FirebaseFirestore.getInstance().collection(COLLECTION_SUBMITTED_ALERTS);
    }

    public static SubmittedAlertRepositoryImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (SubmittedAlertRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SubmittedAlertRepositoryImpl();
                }
            }
        }
        return (SubmittedAlertRepositoryImpl) INSTANCE;
    }

    @Override
    public void createSubmittedAlert(@NonNull SubmittedAlert submittedAlert, @NonNull CreateSubmittedAlertCallback callback) {
        submittedAlertsRef.add(submittedAlert)
                .addOnSuccessListener((DocumentReference doc) -> callback.onSuccess(doc.getId()))
                .addOnFailureListener(e -> callback.onError(e != null ? e : new RuntimeException("Unknown error")));
    }

    @Override
    public void getAllSubmittedAlerts(@NonNull GetAllSubmittedAlertsCallback callback) {
        submittedAlertsRef.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SubmittedAlert> submittedAlerts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SubmittedAlert submittedAlert = document.toObject(SubmittedAlert.class);
                        submittedAlert.setId(document.getId());
                        submittedAlerts.add(submittedAlert);
                    }
                    callback.onSuccess(submittedAlerts);
                })
                .addOnFailureListener(e -> callback.onError(e != null ? e : new RuntimeException("Failed to fetch submitted alerts")));
    }

    @Override
    public void deleteSubmittedAlert(@NonNull String alertId, @NonNull DeleteSubmittedAlertCallback callback) {
        submittedAlertsRef.document(alertId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e != null ? e : new RuntimeException("Failed to delete submitted alert")));
    }
}
