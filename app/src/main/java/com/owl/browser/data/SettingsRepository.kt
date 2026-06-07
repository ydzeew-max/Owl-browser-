package com.owl.browser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val FIRST_RUN = booleanPreferencesKey("first_run")
        val GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        val BLUR_RADIUS = floatPreferencesKey("blur_radius")
        val BOTTOM_BAR_STYLE = booleanPreferencesKey("bottom_bar_floating")
        val THEME = androidx.datastore.preferences.core.stringPreferencesKey("theme")
        val SEARCH_ENGINE = androidx.datastore.preferences.core.stringPreferencesKey("search_engine")
        val LANGUAGE = androidx.datastore.preferences.core.stringPreferencesKey("language")
    }
    
    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_RUN] ?: true
    }
    
    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_RUN] = false
        }
    }
    
    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME] ?: "Dark (Recommended)"
    }
    
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }
    
    val searchEngine: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SEARCH_ENGINE] ?: "Google"
    }
    
    suspend fun setSearchEngine(engine: String) {
        context.dataStore.edit { preferences ->
            preferences[SEARCH_ENGINE] = engine
        }
    }
    
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "English"
    }
    
    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    
    val glassOpacity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GLASS_OPACITY] ?: 0.6f
    }
    
    val blurRadius: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[BLUR_RADIUS] ?: 16f
    }
}
