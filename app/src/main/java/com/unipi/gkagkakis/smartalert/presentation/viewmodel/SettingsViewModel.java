package com.unipi.gkagkakis.smartalert.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.unipi.gkagkakis.smartalert.domain.usecase.GetThemeModeUseCase;
import com.unipi.gkagkakis.smartalert.domain.usecase.SaveThemeModeUseCase;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;

public class SettingsViewModel extends ViewModel {
    private final GetThemeModeUseCase getThemeModeUseCase;
    private final SaveThemeModeUseCase saveThemeModeUseCase;

    private final MutableLiveData<ThemeMode> _currentThemeMode = new MutableLiveData<>();
    public final LiveData<ThemeMode> currentThemeMode = _currentThemeMode;

    private final MutableLiveData<Boolean> _themeApplied = new MutableLiveData<>();

    public SettingsViewModel(GetThemeModeUseCase getThemeModeUseCase, SaveThemeModeUseCase saveThemeModeUseCase) {
        this.getThemeModeUseCase = getThemeModeUseCase;
        this.saveThemeModeUseCase = saveThemeModeUseCase;
        loadCurrentThemeMode();
    }

    public void loadCurrentThemeMode() {
        ThemeMode themeMode = getThemeModeUseCase.execute();
        _currentThemeMode.setValue(themeMode);
    }

    public void setThemeMode(ThemeMode themeMode) {
        saveThemeModeUseCase.execute(themeMode);
        _currentThemeMode.setValue(themeMode);
        _themeApplied.setValue(true);
    }
}
