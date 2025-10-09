package com.unipi.gkagkakis.smartalert.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.gkagkakis.smartalert.model.Alert;
import com.unipi.gkagkakis.smartalert.domain.repository.AlertRepository;

public class AlertRepositoryImpl implements AlertRepository {

    private static final String COLLECTION_ALERTS = "alerts";
    private final CollectionReference alertsRef;

    private static volatile AlertRepository INSTANCE;

    private AlertRepositoryImpl() {
        this.alertsRef = FirebaseFirestore.getInstance().collection(COLLECTION_ALERTS);
    }

    public static AlertRepositoryImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (AlertRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlertRepositoryImpl();
                }
            }
        }
        return (AlertRepositoryImpl) INSTANCE;
    }

    @Override
    public void createAlert(@NonNull Alert alert, @NonNull CreateAlertCallback callback) {
        alertsRef.add(alert)
                .addOnSuccessListener((DocumentReference doc) -> callback.onSuccess(doc.getId()))
                .addOnFailureListener(e -> callback.onError(e != null ? e : new RuntimeException("Unknown error")));
    }
}
