package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.unipi.gkagkakis.smartalert.data.repository.SettingsRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.ThemeUseCase;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class SettingsViewModel extends AndroidViewModel {
    private final ThemeUseCase themeUseCase;

    private final MutableLiveData<ThemeMode> _currentThemeMode = new MutableLiveData<>();
    public final LiveData<ThemeMode> currentThemeMode = _currentThemeMode;

    private final MutableLiveData<Boolean> _themeApplied = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        // Initialize dependencies internally (matching your project's pattern)
        SettingsRepositoryImpl settingsRepository = new SettingsRepositoryImpl(application);
        this.themeUseCase = new ThemeUseCase(settingsRepository);

        loadCurrentThemeMode();
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
}
