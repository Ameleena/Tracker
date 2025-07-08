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
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    private val habitViewModel: HabitViewModel by viewModel()
    private val preferencesManager: PreferencesManager by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Создаем канал уведомлений по умолчанию
        ReminderService.createNotificationChannel(this)
        
        // Запрашиваем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        
        // Проверяем разрешение на точные будильники для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Показываем диалог для перехода в настройки
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        
        // Перепланируем уведомления для всех существующих привычек
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                val habitsWithReminders = habitViewModel.getHabitsWithRemindersList().first()
                habitsWithReminders.forEach { habit ->
                    if (habit.reminderEnabled && habit.reminderTimes.isNotBlank()) {
                        ReminderService.scheduleReminder(this@MainActivity, habit)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка при перепланировании уведомлений", e)
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