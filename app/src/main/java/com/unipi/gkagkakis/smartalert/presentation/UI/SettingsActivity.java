package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
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
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
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
}
