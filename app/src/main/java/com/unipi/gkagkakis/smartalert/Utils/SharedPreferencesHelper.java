package com.unipi.gkagkakis.smartalert.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    public static SharedPreferences getSharedPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }
}