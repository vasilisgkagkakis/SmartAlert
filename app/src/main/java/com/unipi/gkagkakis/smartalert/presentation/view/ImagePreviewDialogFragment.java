package com.unipi.gkagkakis.smartalert.presentation.view;

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
    private Uri imageUri;
    private Bitmap blurredBackground;

    public static ImagePreviewDialogFragment newInstance(Uri uri, Bitmap blurredBg) {
        ImagePreviewDialogFragment frag = new ImagePreviewDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, uri);
        frag.setArguments(args);
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
        imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
        ImageView imageView = view.findViewById(R.id.iv_preview);
        imageView.setImageURI(imageUri);
        imageView.setOnClickListener(v -> dismiss());
        return view;
    }
}