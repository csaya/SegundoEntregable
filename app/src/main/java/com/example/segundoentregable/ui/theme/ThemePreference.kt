package com.example.segundoentregable.ui.theme

import android.content.Context

enum class ThemePreference { SYSTEM, LIGHT, DARK }

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME = "theme_preference"

fun loadThemePreference(context: Context): ThemePreference {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return when (prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)) {
        ThemePreference.LIGHT.name -> ThemePreference.LIGHT
        ThemePreference.DARK.name -> ThemePreference.DARK
        else -> ThemePreference.SYSTEM
    }
}

fun saveThemePreference(context: Context, pref: ThemePreference) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_THEME, pref.name).apply()
}
