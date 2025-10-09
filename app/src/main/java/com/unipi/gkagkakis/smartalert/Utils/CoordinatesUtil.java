package com.unipi.gkagkakis.smartalert.Utils;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CoordinatesUtil {
    private CoordinatesUtil() {}

    private static final Pattern DECIMAL_DEGREES = Pattern.compile(
            "([-+]?\\d{1,2}\\.\\d+)\\s*,\\s*([-+]?\\d{1,3}\\.\\d+)"
    );
    private static final Pattern ANY_DOUBLE = Pattern.compile("([-+]?\\d{1,3}(?:\\.\\d+)?)");

    @Nullable
    public static String tryParseCoordinates(@NonNull String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;

        Matcher m = DECIMAL_DEGREES.matcher(trimmed);
        if (m.find()) {
            double lat = parseDoubleSafe(m.group(1));
            double lon = parseDoubleSafe(m.group(2));
            if (isValidLatLon(lat, lon)) return formatLatLng(lat, lon);
        }

        try {
            Uri uri = Uri.parse(trimmed);
            String q = uri.getQueryParameter("q");
            if (q == null) q = uri.getQueryParameter("query");
            if (q != null) {
                Matcher mq = DECIMAL_DEGREES.matcher(q);
                if (mq.find()) {
                    double lat = parseDoubleSafe(mq.group(1));
                    double lon = parseDoubleSafe(mq.group(2));
                    if (isValidLatLon(lat, lon)) return formatLatLng(lat, lon);
                }
            }
        } catch (Exception ignored) { }

        Matcher any = ANY_DOUBLE.matcher(trimmed);
        Double first = null, second = null;
        while (any.find()) {
            double val = parseDoubleSafe(any.group(1));
            if (first == null) first = val;
            else { second = val; break; }
        }
        if (first != null && second != null) {
            double lat = first, lon = second;
            if (isValidLatLon(lat, lon)) return formatLatLng(lat, lon);
        }
        return null;
    }

    public static boolean isValidLatLon(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    public static String formatLatLng(double lat, double lon) {
        return String.format(Locale.US, "%.6f,%.6f", lat, lon);
    }

    private static double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return Double.NaN; }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    /**
     * Parse coordinates from a coordinate string and calculate distance to another coordinate
     * @param coords1 First coordinate string (e.g., "37.7749,-122.4194")
     * @param coords2 Second coordinate string (e.g., "37.7849,-122.4094")
     * @return Distance in kilometers, or -1 if parsing fails
     */
    public static double calculateDistanceFromStrings(String coords1, String coords2) {
        try {
            String[] parts1 = coords1.split(",");
            String[] parts2 = coords2.split(",");

            if (parts1.length != 2 || parts2.length != 2) return -1;

            double lat1 = Double.parseDouble(parts1[0].trim());
            double lon1 = Double.parseDouble(parts1[1].trim());
            double lat2 = Double.parseDouble(parts2[0].trim());
            double lon2 = Double.parseDouble(parts2[1].trim());

            return calculateDistance(lat1, lon1, lat2, lon2);
        } catch (Exception e) {
            return -1;
        }
    }
}
