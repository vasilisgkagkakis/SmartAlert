package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.unipi.gkagkakis.smartalert.Utils.CoordinatesUtil;

/**
 * Use case for handling location operations
 * Encapsulates location logic following clean architecture
 */
public class LocationUseCase {
    private final FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onLocationReceived(String formattedLocation);
        void onLocationError(String error);
        void onPermissionRequired();
    }

    public LocationUseCase(Context context) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Requests current location and formats it for display
     */
    public void getCurrentLocation(Context context, LocationCallback callback) {
        if (!hasLocationPermissions(context)) {
            callback.onPermissionRequired();
            return;
        }

        boolean hasFineLocation = ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        int priority = hasFineLocation ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        CancellationTokenSource cts = new CancellationTokenSource();

        try {
            fusedLocationClient.getCurrentLocation(priority, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String formattedLocation = formatLocation(location);
                            callback.onLocationReceived(formattedLocation);
                        } else {
                            callback.onLocationError("Could not get location");
                        }
                    })
                    .addOnFailureListener(e -> callback.onLocationError("Location error: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Location permission denied");
        }
    }

    /**
     * Checks if location permissions are granted
     */
    public boolean hasLocationPermissions(Context context) {
        boolean fineGranted = ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return fineGranted || coarseGranted;
    }

    /**
     * Formats location to normalized coordinate string
     */
    private String formatLocation(Location location) {
        return CoordinatesUtil.formatLatLng(location.getLatitude(), location.getLongitude());
    }
}
