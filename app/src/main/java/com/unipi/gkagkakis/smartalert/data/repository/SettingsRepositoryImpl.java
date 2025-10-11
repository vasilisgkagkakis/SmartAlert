package com.unipi.gkagkakis.smartalert.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.unipi.gkagkakis.smartalert.domain.repository.SettingsRepository;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class SettingsRepositoryImpl implements SettingsRepository {
    private static final String PREFS_NAME = "settings_preferences";
    private static final String KEY_THEME_MODE = "theme_mode";

    private final SharedPreferences sharedPreferences;

    public SettingsRepositoryImpl(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void saveThemeMode(ThemeMode themeMode) {
        sharedPreferences.edit()
                .putString(KEY_THEME_MODE, themeMode.name())
                .apply();
    }

    @Override
    public ThemeMode getThemeMode() {
        String themeModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.FOLLOW_SYSTEM.name());
        try {
            return ThemeMode.valueOf(themeModeString);
        } catch (IllegalArgumentException e) {
            return ThemeMode.FOLLOW_SYSTEM;
        }
    }
}
