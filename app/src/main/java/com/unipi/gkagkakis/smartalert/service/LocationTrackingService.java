package com.unipi.gkagkakis.smartalert.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class LocationTrackingService {
    private static final String TAG = "LocationTrackingService";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    public static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1003;

    private static LocationTrackingService instance;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private LocationTrackingService(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public static synchronized LocationTrackingService getInstance(Context context) {
        if (instance == null) {
            instance = new LocationTrackingService(context);
        }
        return instance;
    }

    /**
     * Check if basic location permissions are granted
     */
    public boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if background location permission is granted (Android 10+)
     */
    public boolean hasBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Not needed for older versions
    }

    /**
     * Request basic location permissions
     */
    public void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Request background location permission (must be called after basic permissions are granted)
     */
    public void requestBackgroundLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBackgroundLocationPermission() && hasLocationPermissions()) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Start continuous location tracking with background service
     */
    public void startContinuousLocationTracking() {
        if (!hasLocationPermissions()) {
            Log.w(TAG, "Basic location permissions not granted, cannot start continuous tracking");
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, cannot start continuous tracking");
            return;
        }

        if (hasBackgroundLocationPermission()) {
            // Start the foreground service for continuous tracking
            LocationTrackingForegroundService.startService(context);
            Log.d(TAG, "Continuous location tracking started with background permission");
        } else {
            Log.w(TAG, "Background location permission not granted, starting basic tracking only");
            // Fall back to basic location tracking
            getCurrentLocationAndStore();
        }
    }

    /**
     * Stop continuous location tracking
     */
    public void stopContinuousLocationTracking() {
        LocationTrackingForegroundService.stopService(context);
        Log.d(TAG, "Continuous location tracking stopped");
    }

    /**
     * Get current location and store in Firestore (one-time update)
     */
    public void updateLocationNow() {
        if (hasLocationPermissions()) {
            getCurrentLocationAndStore();
        } else {
            Log.w(TAG, "Cannot update location - permissions not granted");
        }
    }

    /**
     * Get current location and store in Firestore
     */
    private void getCurrentLocationAndStore() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            storeLocationInFirestore(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.w(TAG, "Unable to get current location - location is null");
                            // Try to request a fresh location
                            requestFreshLocation();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    requestFreshLocation();
                });
    }

    /**
     * Request a fresh location update
     */
    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // For simplicity, we'll just try getLastLocation again after a delay
        // In a production app, you might want to use requestLocationUpdates for fresh location
        new android.os.Handler().postDelayed(() -> {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            storeLocationInFirestore(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.w(TAG, "Still unable to get location after retry");
                        }
                    });
        }, 2000); // Wait 2 seconds and try again
    }

    /**
     * Store user location in Firestore
     */
    private void storeLocationInFirestore(double latitude, double longitude) {
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
        locationData.put("locationSource", "manual_update");

        firestore.collection("users")
                .document(userId)
                .update(locationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Location stored successfully: " + latitude + ", " + longitude);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to store location in Firestore", e);
                    // If update fails, try to set the location (in case document doesn't exist)
                    firestore.collection("users")
                            .document(userId)
                            .set(locationData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 ->
                                Log.d(TAG, "Location set successfully: " + latitude + ", " + longitude))
                            .addOnFailureListener(e2 ->
                                Log.e(TAG, "Failed to set location in Firestore", e2));
                });
    }

    /**
     * Handle permission result
     */
    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                 (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                Log.d(TAG, "Basic location permission granted");
                // Start immediate location update
                getCurrentLocationAndStore();
                // Don't start continuous tracking yet - will be started separately
            } else {
                Log.w(TAG, "Basic location permission denied");
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Background location permission granted");
                startContinuousLocationTracking();
            } else {
                Log.w(TAG, "Background location permission denied - will use foreground-only tracking");
            }
        }
    }
}
