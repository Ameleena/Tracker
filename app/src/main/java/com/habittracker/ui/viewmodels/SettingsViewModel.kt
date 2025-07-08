package com.habittracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.data.local.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _isDarkMode = MutableStateFlow(preferencesManager.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(preferencesManager.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _lastQuoteId = MutableStateFlow(preferencesManager.getString("last_quote_id", null))
    val lastQuoteId: StateFlow<String?> = _lastQuoteId.asStateFlow()
    
    private val _notificationSoundUri = MutableStateFlow(preferencesManager.getNotificationSoundUri())
    val notificationSoundUri: StateFlow<String?> = _notificationSoundUri.asStateFlow()
    
    fun setDarkMode(enabled: Boolean) {
        preferencesManager.setDarkMode(enabled)
        _isDarkMode.value = enabled
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        preferencesManager.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }
    
    fun setLastQuoteId(quoteId: String?) {
        preferencesManager.setLastQuoteId(quoteId)
        _lastQuoteId.value = quoteId
    }
    
    fun setNotificationSoundUri(uri: String?) {
        preferencesManager.setNotificationSoundUri(uri)
        _notificationSoundUri.value = uri
    }
} 