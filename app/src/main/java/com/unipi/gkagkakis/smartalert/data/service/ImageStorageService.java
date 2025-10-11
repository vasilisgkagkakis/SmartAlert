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
import com.google.firebase.storage.StorageMetadata;
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
        FirebaseStorage tempStorage = null;
        StorageReference tempStorageRef = null;

        try {
            // Initialize Firebase Storage with proper error handling
            tempStorage = FirebaseStorage.getInstance();
            tempStorageRef = tempStorage.getReference();

            // Validate storage bucket configuration
            String bucketUrl = tempStorage.getApp().getOptions().getStorageBucket();
            if (bucketUrl == null || bucketUrl.isEmpty()) {
                Log.w(TAG, "Firebase Storage bucket not configured - uploads will fail gracefully");
            } else {
                Log.d(TAG, "Firebase Storage initialized successfully with bucket: " + bucketUrl);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Storage", e);
            // tempStorage and tempStorageRef remain null for graceful degradation
        }

        this.storage = tempStorage;
        this.storageRef = tempStorageRef;
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
        // Check if Firebase Storage is properly initialized
        if (storage == null || storageRef == null) {
            Log.w(TAG, "Firebase Storage not initialized, upload will fail");
            callback.onError(new Exception("Firebase Storage not configured. Please check your Firebase setup."));
            return;
        }

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
            StorageMetadata metadata = new StorageMetadata.Builder()
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
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
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
            bitmap = resizeBitmap(bitmap);

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
        }
    }

    private Bitmap resizeBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= ImageStorageService.MAX_IMAGE_WIDTH && height <= ImageStorageService.MAX_IMAGE_HEIGHT) {
            return bitmap;
        }

        float ratio = Math.min((float) ImageStorageService.MAX_IMAGE_WIDTH / width, (float) ImageStorageService.MAX_IMAGE_HEIGHT / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (resized != bitmap) {
            bitmap.recycle(); // Free original bitmap memory
        }
        return resized;
    }
}
