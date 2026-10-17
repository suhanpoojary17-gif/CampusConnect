package com.campusconnect.app.core.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private static final String PREFS_NAME = "campusconnect_settings";
    private static final String KEY_THEME = "app_theme";

    public static final String ALIEN_LIGHT = "alien_light";
    public static final String NIGHT = "night";
    public static final String SYSTEM = "system";

    private ThemeManager() {
    }

    public static String getTheme(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(KEY_THEME, SYSTEM);
    }

    public static void applyTheme(Context context) {
        String selectedTheme = getTheme(context);

        if (NIGHT.equals(selectedTheme)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );

        } else if (ALIEN_LIGHT.equals(selectedTheme)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );

        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            );
        }
    }

    public static void setTheme(
            Context context,
            String selectedTheme
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_THEME, selectedTheme)
                .apply();

        applyTheme(context);
    }
}