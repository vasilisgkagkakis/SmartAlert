package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.unipi.gkagkakis.smartalert.domain.repository.SettingsRepository;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

/**
 * Use case for handling theme operations following clean architecture
 * Manages both getting and setting theme preferences
 */
public class ThemeUseCase {
    private final SettingsRepository settingsRepository;

    public ThemeUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    /**
     * Gets the current theme mode
     */
    public ThemeMode getCurrentTheme() {
        return settingsRepository.getThemeMode();
    }

    /**
     * Saves the theme mode preference
     */
    public void saveTheme(ThemeMode themeMode) {
        settingsRepository.saveThemeMode(themeMode);
    }
}
