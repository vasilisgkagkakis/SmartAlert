package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.content.Context;
import android.util.Log;

import com.unipi.gkagkakis.smartalert.service.LocationTrackingService;

public class InitializeLocationUseCase {
    private static final String TAG = "InitializeLocationUseCase";
    private final LocationTrackingService locationTrackingService;

    public InitializeLocationUseCase(Context context) {
        this.locationTrackingService = LocationTrackingService.getInstance(context);
    }

    /**
     * Initialize location tracking for a user after login
     * This will attempt to get and store the user's current location if permissions are available
     */
    public void initializeUserLocation() {
        Log.d(TAG, "Initializing location for logged-in user");

        if (locationTrackingService.hasLocationPermissions()) {
            // User already has location permissions, update location immediately
            locationTrackingService.updateLocationNow();

            // Start continuous tracking if background permission is available
            if (locationTrackingService.hasBackgroundLocationPermission()) {
                locationTrackingService.startContinuousLocationTracking();
            }
            Log.d(TAG, "Location initialization completed");
        } else {
            Log.d(TAG, "Location permissions not available, will be requested in homepage");
        }
    }

    /**
     * Check if location permissions are available
     */
    public boolean hasLocationPermissions() {
        return locationTrackingService.hasLocationPermissions();
    }

    /**
     * Check if background location permission is available
     */
    public boolean hasBackgroundLocationPermission() {
        return locationTrackingService.hasBackgroundLocationPermission();
    }
}
