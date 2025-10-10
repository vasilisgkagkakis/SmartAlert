package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.app.Activity;
import android.content.Context;

import com.unipi.gkagkakis.smartalert.Utils.NotificationPermissionHelper;
import com.unipi.gkagkakis.smartalert.service.LocationTrackingService;

/**
 * Use case to handle permission requests and results
 * Encapsulates permission logic following clean architecture
 * Implements sequential permission requests for better UX
 */
public class PermissionUseCase {
    private final LocationTrackingService locationTrackingService;
    private boolean notificationPermissionRequested = false;

    public PermissionUseCase(Context context) {
        this.locationTrackingService = LocationTrackingService.getInstance(context);
    }

    /**
     * Starts the sequential permission request flow
     * First requests notification permission, then location permissions
     */
    public void requestInitialPermissions(Activity activity) {
        // Start with notification permission first
        requestNotificationPermission(activity);
    }

    /**
     * Requests notification permission first in the sequence
     */
    private void requestNotificationPermission(Activity activity) {
        if (!notificationPermissionRequested) {
            notificationPermissionRequested = true;
            NotificationPermissionHelper.requestNotificationPermission(activity);
        } else {
            // Notification permission already handled, proceed to location
            requestLocationPermissionsAndStartTracking(activity);
        }
    }

    /**
     * Requests location permissions after notification permission is handled
     */
    public void requestLocationPermissionsAndStartTracking(Activity activity) {
        if (locationTrackingService.hasLocationPermissions()) {
            // Basic permissions already granted, check for background permission
            if (locationTrackingService.hasBackgroundLocationPermission()) {
                // All permissions granted, start continuous tracking
                locationTrackingService.startContinuousLocationTracking();
            } else {
                // Request background location permission
                locationTrackingService.requestBackgroundLocationPermission(activity);
            }
        } else {
            // Request basic location permissions first
            locationTrackingService.requestLocationPermissions(activity);
        }
    }

    public void handlePermissionResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        // Handle notification permissions first
        NotificationPermissionHelper.handlePermissionResult(activity, requestCode, permissions, grantResults);

        // After notification permission is handled, proceed to location permissions
        if (requestCode == NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Notification permission result received, now request location permissions
            requestLocationPermissionsAndStartTracking(activity);
            return;
        }

        // Handle location permissions
        locationTrackingService.handlePermissionResult(requestCode, permissions, grantResults);

        // After handling basic location permissions, request background permission
        if (requestCode == LocationTrackingService.LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationTrackingService.hasLocationPermissions()) {
                // Basic permissions granted, now request background permission
                locationTrackingService.requestBackgroundLocationPermission(activity);
            }
        }
    }
}
