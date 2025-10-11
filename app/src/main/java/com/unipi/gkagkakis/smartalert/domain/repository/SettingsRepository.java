package com.unipi.gkagkakis.smartalert.domain.repository;

import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public interface SettingsRepository {
    void saveThemeMode(ThemeMode themeMode);
    ThemeMode getThemeMode();
}
