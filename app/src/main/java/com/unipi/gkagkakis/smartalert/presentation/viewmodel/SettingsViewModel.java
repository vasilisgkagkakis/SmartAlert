package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.unipi.gkagkakis.smartalert.data.repository.SettingsRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.LanguageUseCase;
import com.unipi.gkagkakis.smartalert.domain.usecase.ThemeUseCase;
import com.unipi.gkagkakis.smartalert.model.Language;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class SettingsViewModel extends AndroidViewModel {
    private final ThemeUseCase themeUseCase;
    private final LanguageUseCase languageUseCase;

    private final MutableLiveData<ThemeMode> _currentThemeMode = new MutableLiveData<>();
    public final LiveData<ThemeMode> currentThemeMode = _currentThemeMode;

    private final MutableLiveData<Language> _currentLanguage = new MutableLiveData<>();
    public final LiveData<Language> currentLanguage = _currentLanguage;

    private final MutableLiveData<Boolean> _themeApplied = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _languageChanged = new MutableLiveData<>();
    public final LiveData<Boolean> languageChanged = _languageChanged;

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        // Initialize dependencies internally (matching your project's pattern)
        SettingsRepositoryImpl settingsRepository = new SettingsRepositoryImpl(application);
        this.themeUseCase = new ThemeUseCase(settingsRepository);
        this.languageUseCase = new LanguageUseCase(application);

        loadCurrentThemeMode();
        loadCurrentLanguage();
    }

    public void loadCurrentThemeMode() {
        ThemeMode themeMode = themeUseCase.getCurrentTheme();
        _currentThemeMode.setValue(themeMode);
    }

    public void setThemeMode(ThemeMode themeMode) {
        themeUseCase.saveTheme(themeMode);
        _currentThemeMode.setValue(themeMode);
        _themeApplied.setValue(true);
    }

    /**
     * Load current language setting
     */
    public void loadCurrentLanguage() {
        Language language = languageUseCase.getCurrentLanguage();
        _currentLanguage.setValue(language);
    }

    /**
     * Set new language and trigger app restart
     */
    public void setLanguage(Language language) {
        languageUseCase.setLanguage(language);
        _currentLanguage.setValue(language);
        _languageChanged.setValue(true);
    }

    /**
     * Get all available languages
     */
    public Language[] getAvailableLanguages() {
        return languageUseCase.getAvailableLanguages();
    }

    /**
     * Clear language changed state
     */
    public void clearLanguageChanged() {
        _languageChanged.setValue(false);
    }
}
