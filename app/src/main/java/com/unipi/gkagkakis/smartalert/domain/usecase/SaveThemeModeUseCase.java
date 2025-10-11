package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.unipi.gkagkakis.smartalert.domain.repository.SettingsRepository;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class SaveThemeModeUseCase {
    private final SettingsRepository settingsRepository;

    public SaveThemeModeUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void execute(ThemeMode themeMode) {
        settingsRepository.saveThemeMode(themeMode);
    }
}
