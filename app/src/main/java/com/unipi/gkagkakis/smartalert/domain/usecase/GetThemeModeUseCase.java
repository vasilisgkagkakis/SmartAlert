package com.unipi.gkagkakis.smartalert.domain.usecase;

import com.unipi.gkagkakis.smartalert.domain.repository.SettingsRepository;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class GetThemeModeUseCase {
    private final SettingsRepository settingsRepository;

    public GetThemeModeUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public ThemeMode execute() {
        return settingsRepository.getThemeMode();
    }
}
