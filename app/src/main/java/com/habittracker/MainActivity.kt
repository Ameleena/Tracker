package com.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.habittracker.data.local.PreferencesManager
import com.habittracker.ui.screens.MainScreen
import com.habittracker.ui.theme.HabitTrackerTheme
import com.habittracker.ui.viewmodels.HabitViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build

class MainActivity : ComponentActivity() {
    private val habitViewModel: HabitViewModel by viewModel()
    private val preferencesManager: PreferencesManager by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем канал уведомлений
        // ReminderService.createNotificationChannel(this, null)
        // Если нужен канал по умолчанию:
        // ReminderService.createNotificationChannel(this, Habit(id = 0, name = "Default", reminderSoundUri = ""))
        
        // Запрашиваем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        
        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)
            
            HabitTrackerTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(habitViewModel = habitViewModel)
                }
            }
        }
    }
} 