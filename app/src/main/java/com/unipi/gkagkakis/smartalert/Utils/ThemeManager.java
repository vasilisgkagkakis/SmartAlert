package com.unipi.gkagkakis.smartalert.Utils;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.unipi.gkagkakis.smartalert.data.repository.SettingsRepositoryImpl;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class ThemeManager {
    private static SettingsRepositoryImpl settingsRepository;

    public static void initialize(Context context) {
        if (settingsRepository == null) {
            settingsRepository = new SettingsRepositoryImpl(context.getApplicationContext());
        }
    }

    public static void applyTheme(Context context) {
        initialize(context);
        ThemeMode themeMode = settingsRepository.getThemeMode();
        applyThemeMode(themeMode);
    }

    public static void setThemeMode(Context context, ThemeMode themeMode) {
        initialize(context);
        settingsRepository.saveThemeMode(themeMode);
        applyThemeMode(themeMode);
    }

    private static void applyThemeMode(ThemeMode themeMode) {
        switch (themeMode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case FOLLOW_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
