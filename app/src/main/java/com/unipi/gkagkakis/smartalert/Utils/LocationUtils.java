package com.unipi.gkagkakis.smartalert.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public interface GeocodeCallback {
        void onSuccess(String address);
        void onError(String error);
    }

    /**
     * Parse location string and get human-readable address
     */
    public static void parseLocationAndGetAddress(@NonNull Context context, @Nullable String locationString, @NonNull GeocodeCallback callback) {
        if (locationString == null || locationString.trim().isEmpty()) {
            callback.onError("Location string is empty");
            return;
        }

        // Check if it's coordinates format (lat,lng)
        if (locationString.contains(",")) {
            String[] parts = locationString.split(",");
            if (parts.length == 2) {
                try {
                    double latitude = Double.parseDouble(parts[0].trim());
                    double longitude = Double.parseDouble(parts[1].trim());
                    getAddressFromCoordinates(context, latitude, longitude, callback);
                    return;
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid coordinate format: " + locationString);
                }
            }
        }

        // If not coordinates or parsing failed, return the original string
        callback.onSuccess(locationString);
    }

    public static void getAddressFromCoordinates(Context context, double latitude, double longitude, GeocodeCallback callback) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String formattedAddress = formatAddress(address);

                    // Run callback on UI thread
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(formattedAddress));
                    } else {
                        callback.onSuccess(formattedAddress);
                    }
                } else {
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> callback.onError("No address found"));
                    } else {
                        callback.onError("No address found");
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Geocoding failed: " + e.getMessage()));
                } else {
                    callback.onError("Geocoding failed: " + e.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during geocoding", e);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> callback.onError("Unexpected error: " + e.getMessage()));
                } else {
                    callback.onError("Unexpected error: " + e.getMessage());
                }
            }
        }).start();
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
