package com.unipi.gkagkakis.smartalert.data.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64ImageService {

    private static final String TAG = "Base64ImageService";
    private static final int IMAGE_QUALITY = 60; // Lower quality for Base64 storage
    private static final int MAX_IMAGE_WIDTH = 512; // Smaller size for Base64
    private static final int MAX_IMAGE_HEIGHT = 512;

    private static volatile Base64ImageService INSTANCE;

    private Base64ImageService() {}

    public static Base64ImageService getInstance() {
        if (INSTANCE == null) {
            synchronized (Base64ImageService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Base64ImageService();
                }
            }
        }
        return INSTANCE;
    }

    public interface ImageConversionCallback {
        void onSuccess(@NonNull String base64Image);
        void onError(@NonNull Exception e);
        void onProgress(int progress);
    }

    public void convertImageToBase64(@NonNull Context context, @NonNull Uri imageUri, @NonNull ImageConversionCallback callback) {
        Log.d(TAG, "Starting Base64 conversion for URI: " + imageUri.toString());

        new Thread(() -> {
            try {
                callback.onProgress(10);

                // Convert URI to byte array
                byte[] imageData = uriToByteArray(context, imageUri);
                if (imageData == null) {
                    callback.onError(new Exception("Failed to process image - could not read image data"));
                    return;
                }

                callback.onProgress(50);
                Log.d(TAG, "Image processed, size: " + imageData.length + " bytes");

                // Convert to Base64
                String base64String = Base64.encodeToString(imageData, Base64.DEFAULT);
                String base64Image = "data:image/jpeg;base64," + base64String;

                callback.onProgress(100);
                Log.d(TAG, "Base64 conversion completed, length: " + base64Image.length());

                // Return on main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(base64Image));

            } catch (Exception e) {
                Log.e(TAG, "Exception during Base64 conversion", e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(new Exception("Failed to convert image to Base64: " + e.getMessage())));
            }
        }).start();
    }

    @Nullable
    private byte[] uriToByteArray(@NonNull Context context, @NonNull Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: " + uri);
                return null;
            }

            // Decode the image
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: " + uri);
                return null;
            }

            Log.d(TAG, "Original bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Resize to smaller size for Base64 storage
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

            Log.d(TAG, "Resized bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);

            if (!compressed) {
                Log.e(TAG, "Failed to compress bitmap");
                bitmap.recycle();
                return null;
            }

            bitmap.recycle();
            byte[] result = baos.toByteArray();
            baos.close();

            Log.d(TAG, "Final compressed image size: " + result.length + " bytes");
            return result;

        } catch (IOException e) {
            Log.e(TAG, "IO Exception while processing image", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception while processing image", e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close input stream", e);
                }
            }
        }
    }

    private Bitmap resizeBitmap(@NonNull Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (resized != bitmap) {
            bitmap.recycle(); // Free original bitmap memory
        }
        return resized;
    }

    public static Bitmap decodeBase64ToBitmap(@NonNull String base64Image) {
        try {
            // Remove data:image/jpeg;base64, prefix if present
            String base64Data = base64Image;
            if (base64Data.startsWith("data:image")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode Base64 image", e);
            return null;
        }
    }
}
