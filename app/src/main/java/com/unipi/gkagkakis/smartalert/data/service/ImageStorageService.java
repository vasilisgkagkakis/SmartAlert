package com.unipi.gkagkakis.smartalert.data.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageStorageService {

    private static final String TAG = "ImageStorageService";
    private static final String ALERT_IMAGES_PATH = "alert_images/";
    private static final int IMAGE_QUALITY = 80;
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;

    private final FirebaseStorage storage;
    private final StorageReference storageRef;

    private static volatile ImageStorageService INSTANCE;

    private ImageStorageService() {
        try {
            // Use default Firebase Storage instance first
            this.storage = FirebaseStorage.getInstance();
            this.storageRef = storage.getReference();
            Log.d(TAG, "Firebase Storage initialized successfully with default instance");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Storage", e);
            throw new RuntimeException("Failed to initialize Firebase Storage", e);
        }
    }

    public static ImageStorageService getInstance() {
        if (INSTANCE == null) {
            synchronized (ImageStorageService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageStorageService();
                }
            }
        }
        return INSTANCE;
    }

    public interface ImageUploadCallback {
        void onSuccess(@NonNull String downloadUrl);
        void onError(@NonNull Exception e);
        void onProgress(int progress);
    }

    public void uploadImage(@NonNull Context context, @NonNull Uri imageUri, @NonNull ImageUploadCallback callback) {
        // Check if user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError(new Exception("User must be authenticated to upload images"));
            return;
        }

        Log.d(TAG, "Starting image upload for URI: " + imageUri);

        try {
            // Convert URI to byte array
            byte[] imageData = uriToByteArray(context, imageUri);
            if (imageData == null) {
                callback.onError(new Exception("Failed to process image - could not read image data"));
                return;
            }

            Log.d(TAG, "Image processed, size: " + imageData.length + " bytes");

            // Generate unique filename with user ID and timestamp
            String fileName = user.getUid() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageRef.child(ALERT_IMAGES_PATH + fileName);

            Log.d(TAG, "Uploading to path: " + ALERT_IMAGES_PATH + fileName);

            // Create metadata
            com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .setCustomMetadata("uploadedBy", user.getUid())
                    .setCustomMetadata("uploadTime", String.valueOf(System.currentTimeMillis()))
                    .build();

            // Upload the image with metadata
            UploadTask uploadTask = imageRef.putBytes(imageData, metadata);

            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                callback.onProgress((int) progress);
                Log.d(TAG, "Upload progress: " + (int) progress + "%");
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Image uploaded successfully");
                // Get download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d(TAG, "Download URL obtained: " + downloadUrl);
                    callback.onSuccess(downloadUrl);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    callback.onError(new Exception("Upload successful but failed to get download URL: " + e.getMessage()));
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image", e);

                // Provide more specific error messages
                String errorMessage = "Failed to upload image: ";
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("User does not have permission")) {
                        errorMessage += "Permission denied. Please check Firebase Storage security rules.";
                    } else if (e.getMessage().contains("Object does not exist")) {
                        errorMessage += "Storage bucket not found. Please check Firebase configuration.";
                    } else if (e.getMessage().contains("Network error")) {
                        errorMessage += "Network error. Please check your internet connection.";
                    } else {
                        errorMessage += e.getMessage();
                    }
                } else {
                    errorMessage += "Unknown error occurred";
                }

                callback.onError(new Exception(errorMessage));
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception during upload preparation", e);
            callback.onError(new Exception("Failed to prepare image for upload: " + e.getMessage()));
        }
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

            // Resize if necessary
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

    public void deleteImage(@NonNull String imageUrl, @NonNull DeleteImageCallback callback) {
        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Image deleted successfully: " + imageUrl);
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete image: " + imageUrl, e);
                        callback.onError(e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception while deleting image: " + imageUrl, e);
            callback.onError(e);
        }
    }

    public interface DeleteImageCallback {
        void onSuccess();
        void onError(@NonNull Exception e);
    }
}
