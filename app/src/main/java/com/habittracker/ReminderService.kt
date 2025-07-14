package com.habittracker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.habittracker.domain.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import android.util.Log
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.net.toUri
import com.habittracker.data.local.PreferencesManager

class ReminderService {
    companion object {
        const val CHANNEL_ID = "habit_reminders_channel"
        private const val CHANNEL_NAME = "Напоминания о привычках"
        
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                val prefs = PreferencesManager(context)
                val soundUriStr = prefs.getNotificationSoundUri()
                val soundUri = if (!soundUriStr.isNullOrBlank()) soundUriStr.toUri() else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                notificationManager.deleteNotificationChannel(CHANNEL_ID)
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Напоминания о выполнении привычек"
                    enableVibration(true)
                    enableLights(true)
                    setSound(soundUri, AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                }
                notificationManager.createNotificationChannel(channel)
                // Log.d("ReminderService", "Выбран звук для канала: $soundUri, title: ${RingtoneManager.getRingtone(context, soundUri)?.getTitle(context) ?: "(стандартный)"}")
            }
        }
        
        fun scheduleReminder(context: Context, habit: Habit) {
            // Log.d("ReminderService", "=== scheduleReminder вызван ===")
            // Log.d("ReminderService", "Привычка: ${habit.name} (ID: ${habit.id})")
            // Log.d("ReminderService", "reminderEnabled: ${habit.reminderEnabled}")
            // Log.d("ReminderService", "reminderTimes: '${habit.reminderTimes}'")
            // Log.d("ReminderService", "reminderDays: '${habit.reminderDays}'")
            
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentSecond = calendar.get(java.util.Calendar.SECOND)
            
            if (!habit.reminderEnabled || habit.reminderTimes.isBlank()) {
                return
            }
            
            // Log.d("ReminderService", "Отменяем старые уведомления для привычки: ${habit.name}")
            cancelReminder(context, habit)
            
            // Log.d("ReminderService", "Создаём общий канал уведомлений")
            createNotificationChannel(context)
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            val reminderDays = if (habit.reminderDays.isNotBlank()) {
                habit.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                setOf(1, 2, 3, 4, 5, 6, 7)
            }
            times.forEachIndexed { timeIdx, timeStr ->
                val time = try {
                    LocalTime.parse(timeStr)
                } catch (e: Exception) {
                    LocalTime.of(9, 0)
                }
                reminderDays.forEach { dayOfWeek ->
                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra("habit_id", habit.id)
                        putExtra("habit_name", habit.name)
                        putExtra("reminder_time", timeStr)
                        putExtra("reminder_days", habit.reminderDays)
                        putExtra("time_idx", timeIdx)
                    }
                    val requestCode = habit.id * 1000 + dayOfWeek * 100 + timeIdx
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    scheduleReminderForDay(context, alarmManager, pendingIntent, time, dayOfWeek, habit.name, timeStr)
                }
            }
        }
        
        private fun scheduleReminderForDay(
            context: Context,
            alarmManager: AlarmManager,
            pendingIntent: PendingIntent,
            time: LocalTime,
            dayOfWeek: Int,
            habitName: String,
            reminderTime: String
        ) {
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentSecond = calendar.get(java.util.Calendar.SECOND)
            val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            
            val now = LocalDate.of(currentYear, currentMonth, currentDay)
            val currentTime = LocalTime.of(currentHour, currentMinute, currentSecond)
            
            val androidDayOfWeek = when (currentDayOfWeek) {
                java.util.Calendar.SUNDAY -> 7
                java.util.Calendar.MONDAY -> 1
                java.util.Calendar.TUESDAY -> 2
                java.util.Calendar.WEDNESDAY -> 3
                java.util.Calendar.THURSDAY -> 4
                java.util.Calendar.FRIDAY -> 5
                java.util.Calendar.SATURDAY -> 6
                else -> 1
            }
            
            val targetDayOfWeek = DayOfWeek.of(dayOfWeek)
            var targetDate = now
            
            if (androidDayOfWeek == dayOfWeek) {
                if (time.isAfter(currentTime)) {
                    targetDate = now
                } else {
                    targetDate = now.plusWeeks(1)
                }
            } else {
                while (targetDate.dayOfWeek.value != dayOfWeek) {
                    targetDate = targetDate.plusDays(1)
                }
            }
            
            val targetDateTime = targetDate.atTime(time)
            val zoneId = java.time.ZoneId.systemDefault()
            val triggerTime = targetDateTime.atZone(zoneId).toEpochSecond() * 1000
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val canSchedule = alarmManager.canScheduleExactAlarms()
                    if (!canSchedule) {
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return
                    }
                }
                
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        fun cancelReminder(context: Context, habit: Habit) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            val reminderDays = if (habit.reminderDays.isNotBlank()) {
                habit.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                setOf(1, 2, 3, 4, 5, 6, 7)
            }
            times.forEachIndexed { timeIdx, _ ->
                reminderDays.forEach { dayOfWeek ->
                    val intent = Intent(context, ReminderReceiver::class.java)
                    val requestCode = habit.id * 1000 + dayOfWeek * 100 + timeIdx
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent?.let {
                        alarmManager.cancel(it)
                        it.cancel()
                    }
                }
            }
        }
        
        fun testNotification(context: Context, habit: Habit) {
            createNotificationChannel(context)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Тестовое уведомление")
                .setContentText("Уведомления работают!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(999, notification)
        }
        
        fun scheduleTestReminder(context: Context, habit: Habit, secondsFromNow: Int = 10) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            times.forEachIndexed { timeIdx, timeStr ->
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("habit_id", habit.id)
                    putExtra("habit_name", habit.name)
                    putExtra("reminder_time", timeStr)
                    putExtra("reminder_days", habit.reminderDays)
                    putExtra("reminder_sound_uri", habit.reminderSoundUri)
                    putExtra("is_test_notification", true) // Флаг для тестового уведомления
                }
                val requestCode = habit.id * 1000 + 999 + timeIdx
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val triggerTime = System.currentTimeMillis() + (secondsFromNow * 1000L)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!(alarmManager.canScheduleExactAlarms())) {
                            val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            intentSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intentSettings)
                            return
                        }
                    }
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        fun scheduleQuickTestReminder(context: Context, habit: Habit) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("habit_id", habit.id)
                putExtra("habit_name", habit.name)
                putExtra("reminder_time", "09:00")
                putExtra("reminder_days", "1")
                putExtra("reminder_sound_uri", habit.reminderSoundUri)
                putExtra("time_idx", 999)
                putExtra("is_test_notification", true) // Флаг для тестового уведомления
            }
            
            val requestCode = habit.id * 1000 + 999999
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val triggerTime = System.currentTimeMillis() + 5000L
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!(alarmManager.canScheduleExactAlarms())) {
                        val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intentSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intentSettings)
                        return
                    }
                }
                
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        fun cancelTestReminders(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Отменяем тестовые уведомления для привычки с ID 9996
            val testHabitId = 9996
            val times = listOf("09:00") // Время тестового уведомления
            times.forEachIndexed { timeIdx, _ ->
                val requestCode = testHabitId * 1000 + 999 + timeIdx
                val intent = Intent(context, ReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
                
                // Отменяем быстрые тестовые уведомления
                val quickRequestCode = testHabitId * 1000 + 999999
                val quickIntent = Intent(context, ReminderReceiver::class.java)
                val quickPendingIntent = PendingIntent.getBroadcast(
                    context,
                    quickRequestCode,
                    quickIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                quickPendingIntent?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habit_id", 0)
        val habitName = intent.getStringExtra("habit_name") ?: "Привычка"
        val reminderTime = intent.getStringExtra("reminder_time") ?: "09:00"
        val reminderDays = intent.getStringExtra("reminder_days") ?: ""
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val timeIdx = intent.getIntExtra("time_idx", 0)
        val isTestNotification = intent.getBooleanExtra("is_test_notification", false)
        
        if (habitId == 0) {
            return
        }
        
        try {
            showNotification(context, habitId, habitName)
            // Не создаем повторяющийся будильник для тестовых уведомлений
            if (!isTestNotification) {
                scheduleNextReminder(context, habitId, habitName, reminderTime, reminderDays, dayOfWeek, timeIdx)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showNotification(context: Context, habitId: Int, habitName: String) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(ReminderService.CHANNEL_ID)
            if (existingChannel == null) {
                ReminderService.createNotificationChannel(context)
            }
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Напоминание о привычке")
            .setContentText("Не забудьте выполнить: $habitName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(habitId, notification)
    }
    
    private fun scheduleNextReminder(
        context: Context,
        habitId: Int,
        habitName: String,
        reminderTime: String,
        reminderDays: String,
        prevDayOfWeek: Int,
        timeIdx: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val days = if (reminderDays.isNotBlank()) {
            reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else {
            setOf(1, 2, 3, 4, 5, 6, 7)
        }
        
        var nextDay = prevDayOfWeek
        var found = false
        for (i in 1..7) {
            nextDay = (prevDayOfWeek + i - 1) % 7 + 1
            if (days.contains(nextDay)) {
                found = true
                break
            }
        }
        if (!found) {
            return
        }
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("habit_id", habitId)
            putExtra("habit_name", habitName)
            putExtra("reminder_time", reminderTime)
            putExtra("reminder_days", reminderDays)
            putExtra("time_idx", timeIdx)
        }
        val requestCode = habitId * 1000 + nextDay * 100 + timeIdx
        val time = try {
            LocalTime.parse(reminderTime)
        } catch (e: Exception) {
            LocalTime.of(9, 0)
        }
        
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        
        var targetDate = LocalDate.of(currentYear, currentMonth, currentDay).plusDays(1)
        while (targetDate.dayOfWeek.value != nextDay) {
            targetDate = targetDate.plusDays(1)
        }
        val targetDateTime = targetDate.atTime(time)
        val zoneId = java.time.ZoneId.systemDefault()
        val triggerTime = targetDateTime.atZone(zoneId).toEpochSecond() * 1000
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!(alarmManager.canScheduleExactAlarms())) {
                    val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intentSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intentSettings)
                    return
                }
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 