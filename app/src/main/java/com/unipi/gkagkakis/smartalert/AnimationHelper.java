package com.unipi.gkagkakis.smartalert;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationHelper {

    public static void startLogoAnimation(Context context, View logoView, int animationResource) {
        Animation animation = AnimationUtils.loadAnimation(context, animationResource);
        logoView.startAnimation(animation);
    }
}