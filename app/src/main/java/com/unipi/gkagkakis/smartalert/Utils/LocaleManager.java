package com.unipi.gkagkakis.smartalert.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.unipi.gkagkakis.smartalert.model.Language;

import java.util.Locale;

/**
 * Utility class for managing app localization
 * Handles language switching and locale persistence
 */
public class LocaleManager {
    private static final String PREF_NAME = "locale_prefs";
    private static final String KEY_LANGUAGE = "selected_language";

    /**
     * Apply saved language to context
     */
    public static Context applyLocale(Context context) {
        String languageCode = getSavedLanguage(context);
        return setLocale(context, languageCode);
    }

    /**
     * Set and save new language
     */
    public static Context setNewLocale(Context context, Language language) {
        saveLanguage(context, language.getCode());
        return setLocale(context, language.getCode());
    }

    /**
     * Set locale for context
     */
    private static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Get saved language code
     */
    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, Language.ENGLISH.getCode());
    }

    /**
     * Get saved language enum
     */
    public static Language getSavedLanguageEnum(Context context) {
        String languageCode = getSavedLanguage(context);
        return Language.fromCode(languageCode);
    }

    /**
     * Save language preference
     */
    private static void saveLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
}
