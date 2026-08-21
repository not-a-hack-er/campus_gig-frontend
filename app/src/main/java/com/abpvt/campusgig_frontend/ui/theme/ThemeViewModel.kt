/**
 * ThemeViewModel.kt — Global theme state manager for CampusGig.
 *
 * Persists the user's dark/light mode preference to SharedPreferences
 * so it survives app restarts. Exposes a reactive StateFlow so every
 * composable that reads it will recompose when the theme switches.
 */
package com.abpvt.campusgig_frontend.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME  = "campus_gig_theme_prefs"
private const val KEY_IS_DARK = "is_dark_theme"

class ThemeViewModel(context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Default: dark mode (existing app behaviour)
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK, true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        prefs.edit().putBoolean(KEY_IS_DARK, next).apply()
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
        prefs.edit().putBoolean(KEY_IS_DARK, dark).apply()
    }
}
