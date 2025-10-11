package com.unipi.gkagkakis.smartalert.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.presentation.UI.HomepageActivity;

import java.util.HashMap;
import java.util.Map;

public class LocationTrackingForegroundService extends Service {
    private static final String TAG = "LocationForegroundService";
    private static final String CHANNEL_ID = "location_tracking_channel";
    private static final int NOTIFICATION_ID = 2000;

    // Location update intervals - More aggressive for testing and real-time tracking
    private static final long UPDATE_INTERVAL = 7 * 1000; // 7 seconds (more frequent)
    private static final long FASTEST_INTERVAL = 5 * 1000; // 5 seconds
    private static final float MIN_DISTANCE_METERS = 100.0f; // 100 meters minimum movement

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private NotificationManager notificationManager;

    // Track last stored location to calculate distance
    private Location lastStoredLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        createLocationRequest();
        createLocationCallback();

        Log.d(TAG, "LocationTrackingForegroundService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationTrackingForegroundService started");

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());

        // Start location updates
        startLocationUpdates();

        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        Log.d(TAG, "LocationTrackingForegroundService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't provide binding
    }

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks location for smart alerts");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, HomepageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartAlert Location Tracking")
                .setContentText("Tracking location for nearby alerts")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL) // Match the update interval
                .build();

        Log.d(TAG, "Location request created with " + UPDATE_INTERVAL / 1000 + "s interval, " + MIN_DISTANCE_METERS + "m minimum distance");
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.d(TAG, "Location update received: " + location.getLatitude() + ", " + location.getLongitude() +
                            " (accuracy: " + location.getAccuracy() + "m, age: " + (System.currentTimeMillis() - location.getTime()) + "ms)");

                    // Check if we should update based on distance
                    if (shouldUpdateLocation(location)) {
                        updateLocationInFirestore(location.getLatitude(), location.getLongitude());
                        lastStoredLocation = location; // Update the last stored location
                        updateNotification();
                    } else {
                        Log.d(TAG, "Location update skipped - movement less than " + MIN_DISTANCE_METERS + "m");
                    }
                }
            }
        };
    }

    /**
     * Check if location should be updated based on minimum distance requirement
     */
    private boolean shouldUpdateLocation(Location newLocation) {
        // Always update if we don't have a previously stored location
        if (lastStoredLocation == null) {
            Log.d(TAG, "No previous location - storing initial location");
            return true;
        }

        // Calculate distance from last stored location
        float distance = lastStoredLocation.distanceTo(newLocation);
        Log.d(TAG, "Distance from last stored location: " + String.format("%.1f", distance) + "m (min required: " + MIN_DISTANCE_METERS + "m)");

        // Only update if moved more than minimum distance
        if (distance >= MIN_DISTANCE_METERS) {
            Log.d(TAG, "Distance threshold met - updating location");
            return true;
        }

        return false;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions not granted, cannot start location updates");
            stopSelf();
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Location updates started successfully - expecting updates every " + UPDATE_INTERVAL / 1000 + " seconds");

            // Schedule a check to ensure location updates are working
            new Handler(Looper.getMainLooper()).postDelayed(() -> Log.d(TAG, "Location update check - service should be providing regular updates now"), UPDATE_INTERVAL + 5000); // Check 5 seconds after first expected update

        } catch (Exception e) {
            Log.e(TAG, "Failed to request location updates", e);
            stopSelf();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates stopped");
        }
    }

    private void updateLocationInFirestore(double latitude, double longitude) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, cannot store location");
            return;
        }

        String userId = currentUser.getUid();
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("location", geoPoint);
        locationData.put("lastLocationUpdate", System.currentTimeMillis());
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("locationSource", "background_service");

        firestore.collection("users")
                .document(userId)
                .update(locationData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Background location stored successfully: " + latitude + ", " + longitude))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to store background location", e);
                    // If update fails, try to set the location (in case document doesn't exist)
                    firestore.collection("users")
                            .document(userId)
                            .set(locationData, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 ->
                                    Log.d(TAG, "Background location set successfully: " + latitude + ", " + longitude))
                            .addOnFailureListener(e2 ->
                                    Log.e(TAG, "Failed to set background location", e2));
                });
    }

    private void updateNotification() {
        String currentTime = java.text.DateFormat.getTimeInstance().format(new java.util.Date());

        Notification updatedNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartAlert Location Tracking")
                .setContentText("Last update: " + currentTime)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        notificationManager.notify(NOTIFICATION_ID, updatedNotification);
    }

    @SuppressLint("ObsoleteSdkInt")
    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, LocationTrackingForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
