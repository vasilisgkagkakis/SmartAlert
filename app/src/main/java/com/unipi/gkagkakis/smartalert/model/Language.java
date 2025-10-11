package com.unipi.gkagkakis.smartalert.model;

/**
 * Enum representing supported languages in the app
 */
public enum Language {
    ENGLISH("en", "English"),
    GREEK("el", "Ελληνικά");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Language from language code
     */
    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return ENGLISH; // Default fallback
    }
}
