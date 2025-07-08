package com.habittracker.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: Flow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: Flow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _lastQuoteId = MutableStateFlow(prefs.getString(KEY_LAST_QUOTE_ID, null))
    val lastQuoteId: Flow<String?> = _lastQuoteId.asStateFlow()
    
    fun setDarkMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_MODE, enabled) }
        _isDarkMode.value = enabled
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
        _notificationsEnabled.value = enabled
    }
    
    fun setLastQuoteId(quoteId: String?) {
        prefs.edit { putString(KEY_LAST_QUOTE_ID, quoteId) }
        _lastQuoteId.value = quoteId
    }
    
    fun getString(key: String, defaultValue: String? = null): String? {
        return prefs.getString(key, defaultValue)
    }
    
    fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }
    
    fun setNotificationSoundUri(uri: String?) {
        prefs.edit { putString(KEY_NOTIFICATION_SOUND_URI, uri) }
    }
    
    fun getNotificationSoundUri(): String? {
        return prefs.getString(KEY_NOTIFICATION_SOUND_URI, null)
    }
    
    companion object {
        private const val PREF_NAME = "habit_tracker_prefs"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_LAST_QUOTE_ID = "last_quote_id"
        private const val KEY_NOTIFICATION_SOUND_URI = "notification_sound_uri"
    }
} 