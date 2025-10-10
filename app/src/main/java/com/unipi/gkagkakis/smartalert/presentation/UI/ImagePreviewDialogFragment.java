package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.unipi.gkagkakis.smartalert.R;

/**
 * Dialog fragment for previewing images with blurred background
 * Supports both Uri and Bitmap image sources following clean architecture
 */
public class ImagePreviewDialogFragment extends DialogFragment {
    private static final String ARG_HAS_BITMAP = "has_bitmap";
    private static final String ARG_IMAGE_URI = "image_uri";

    // Temporary storage for bitmap - will be cleared after use
    private Bitmap tempImageBitmap;
    private Bitmap tempBlurredBackground;

    /**
     * Creates instance with Uri image source
     */
    public static ImagePreviewDialogFragment newInstance(Uri imageUri, Bitmap blurredBackground) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, imageUri);
        args.putBoolean(ARG_HAS_BITMAP, false);
        fragment.setArguments(args);
        fragment.tempBlurredBackground = blurredBackground;
        return fragment;
    }

    /**
     * Creates instance with Bitmap image source
     */
    public static ImagePreviewDialogFragment newInstance(Bitmap imageBitmap, Bitmap blurredBackground) {
        ImagePreviewDialogFragment fragment = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_BITMAP, true);
        fragment.setArguments(args);
        fragment.tempImageBitmap = imageBitmap;
        fragment.tempBlurredBackground = blurredBackground;
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupDialogWindow();
    }

    private void setupDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            // Set blurred background if available
            if (tempBlurredBackground != null) {
                window.setBackgroundDrawable(new BitmapDrawable(getResources(), tempBlurredBackground));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_preview, container, false);

        ImageView imageView = view.findViewById(R.id.iv_preview);
        setupImageView(imageView);

        // Set click listener to dismiss dialog
        imageView.setOnClickListener(v -> dismiss());

        return view;
    }

    private void setupImageView(ImageView imageView) {
        Bundle args = getArguments();
        if (args == null) return;

        boolean hasBitmap = args.getBoolean(ARG_HAS_BITMAP, false);

        if (hasBitmap && tempImageBitmap != null) {
            // Display bitmap image
            imageView.setImageBitmap(tempImageBitmap);
        } else {
            // Display Uri image
            Uri imageUri = args.getParcelable(ARG_IMAGE_URI);
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear bitmap references to prevent memory leaks
        clearBitmapReferences();
    }

    private void clearBitmapReferences() {
        tempImageBitmap = null;
        tempBlurredBackground = null;
    }
}