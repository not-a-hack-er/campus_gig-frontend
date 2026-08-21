/**
 * ThemeViewModelFactory.kt — Factory for ThemeViewModel.
 *
 * ThemeViewModel requires an application Context to access SharedPreferences,
 * so it cannot be created with the default no-arg ViewModelProvider.Factory.
 */
package com.abpvt.campusgig_frontend.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
