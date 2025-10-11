package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.data.repository.SettingsRepositoryImpl;
import com.unipi.gkagkakis.smartalert.domain.usecase.GetThemeModeUseCase;
import com.unipi.gkagkakis.smartalert.domain.usecase.SaveThemeModeUseCase;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel settingsViewModel;

    private LinearLayout optionFollowSystem;
    private LinearLayout optionLightTheme;
    private LinearLayout optionDarkTheme;
    private RadioButton radioFollowSystem;
    private RadioButton radioLightTheme;
    private RadioButton radioDarkTheme;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_settings);

        initializeViewModel();
        initializeViews();
        setupClickListeners();
        observeViewModel();
    }

    private void initializeViewModel() {
        SettingsRepositoryImpl settingsRepository = new SettingsRepositoryImpl(this);
        GetThemeModeUseCase getThemeModeUseCase = new GetThemeModeUseCase(settingsRepository);
        SaveThemeModeUseCase saveThemeModeUseCase = new SaveThemeModeUseCase(settingsRepository);

        // Create a proper ViewModelFactory to handle dependency injection
        SettingsViewModelFactory factory = new SettingsViewModelFactory(getThemeModeUseCase, saveThemeModeUseCase);
        settingsViewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        optionFollowSystem = findViewById(R.id.option_follow_system);
        optionLightTheme = findViewById(R.id.option_light_theme);
        optionDarkTheme = findViewById(R.id.option_dark_theme);
        radioFollowSystem = findViewById(R.id.radio_follow_system);
        radioLightTheme = findViewById(R.id.radio_light_theme);
        radioDarkTheme = findViewById(R.id.radio_dark_theme);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        optionFollowSystem.setOnClickListener(v -> setThemeMode(ThemeMode.FOLLOW_SYSTEM));

        optionLightTheme.setOnClickListener(v -> setThemeMode(ThemeMode.LIGHT));

        optionDarkTheme.setOnClickListener(v -> setThemeMode(ThemeMode.DARK));
    }

    private void observeViewModel() {
        settingsViewModel.currentThemeMode.observe(this, this::updateThemeSelection);
        // Removed the themeApplied observer to prevent infinite recreation loop
    }

    private void setThemeMode(ThemeMode themeMode) {
        // Only update the ViewModel to save the preference
        settingsViewModel.setThemeMode(themeMode);
        // Apply theme through ThemeManager and recreate once
        ThemeManager.setThemeMode(this, themeMode);
        // Recreate activity to apply new theme
        recreate();
    }

    private void updateThemeSelection(ThemeMode themeMode) {
        // Clear all selections
        radioFollowSystem.setChecked(false);
        radioLightTheme.setChecked(false);
        radioDarkTheme.setChecked(false);

        // Set the appropriate selection
        switch (themeMode) {
            case FOLLOW_SYSTEM:
                radioFollowSystem.setChecked(true);
                break;
            case LIGHT:
                radioLightTheme.setChecked(true);
                break;
            case DARK:
                radioDarkTheme.setChecked(true);
                break;
        }
    }

    /**
     * ViewModelFactory for SettingsViewModel to handle dependency injection
     * This eliminates the need for anonymous factory and fixes compiler warnings
     */
    private static class SettingsViewModelFactory implements ViewModelProvider.Factory {
        private final GetThemeModeUseCase getThemeModeUseCase;
        private final SaveThemeModeUseCase saveThemeModeUseCase;

        public SettingsViewModelFactory(GetThemeModeUseCase getThemeModeUseCase, SaveThemeModeUseCase saveThemeModeUseCase) {
            this.getThemeModeUseCase = getThemeModeUseCase;
            this.saveThemeModeUseCase = saveThemeModeUseCase;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
                return (T) new SettingsViewModel(getThemeModeUseCase, saveThemeModeUseCase);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
