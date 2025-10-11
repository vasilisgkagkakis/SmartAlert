package com.unipi.gkagkakis.smartalert;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.unipi.gkagkakis.smartalert.Utils.LocaleManager;

/**
 * Custom Application class to handle app-wide localization
 * Ensures consistent language settings across the entire app
 */
public class SmartAlertApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        // Apply saved locale to the entire application
        super.attachBaseContext(LocaleManager.applyLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Reapply locale when configuration changes (e.g., system language change)
        LocaleManager.applyLocale(this);
    }
}
