package com.unipi.gkagkakis.smartalert.domain.usecase;

import android.content.Context;

import com.unipi.gkagkakis.smartalert.Utils.LocaleManager;
import com.unipi.gkagkakis.smartalert.model.Language;

/**
 * Use case for handling language operations following clean architecture
 * Manages language switching and retrieval
 */
public class LanguageUseCase {
    private final Context context;

    public LanguageUseCase(Context context) {
        this.context = context;
    }

    /**
     * Get currently selected language
     */
    public Language getCurrentLanguage() {
        return LocaleManager.getSavedLanguageEnum(context);
    }

    /**
     * Set new language and apply it
     */
    public Context setLanguage(Language language) {
        return LocaleManager.setNewLocale(context, language);
    }

    /**
     * Apply saved language to context
     */
    public Context applyCurrentLanguage() {
        return LocaleManager.applyLocale(context);
    }

    /**
     * Get all available languages
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }
}
