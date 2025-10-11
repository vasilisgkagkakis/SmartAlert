package com.unipi.gkagkakis.smartalert.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unipi.gkagkakis.smartalert.data.service.Base64ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public interface ImageLoadCallback {
        void onImageLoaded(@NonNull Bitmap bitmap);
        void onError(@NonNull Exception e);
    }

    public static void loadImage(@NonNull String imageUrl, @NonNull ImageView imageView, @Nullable ImageLoadCallback callback) {
        // Show placeholder or loading state
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);

        // Check if it's a Base64 image
        if (imageUrl.startsWith("data:image")) {
            // Handle Base64 image
            executor.execute(() -> {
                try {
                    Bitmap bitmap = Base64ImageService.decodeBase64ToBitmap(imageUrl);

                    // Switch back to main thread to update UI
                    imageView.post(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            if (callback != null) {
                                callback.onImageLoaded(bitmap);
                            }
                        } else {
                            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            if (callback != null) {
                                callback.onError(new Exception("Failed to decode Base64 image"));
                            }
                        }
                    });

                } catch (Exception e) {
                    imageView.post(() -> {
                        imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                        if (callback != null) {
                            callback.onError(e);
                        }
                    });
                }
            });
        } else {
            // Handle Firebase Storage URL or regular URL
            executor.execute(() -> {
                try {
                    Bitmap bitmap = downloadImage(imageUrl);

                    // Switch back to main thread to update UI
                    imageView.post(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            if (callback != null) {
                                callback.onImageLoaded(bitmap);
                            }
                        } else {
                            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            if (callback != null) {
                                callback.onError(new Exception("Failed to download image"));
                            }
                        }
                    });

                } catch (Exception e) {
                    imageView.post(() -> {
                        imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                        if (callback != null) {
                            callback.onError(e);
                        }
                    });
                }
            });
        }
    }

    @Nullable
    private static Bitmap downloadImage(@NonNull String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();

        InputStream input = connection.getInputStream();

        // Use BitmapFactory.Options for better quality decoding
        Options options = new Options();
        options.inPreferQualityOverSpeed = true; // Prefer quality over speed
        options.inDither = false; // Disable dithering for better quality
        options.inScaled = false; // Don't scale during decode

        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();
        connection.disconnect();

        return bitmap;
    }
}
