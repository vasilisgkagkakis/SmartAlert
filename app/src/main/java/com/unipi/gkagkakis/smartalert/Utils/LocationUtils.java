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
            }
        }).start();
    }

    /**
     * Format address object into a readable string
     * @param address Address object from geocoder
     * @return Formatted address string
     */
    private static String formatAddress(Address address) {
        StringBuilder formattedAddress = new StringBuilder();

        // Add street address if available
        if (address.getSubThoroughfare() != null) {
            formattedAddress.append(address.getSubThoroughfare()).append(" ");
        }
        if (address.getThoroughfare() != null) {
            formattedAddress.append(address.getThoroughfare()).append(", ");
        }

        // Add locality (city)
        if (address.getLocality() != null) {
            formattedAddress.append(address.getLocality()).append(", ");
        }

        // Add admin area (state/province)
        if (address.getAdminArea() != null) {
            formattedAddress.append(address.getAdminArea()).append(", ");
        }

        // Add country
        if (address.getCountryName() != null) {
            formattedAddress.append(address.getCountryName());
        }

        // Clean up trailing commas and spaces
        String result = formattedAddress.toString().trim();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        return result.isEmpty() ? "Unknown location" : result;
    }

    /**
     * Get a short address (city, state format)
     * @param context Application context
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback for result
     */
    public static void getShortAddressFromCoordinates(Context context, double latitude, double longitude, GeocodeCallback callback) {
        if (!Geocoder.isPresent()) {
            callback.onError("Geocoder not available");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, MAX_RESULTS);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String shortAddress = formatShortAddress(address);

                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> callback.onSuccess(shortAddress));
                    } else {
                        callback.onSuccess(shortAddress);
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
            }
        }).start();
    }

    /**
     * Format a short version of the address (locality, admin area)
     * @param address Address object from geocoder
     * @return Short formatted address
     */
    private static String formatShortAddress(Address address) {
        StringBuilder shortAddress = new StringBuilder();

        // Add locality (city) first
        if (address.getLocality() != null) {
            shortAddress.append(address.getLocality());
        }

        // Add admin area (state/province) if different from locality
        if (address.getAdminArea() != null && !address.getAdminArea().equals(address.getLocality())) {
            if (shortAddress.length() > 0) {
                shortAddress.append(", ");
            }
            shortAddress.append(address.getAdminArea());
        }

        // Fallback to sub-admin area if no locality
        if (shortAddress.length() == 0 && address.getSubAdminArea() != null) {
            shortAddress.append(address.getSubAdminArea());
        }

        // Fallback to country if nothing else
        if (shortAddress.length() == 0 && address.getCountryName() != null) {
            shortAddress.append(address.getCountryName());
        }

        return shortAddress.length() > 0 ? shortAddress.toString() : "Unknown location";
    }

    /**
     * Parse coordinates from a location string and get address
     * @param context Application context
     * @param locationString Location string that may contain coordinates
     * @param callback Callback for result
     */
    public static void parseLocationAndGetAddress(Context context, String locationString, GeocodeCallback callback) {
        if (locationString == null || locationString.trim().isEmpty()) {
            callback.onError("Empty location string");
            return;
        }

        // Try to parse coordinates from the location string
        String coordinates = CoordinatesUtil.tryParseCoordinates(locationString);

        if (coordinates != null) {
            try {
                String[] parts = coordinates.split(",");
                if (parts.length == 2) {
                    double latitude = Double.parseDouble(parts[0].trim());
                    double longitude = Double.parseDouble(parts[1].trim());

                    // Get short address for group display
                    getShortAddressFromCoordinates(context, latitude, longitude, callback);
                } else {
                    callback.onError("Invalid coordinate format");
                }
            } catch (NumberFormatException e) {
                callback.onError("Failed to parse coordinates");
            }
        } else {
            // If no coordinates found, assume it's already a readable location
            callback.onSuccess(locationString);
        }
    }
}
