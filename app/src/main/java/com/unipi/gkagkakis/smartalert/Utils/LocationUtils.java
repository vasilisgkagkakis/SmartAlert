package com.unipi.gkagkakis.smartalert.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {
    private static final String TAG = "LocationUtils";
    private static final int MAX_RESULTS = 1;

    /**
     * Interface for geocoding callbacks
     */
    public interface GeocodeCallback {
        void onSuccess(String address);
        void onError(String error);
    }

    /**
     * Convert coordinates to human-readable address
     * @param context Application context
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback for result
     */
    public static void getAddressFromCoordinates(Context context, double latitude, double longitude, GeocodeCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onError("Geocoder not available");
            return;
        }

        // Perform geocoding in background thread
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, MAX_RESULTS);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String formattedAddress = formatAddress(address);

                    // Post result back to main thread
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> callback.onSuccess(formattedAddress));
                    } else {
                        callback.onSuccess(formattedAddress);
                    }
                } else {
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> callback.onError("No address found"));
                    } else {
                        callback.onError("No address found");
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onError("Geocoding failed: " + e.getMessage()));
                } else {
                    callback.onError("Geocoding failed: " + e.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during geocoding", e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onError("Unexpected error: " + e.getMessage()));
                } else {
                    callback.onError("Unexpected error: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Parse location string and get human-readable address
     * @param context Application context
     * @param locationString Location string (may contain coordinates)
     * @param callback Callback for result
     */
    public static void parseLocationAndGetAddress(Context context, String locationString, GeocodeCallback callback) {
        if (locationString == null || locationString.isEmpty()) {
            callback.onError("Location string is empty");
            return;
        }

        // Try to parse coordinates from the location string
        try {
            String[] parts = locationString.split(",");
            if (parts.length >= 2) {
                double latitude = Double.parseDouble(parts[0].trim());
                double longitude = Double.parseDouble(parts[1].trim());
                getAddressFromCoordinates(context, latitude, longitude, callback);
            } else {
                // If not coordinates, return the original string
                callback.onSuccess(locationString);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return the original string
            callback.onSuccess(locationString);
        }
    }

    /**
     * Format address from Address object
     * @param address Address object from geocoder
     * @return Formatted address string
     */
    private static String formatAddress(Address address) {
        StringBuilder formattedAddress = new StringBuilder();

        // Add address line if available
        if (address.getMaxAddressLineIndex() >= 0) {
            formattedAddress.append(address.getAddressLine(0));
        } else {
            // Build address from components
            if (address.getSubThoroughfare() != null) {
                formattedAddress.append(address.getSubThoroughfare()).append(" ");
            }
            if (address.getThoroughfare() != null) {
                formattedAddress.append(address.getThoroughfare()).append(", ");
            }
            if (address.getLocality() != null) {
                formattedAddress.append(address.getLocality()).append(", ");
            }
            if (address.getAdminArea() != null) {
                formattedAddress.append(address.getAdminArea());
            }
        }

        return formattedAddress.toString().trim();
    }
}
