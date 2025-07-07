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
import java.time.format.DateTimeFormatter
import java.util.*
import android.util.Log
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri

class ReminderService {
    companion object {
        const val CHANNEL_ID = "habit_reminders"
        private const val CHANNEL_NAME = "Напоминания о привычках"
        
        fun createNotificationChannel(context: Context, habit: Habit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "habit_reminders_${habit.id}"
                val soundUri = if (!habit.reminderSoundUri.isNullOrBlank()) Uri.parse(habit.reminderSoundUri) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val channel = NotificationChannel(
                    channelId,
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
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
        
        fun scheduleReminder(context: Context, habit: Habit) {
            if (!habit.reminderEnabled || habit.reminderTimes.isBlank()) return
            cancelReminder(context, habit)
            createNotificationChannel(context, habit)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            val reminderDays = if (habit.reminderDays.isNotBlank()) {
                habit.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                setOf(1, 2, 3, 4, 5, 6, 7) // Все дни недели
            }
            // Для каждого времени и каждого дня недели создаём отдельный PendingIntent
            times.forEachIndexed { timeIdx, timeStr ->
                val time = try {
                    java.time.LocalTime.parse(timeStr)
                } catch (e: Exception) {
                    java.time.LocalTime.of(9, 0)
                }
                reminderDays.forEach { dayOfWeek ->
                    val intent = Intent(context, ReminderReceiver::class.java).apply {
                        putExtra("habit_id", habit.id)
                        putExtra("habit_name", habit.name)
                        putExtra("reminder_time", timeStr)
                        putExtra("reminder_days", habit.reminderDays)
                    }
                    // Уникальный requestCode для каждого времени и дня
                    val requestCode = habit.id * 1000 + dayOfWeek * 100 + timeIdx
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    scheduleReminderForDay(context, alarmManager, pendingIntent, time, dayOfWeek)
                }
            }
        }
        
        private fun scheduleReminderForDay(
            context: Context,
            alarmManager: AlarmManager,
            pendingIntent: PendingIntent,
            time: LocalTime,
            dayOfWeek: Int
        ) {
            val now = LocalDate.now()
            val targetDayOfWeek = DayOfWeek.of(dayOfWeek)
            var targetDate = now
            
            // Находим следующий день недели
            while (targetDate.dayOfWeek != targetDayOfWeek) {
                targetDate = targetDate.plusDays(1)
            }
            
            // Если время уже прошло сегодня, переносим на следующую неделю
            if (targetDate == now && time.isBefore(LocalTime.now())) {
                targetDate = targetDate.plusWeeks(1)
            }
            
            val targetDateTime = targetDate.atTime(time)
            val triggerTime = targetDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!(alarmManager.canScheduleExactAlarms())) {
                        // Открываем настройки для разрешения точных будильников
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
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
        
        // Метод для тестирования уведомлений
        fun testNotification(context: Context, habit: Habit) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channelId = "habit_reminders_${habit.id}"
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Тестовое уведомление")
                .setContentText("Уведомления работают!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(999, notification)
        }
        
        // Метод для планирования уведомления через несколько секунд (для тестирования)
        fun scheduleTestReminder(context: Context, habit: Habit, secondsFromNow: Int = 10) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            times.forEachIndexed { timeIdx, timeStr ->
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("habit_id", habit.id)
                    putExtra("habit_name", habit.name)
                    putExtra("reminder_time", timeStr)
                    putExtra("reminder_days", habit.reminderDays)
                }
                val requestCode = habit.id * 1000 + 999 + timeIdx // уникальный для теста
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
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
        Log.d("ReminderReceiver", "onReceive: habitId=$habitId, habitName=$habitName, time=$reminderTime, dayOfWeek=$dayOfWeek, timeIdx=$timeIdx")
        showNotification(context, habitId, habitName)
        scheduleNextReminder(context, habitId, habitName, reminderTime, reminderDays, dayOfWeek, timeIdx)
    }
    
    private fun showNotification(context: Context, habitId: Int, habitName: String) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId = "habit_reminders_${habitId}"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
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
        // Найти следующий день недели из списка
        var nextDay = prevDayOfWeek
        var found = false
        for (i in 1..7) {
            nextDay = (prevDayOfWeek + i - 1) % 7 + 1
            if (days.contains(nextDay)) {
                found = true
                break
            }
        }
        if (!found) return
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
        // Найти дату следующего дня недели
        var targetDate = LocalDate.now().plusDays(1)
        while (targetDate.dayOfWeek.value != nextDay) {
            targetDate = targetDate.plusDays(1)
        }
        val targetDateTime = targetDate.atTime(time)
        val triggerTime = targetDateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
} 