package com.unipi.gkagkakis.smartalert.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowInsetsController;

public class StatusBarHelper {
    @SuppressLint("ObsoleteSdkInt")
    public static void hideStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = activity.getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}