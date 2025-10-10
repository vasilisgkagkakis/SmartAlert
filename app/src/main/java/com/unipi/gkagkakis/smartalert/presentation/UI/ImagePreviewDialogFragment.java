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

public class ImagePreviewDialogFragment extends DialogFragment {
    private static final String ARG_IMAGE_URI = "image_uri";
    private static final String ARG_HAS_BITMAP = "has_bitmap";
    private Bitmap imageBitmap;
    private Bitmap blurredBackground;

    public static ImagePreviewDialogFragment newInstance(Uri uri, Bitmap blurredBg) {
        ImagePreviewDialogFragment frag = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, uri);
        args.putBoolean(ARG_HAS_BITMAP, false);
        frag.setArguments(args);
        frag.blurredBackground = blurredBg;
        return frag;
    }

    public static ImagePreviewDialogFragment newInstance(Bitmap bitmap, Bitmap blurredBg) {
        ImagePreviewDialogFragment frag = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_BITMAP, true);
        frag.setArguments(args);
        frag.imageBitmap = bitmap;
        frag.blurredBackground = blurredBg;
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blurredBackground));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_preview, container, false);

        ImageView imageView = view.findViewById(R.id.iv_preview);

        Bundle args = getArguments();
        boolean hasBitmap = args != null && args.getBoolean(ARG_HAS_BITMAP, false);
        if (hasBitmap) {
            imageView.setImageBitmap(imageBitmap);
        } else {
            Uri imageUri = args != null ? args.getParcelable(ARG_IMAGE_URI) : null;
            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            }
        }

        imageView.setOnClickListener(v -> dismiss());
        return view;
    }
}