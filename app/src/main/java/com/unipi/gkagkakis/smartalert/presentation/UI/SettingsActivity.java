package com.unipi.gkagkakis.smartalert.presentation.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.unipi.gkagkakis.smartalert.R;
import com.unipi.gkagkakis.smartalert.Utils.LocaleManager;
import com.unipi.gkagkakis.smartalert.Utils.ThemeManager;
import com.unipi.gkagkakis.smartalert.model.Language;
import com.unipi.gkagkakis.smartalert.model.ThemeMode;
import com.unipi.gkagkakis.smartalert.presentation.viewmodel.SettingsViewModel;

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel settingsViewModel;

    // Theme UI elements
    private LinearLayout optionFollowSystem;
    private LinearLayout optionLightTheme;
    private LinearLayout optionDarkTheme;
    private RadioButton radioFollowSystem;
    private RadioButton radioLightTheme;
    private RadioButton radioDarkTheme;

    // Language UI elements
    private LinearLayout optionEnglish;
    private LinearLayout optionGreek;
    private RadioButton radioEnglish;
    private RadioButton radioGreek;

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

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        // Apply locale before activity is created
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }

    private void initializeViewModel() {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);

        // Theme views
        optionFollowSystem = findViewById(R.id.option_follow_system);
        optionLightTheme = findViewById(R.id.option_light_theme);
        optionDarkTheme = findViewById(R.id.option_dark_theme);
        radioFollowSystem = findViewById(R.id.radio_follow_system);
        radioLightTheme = findViewById(R.id.radio_light_theme);
        radioDarkTheme = findViewById(R.id.radio_dark_theme);

        // Language views
        optionEnglish = findViewById(R.id.option_english);
        optionGreek = findViewById(R.id.option_greek);
        radioEnglish = findViewById(R.id.radio_english);
        radioGreek = findViewById(R.id.radio_greek);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Theme click listeners
        optionFollowSystem.setOnClickListener(v -> setThemeMode(ThemeMode.FOLLOW_SYSTEM));
        optionLightTheme.setOnClickListener(v -> setThemeMode(ThemeMode.LIGHT));
        optionDarkTheme.setOnClickListener(v -> setThemeMode(ThemeMode.DARK));

        // Language click listeners
        optionEnglish.setOnClickListener(v -> setLanguage(Language.ENGLISH));
        optionGreek.setOnClickListener(v -> setLanguage(Language.GREEK));
    }

    private void observeViewModel() {
        // Observe theme changes
        settingsViewModel.currentThemeMode.observe(this, this::updateThemeSelection);

        // Observe language changes
        settingsViewModel.currentLanguage.observe(this, this::updateLanguageSelection);

        // Observe language change trigger
        settingsViewModel.languageChanged.observe(this, languageChanged -> {
            if (languageChanged != null && languageChanged) {
                // Show toast message
                Toast.makeText(this, getString(R.string.language_changed), Toast.LENGTH_SHORT).show();

                // Clear the language changed state
                settingsViewModel.clearLanguageChanged();

                // Restart the app to apply language change
                restartApp();
            }
        });
    }

    private void setThemeMode(ThemeMode themeMode) {
        // Only update the ViewModel to save the preference
        settingsViewModel.setThemeMode(themeMode);
        // Apply theme through ThemeManager and recreate once
        ThemeManager.setThemeMode(this, themeMode);
        // Recreate activity to apply new theme
        recreate();
    }

    private void setLanguage(Language language) {
        // Only change if it's different from current
        Language currentLanguage = settingsViewModel.currentLanguage.getValue();
        if (currentLanguage != null && currentLanguage != language) {
            settingsViewModel.setLanguage(language);
        }
    }

    private void updateThemeSelection(ThemeMode themeMode) {
        // Clear all theme selections
        radioFollowSystem.setChecked(false);
        radioLightTheme.setChecked(false);
        radioDarkTheme.setChecked(false);

        // Set the appropriate theme selection
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

    private void updateLanguageSelection(Language language) {
        // Clear all language selections
        radioEnglish.setChecked(false);
        radioGreek.setChecked(false);

        // Set the appropriate language selection
        switch (language) {
            case ENGLISH:
                radioEnglish.setChecked(true);
                break;
            case GREEK:
                radioGreek.setChecked(true);
                break;
        }
    }

    /**
     * Restart the entire app to apply language changes
     */
    private void restartApp() {
        // Get the main activity intent
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity(); // Close all activities
        }
    }
}
