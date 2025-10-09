package com.unipi.gkagkakis.smartalert.data.service;

import android.util.Log;
import androidx.annotation.NonNull;

import com.unipi.gkagkakis.smartalert.data.repository.SubmittedAlertRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.repository.SubmittedAlertRepository;
import com.unipi.gkagkakis.smartalert.model.SubmittedAlert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AlertSeedService {

    private static final String TAG = "AlertSeedService";
    private final SubmittedAlertRepository repository;

    public AlertSeedService() {
        this.repository = SubmittedAlertRepositoryImpl.getInstance();
    }

    public interface SeedCallback {
        void onSeedComplete(int successCount, int totalCount);
        void onSeedError(@NonNull Exception e);
    }

    public void seedTestAlerts(@NonNull String userId, @NonNull SeedCallback callback) {
        List<SubmittedAlert> testAlerts = createTestAlerts(userId);

        Log.d(TAG, "Starting to seed " + testAlerts.size() + " test alerts");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger completedCount = new AtomicInteger(0);
        int totalCount = testAlerts.size();

        for (SubmittedAlert alert : testAlerts) {
            repository.createSubmittedAlert(alert, new SubmittedAlertRepository.CreateSubmittedAlertCallback() {
                @Override
                public void onSuccess(@NonNull String alertId) {
                    successCount.incrementAndGet();
                    int completed = completedCount.incrementAndGet();
                    Log.d(TAG, "Seeded alert: " + alert.getType() + " (" + completed + "/" + totalCount + ")");

                    if (completed == totalCount) {
                        callback.onSeedComplete(successCount.get(), totalCount);
                    }
                }

                @Override
                public void onError(@NonNull Exception e) {
                    int completed = completedCount.incrementAndGet();
                    Log.e(TAG, "Failed to seed alert: " + alert.getType(), e);

                    if (completed == totalCount) {
                        callback.onSeedComplete(successCount.get(), totalCount);
                    }
                }
            });
        }

        if (testAlerts.isEmpty()) {
            callback.onSeedComplete(0, 0);
        }
    }

    private List<SubmittedAlert> createTestAlerts(@NonNull String userId) {
        List<SubmittedAlert> alerts = new ArrayList<>();

        // Fire alerts - Mix of close and far locations
        alerts.add(new SubmittedAlert(
                null,
                "Fire",
                "High",
                "37.7749, -122.4194", // San Francisco, CA
                "Large fire spotted in downtown San Francisco. Smoke visible from several blocks away.",
                createSampleBase64Image("fire"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Fire",
                "Critical",
                "40.7128, -74.0060", // New York City, NY (2900+ km away)
                "Building on fire in Manhattan, people evacuating. Fire department needed urgently.",
                null,
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Fire",
                "Medium",
                "37.7849, -122.4094", // Close to SF (within 5km)
                "Small grass fire near Golden Gate Park. Contained but needs attention.",
                createSampleBase64Image("fire"),
                userId,
                null
        ));

        // Add a fire in London (very far - 8600+ km)
        alerts.add(new SubmittedAlert(
                null,
                "Fire",
                "High",
                "51.5074, -0.1278", // London, UK
                "Fire reported in central London building. Emergency services responding.",
                null,
                userId,
                null
        ));

        // Flood alerts - Different continents
        alerts.add(new SubmittedAlert(
                null,
                "Flood",
                "High",
                "37.7949, -122.4394", // Close to SF (within 5km)
                "Street flooding after heavy rain in San Francisco. Cars stuck, road impassable.",
                createSampleBase64Image("flood"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Flood",
                "Critical",
                "-33.8688, 151.2093", // Sydney, Australia (12000+ km away)
                "Severe flooding in Sydney CBD. Multiple streets underwater.",
                null,
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Flood",
                "Medium",
                "35.6762, 139.6503", // Tokyo, Japan (8300+ km away)
                "Flash flooding in Tokyo after typhoon. Subway stations affected.",
                createSampleBase64Image("flood"),
                userId,
                null
        ));

        // Earthquake alerts - Different countries
        alerts.add(new SubmittedAlert(
                null,
                "Earthquake",
                "Critical",
                "37.8049, -122.4194", // Close to SF (within 5km)
                "Strong earthquake felt in San Francisco. Building damage reported, people injured.",
                createSampleBase64Image("earthquake"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Earthquake",
                "High",
                "35.6895, 139.6917", // Tokyo, Japan (8300+ km away)
                "Major earthquake in Tokyo. Buildings swaying, metro stopped.",
                null,
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Earthquake",
                "Medium",
                "41.9028, 12.4964", // Rome, Italy (10400+ km away)
                "Moderate earthquake in Rome. Historical buildings checked for damage.",
                createSampleBase64Image("earthquake"),
                userId,
                null
        ));

        // Medical Emergency alerts - Different US states
        alerts.add(new SubmittedAlert(
                null,
                "Medical Emergency",
                "Critical",
                "37.7749, -122.3994", // Close to SF (within 5km)
                "Person collapsed on Market Street SF. CPR in progress. Ambulance needed immediately.",
                null,
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Medical Emergency",
                "High",
                "25.7617, -80.1918", // Miami, Florida (4100+ km away)
                "Car accident with injuries on Miami Beach. Multiple people need medical attention.",
                createSampleBase64Image("medical"),
                userId,
                null
        ));

        // Crime alerts - Different countries
        alerts.add(new SubmittedAlert(
                null,
                "Crime",
                "High",
                "37.7649, -122.3894", // Close to SF (within 5km)
                "Armed robbery in progress at store in San Francisco. Suspects still on scene.",
                null,
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Crime",
                "Medium",
                "48.8566, 2.3522", // Paris, France (9100+ km away)
                "Suspicious activity reported near Eiffel Tower. Multiple people acting strangely.",
                createSampleBase64Image("crime"),
                userId,
                null
        ));

        // Traffic Accident alerts - Different continents
        alerts.add(new SubmittedAlert(
                null,
                "Traffic Accident",
                "Medium",
                "37.7949, -122.3794", // Close to SF (within 5km)
                "Multi-car accident on Bay Bridge. Traffic backed up for miles.",
                createSampleBase64Image("traffic"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Traffic Accident",
                "Low",
                "52.5200, 13.4050", // Berlin, Germany (9200+ km away)
                "Fender bender at Brandenburg Gate intersection. Minor damage, no injuries.",
                null,
                userId,
                null
        ));

        // Storm alerts - Different hemispheres
        alerts.add(new SubmittedAlert(
                null,
                "Storm",
                "High",
                "37.8149, -122.4094", // Close to SF (within 5km)
                "Severe thunderstorm hitting San Francisco Bay Area. High winds, power lines damaged.",
                createSampleBase64Image("storm"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Storm",
                "Critical",
                "-26.2041, 28.0473", // Johannesburg, South Africa (17000+ km away)
                "Severe hailstorm in Johannesburg. Cars damaged, windows broken.",
                null,
                userId,
                null
        ));

        // Add some more distant alerts for testing
        alerts.add(new SubmittedAlert(
                null,
                "Medical Emergency",
                "Medium",
                "55.7558, 37.6173", // Moscow, Russia (10400+ km away)
                "Medical emergency near Red Square. Patient experiencing chest pains.",
                createSampleBase64Image("medical"),
                userId,
                null
        ));

        alerts.add(new SubmittedAlert(
                null,
                "Crime",
                "High",
                "-34.6037, -58.3816", // Buenos Aires, Argentina (10400+ km away)
                "Bank robbery reported in Buenos Aires city center. Police responding.",
                null,
                userId,
                null
        ));

        return alerts;
    }

    private String createSampleBase64Image(String type) {
        // Create a simple colored rectangle as a placeholder image
        // This generates a small Base64 image for testing purposes

        // Different colors for different alert types
        String colorHex;
        switch (type) {
            case "fire":
                colorHex = "FF4444"; // Red
                break;
            case "flood":
                colorHex = "4444FF"; // Blue
                break;
            case "earthquake":
                colorHex = "8B4513"; // Brown
                break;
            case "medical":
                colorHex = "FF69B4"; // Pink
                break;
            case "crime":
                colorHex = "800080"; // Purple
                break;
            case "traffic":
                colorHex = "FFA500"; // Orange
                break;
            case "storm":
                colorHex = "808080"; // Gray
                break;
            default:
                colorHex = "000000"; // Black
                break;
        }

        // Simple 1x1 pixel image in Base64 format (placeholder)
        // In a real implementation, you might want to use actual sample images
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
    }

    public void clearAllSubmittedAlerts(@NonNull SeedCallback callback) {
        // This method would require getting all alerts first and then deleting them
        // For now, we'll just log that this functionality could be added
        Log.d(TAG, "Clear all submitted alerts functionality would be implemented here");
        callback.onSeedComplete(0, 0);
    }
}
