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
}
