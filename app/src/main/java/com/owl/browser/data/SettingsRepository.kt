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
    }
    
    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_RUN] ?: true
    }
    
    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_RUN] = false
        }
    }
    
    val glassOpacity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GLASS_OPACITY] ?: 0.6f
    }
    
    val blurRadius: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[BLUR_RADIUS] ?: 16f
    }
}
