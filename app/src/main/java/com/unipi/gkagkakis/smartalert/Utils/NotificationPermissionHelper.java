package com.unipi.gkagkakis.smartalert.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unipi.gkagkakis.smartalert.service.FCMTokenManager;

public class NotificationPermissionHelper {
    private static final String TAG = "NotificationPermission";
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    public static boolean hasNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // For older versions, notifications are granted by default
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, initialize FCM
                initializeFCM(activity);
            }
        } else {
            // For older versions, initialize FCM directly
            initializeFCM(activity);
        }
    }

    public static void handlePermissionResult(Activity activity, int requestCode,
                                            String[] permissions, int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                initializeFCM(activity);
            } else {
                Log.w(TAG, "Notification permission denied");
                // You might want to show a dialog explaining why the permission is needed
            }
        }
    }

    private static void initializeFCM(Activity activity) {
        FCMTokenManager.getInstance(activity).initializeToken();
        Log.d(TAG, "FCM initialized");
    }
}
