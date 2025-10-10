package com.unipi.gkagkakis.smartalert.service;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMNotificationSender {
    private static final String TAG = "FCMNotificationSender";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double NOTIFICATION_RADIUS_KM = 10.0;

    private final Context context;
    private final OkHttpClient client;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;
    private String projectId;

    public FCMNotificationSender(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
        this.firestore = FirebaseFirestore.getInstance();
        this.executor = Executors.newCachedThreadPool();

        // Set your Firebase project ID here
        this.projectId = "smartalert-ed251"; // Replace with your actual project ID if different
    }

    public void sendAlertNotificationToNearbyUsers(double alertLatitude, double alertLongitude,
                                                   String alertType, String alertDescription,
                                                   String locationName, String severity) {
        executor.execute(() -> {
            try {
                // Get access token
                String accessToken = getAccessToken();
                if (accessToken == null) {
                    Log.e(TAG, "Failed to get access token");
                    return;
                }

                // Send notification to nearby users
                findAndNotifyNearbyUsers(accessToken, alertLatitude, alertLongitude,
                    alertType, alertDescription, locationName, severity);

            } catch (Exception e) {
                Log.e(TAG, "Error sending notifications", e);
            }
        });
    }

    private void findAndNotifyNearbyUsers(String accessToken, double alertLat, double alertLng,
                                        String alertType, String alertDescription, String locationName, String severity) {
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = 0;
                    int nearbyUsers = 0;
                    int notifiedUsers = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        totalUsers++;
                        try {
                            String fcmToken = document.getString("fcmToken");
                            if (fcmToken == null || fcmToken.isEmpty()) {
                                Log.d(TAG, "Skipping user " + document.getId() + " - no FCM token");
                                continue; // Skip users without FCM tokens
                            }

                            // Get user's location from separate latitude/longitude fields
                            Double userLatitude = document.getDouble("latitude");
                            Double userLongitude = document.getDouble("longitude");

                            if (userLatitude == null || userLongitude == null) {
                                Log.d(TAG, "Skipping user " + document.getId() + " - no latitude/longitude data");
                                continue; // Skip users without location coordinates
                            }

                            // Calculate distance
                            double distance = calculateDistance(alertLat, alertLng, userLatitude, userLongitude);

                            // If user is within 10km, send notification
                            if (distance <= NOTIFICATION_RADIUS_KM) {
                                nearbyUsers++;
                                Log.d(TAG, "Sending notification to user " + document.getId() + " at distance: " + String.format("%.2f", distance) + "km");
                                sendNotificationToUser(accessToken, fcmToken, alertType,
                                    alertDescription, locationName, severity, distance);
                                notifiedUsers++;
                            } else {
                                Log.d(TAG, "User " + document.getId() + " is too far away: " + String.format("%.2f", distance) + "km");
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Error processing user document: " + document.getId(), e);
                        }
                    }

                    Log.i(TAG, "Alert processed: " + totalUsers + " total users, " + nearbyUsers + " within 10km, " + notifiedUsers + " notifications sent");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching users", e));
    }

    private void sendNotificationToUser(String accessToken, String fcmToken, String alertType,
                                      String alertDescription, String locationName, String severity, double distance) {
        try {
            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject fcmMessage = new JSONObject();

            // Build notification payload
            notification.put("title", "⚠️ Alert Nearby - Be Careful!");
            notification.put("body", String.format("A %s alert has been reported at %s (%.1fkm away). Severity: %s",
                alertType != null ? alertType : "safety",
                locationName != null ? locationName : "a nearby location",
                distance,
                severity != null ? severity : "unknown"));

            // Build data payload
            data.put("alert_type", alertType != null ? alertType : "safety");
            data.put("location", locationName != null ? locationName : "nearby");
            data.put("severity", severity != null ? severity : "unknown");
            data.put("distance", String.valueOf(distance));
            data.put("description", alertDescription != null ? alertDescription : "");
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

            // Build FCM message
            fcmMessage.put("token", fcmToken);
            fcmMessage.put("notification", notification);
            fcmMessage.put("data", data);

            // Add Android-specific configuration
            JSONObject android = new JSONObject();
            JSONObject androidNotification = new JSONObject();
            androidNotification.put("icon", "ic_notification");
            androidNotification.put("color", "#FF0000"); // Red color for alerts
            androidNotification.put("sound", "default");
            androidNotification.put("channel_id", "smart_alert_notifications");
            android.put("notification", androidNotification);
            android.put("priority", "high");
            fcmMessage.put("android", android);

            message.put("message", fcmMessage);

            // Send the notification
            String url = String.format(FCM_URL, projectId);
            RequestBody body = RequestBody.create(message.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send notification to token: " + fcmToken, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Notification sent successfully to: " + fcmToken);
                    } else {
                        Log.e(TAG, "Failed to send notification. Response: " + response.body().string());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating notification JSON", e);
        }
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private String getAccessToken() {
        try {
            InputStream serviceAccount = context.getAssets().open("service-account-key.json");
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(serviceAccount)
                    .createScoped(Arrays.asList(SCOPE));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            Log.e(TAG, "Error getting access token", e);
            return null;
        }
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
