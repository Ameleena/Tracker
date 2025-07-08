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
                val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
                var needCreate = false
                if (existingChannel == null) {
                    needCreate = true
                } else {
                    val existingSound = existingChannel.sound
                    if (existingSound == null || existingSound != soundUri) {
                        notificationManager.deleteNotificationChannel(CHANNEL_ID)
                        needCreate = true
                        Log.d("ReminderService", "Удалён старый канал $CHANNEL_ID из-за смены звука")
                    }
                }
                if (needCreate) {
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
                    Log.d("ReminderService", "Создан канал: $CHANNEL_ID со звуком $soundUri")
                }
            }
        }
        
        fun scheduleReminder(context: Context, habit: Habit) {
            Log.d("ReminderService", "=== scheduleReminder вызван ===")
            Log.d("ReminderService", "Привычка: ${habit.name} (ID: ${habit.id})")
            Log.d("ReminderService", "reminderEnabled: ${habit.reminderEnabled}")
            Log.d("ReminderService", "reminderTimes: '${habit.reminderTimes}'")
            Log.d("ReminderService", "reminderDays: '${habit.reminderDays}'")
            
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentSecond = calendar.get(java.util.Calendar.SECOND)
            
            Log.d("ReminderService", "Текущее время устройства: $currentYear-$currentMonth-$currentDay $currentHour:$currentMinute:$currentSecond")
            
            if (!habit.reminderEnabled || habit.reminderTimes.isBlank()) {
                Log.d("ReminderService", "Уведомления отключены или время не задано для привычки: ${habit.name}")
                return
            }
            
            Log.d("ReminderService", "Отменяем старые уведомления для привычки: ${habit.name}")
            cancelReminder(context, habit)
            
            Log.d("ReminderService", "Создаём общий канал уведомлений")
            createNotificationChannel(context)
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val times = habit.reminderTimes.split(",").mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            val reminderDays = if (habit.reminderDays.isNotBlank()) {
                habit.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                setOf(1, 2, 3, 4, 5, 6, 7)
            }
            Log.d("ReminderService", "Планируем уведомления для привычки ${habit.name}: времена=$times, дни=$reminderDays")
            times.forEachIndexed { timeIdx, timeStr ->
                val time = try {
                    LocalTime.parse(timeStr)
                } catch (e: Exception) {
                    Log.e("ReminderService", "Ошибка парсинга времени: $timeStr", e)
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
                    Log.d("ReminderService", "Создан PendingIntent для привычки ${habit.name}, время=$timeStr, день=$dayOfWeek, requestCode=$requestCode")
                    scheduleReminderForDay(context, alarmManager, pendingIntent, time, dayOfWeek, habit.name, timeStr)
                }
            }
            Log.d("ReminderService", "=== scheduleReminder завершен для привычки: ${habit.name} ===")
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
            Log.d("ReminderService", "=== scheduleReminderForDay вызван ===")
            Log.d("ReminderService", "Привычка: $habitName, время: $time, день недели: $dayOfWeek")
            
            // Получаем текущее время устройства через системные методы Android
            val calendar = java.util.Calendar.getInstance()
            val currentYear = calendar.get(java.util.Calendar.YEAR)
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH начинается с 0
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(java.util.Calendar.MINUTE)
            val currentSecond = calendar.get(java.util.Calendar.SECOND)
            val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1=воскресенье, 2=понедельник, ...
            
            // Конвертируем в LocalDate и LocalTime
            val now = LocalDate.of(currentYear, currentMonth, currentDay)
            val currentTime = LocalTime.of(currentHour, currentMinute, currentSecond)
            
            // Конвертируем день недели из Calendar (1=воскресенье) в нашу систему (1=понедельник)
            val androidDayOfWeek = when (currentDayOfWeek) {
                java.util.Calendar.SUNDAY -> 7 // воскресенье = 7
                java.util.Calendar.MONDAY -> 1 // понедельник = 1
                java.util.Calendar.TUESDAY -> 2 // вторник = 2
                java.util.Calendar.WEDNESDAY -> 3 // среда = 3
                java.util.Calendar.THURSDAY -> 4 // четверг = 4
                java.util.Calendar.FRIDAY -> 5 // пятница = 5
                java.util.Calendar.SATURDAY -> 6 // суббота = 6
                else -> 1
            }
            
            Log.d("ReminderService", "Конвертация дней недели:")
            Log.d("ReminderService", "Calendar.SUNDAY=${java.util.Calendar.SUNDAY}, Calendar.MONDAY=${java.util.Calendar.MONDAY}")
            Log.d("ReminderService", "Calendar.TUESDAY=${java.util.Calendar.TUESDAY}, Calendar.WEDNESDAY=${java.util.Calendar.WEDNESDAY}")
            Log.d("ReminderService", "Calendar.THURSDAY=${java.util.Calendar.THURSDAY}, Calendar.FRIDAY=${java.util.Calendar.FRIDAY}")
            Log.d("ReminderService", "Calendar.SATURDAY=${java.util.Calendar.SATURDAY}")
            Log.d("ReminderService", "Текущий день недели (Calendar): $currentDayOfWeek")
            Log.d("ReminderService", "Конвертированный день недели: $androidDayOfWeek")
            Log.d("ReminderService", "Целевой день недели: $dayOfWeek")
            
            val targetDayOfWeek = DayOfWeek.of(dayOfWeek)
            var targetDate = now
            
            Log.d("ReminderService", "=== Время с телефона ===")
            Log.d("ReminderService", "Год: $currentYear, Месяц: $currentMonth, День: $currentDay")
            Log.d("ReminderService", "Час: $currentHour, Минута: $currentMinute, Секунда: $currentSecond")
            Log.d("ReminderService", "День недели (Calendar): $currentDayOfWeek")
            Log.d("ReminderService", "День недели (конвертированный): $androidDayOfWeek")
            Log.d("ReminderService", "Текущая дата: $now")
            Log.d("ReminderService", "Текущее время: $currentTime")
            Log.d("ReminderService", "Целевой день недели: $dayOfWeek, целевое время: $time")
            Log.d("ReminderService", "System.currentTimeMillis(): ${System.currentTimeMillis()}")
            
            // Если сегодня уже нужный день недели
            if (androidDayOfWeek == dayOfWeek) {
                Log.d("ReminderService", "Сегодня уже нужный день недели: $androidDayOfWeek")
                // Если время еще не прошло сегодня, планируем на сегодня
                if (time.isAfter(currentTime)) {
                    targetDate = now
                    Log.d("ReminderService", "Время еще не прошло, планируем на сегодня: $targetDate")
                } else {
                    // Время уже прошло, планируем на следующий раз (через неделю)
                    targetDate = now.plusWeeks(1)
                    Log.d("ReminderService", "Время уже прошло, планируем на следующую неделю: $targetDate")
                }
            } else {
                // Находим следующий день недели
                while (targetDate.dayOfWeek.value != dayOfWeek) {
                    targetDate = targetDate.plusDays(1)
                }
                Log.d("ReminderService", "Найденная дата: $targetDate")
            }
            
            val targetDateTime = targetDate.atTime(time)
            // Используем локальный часовой пояс для конвертации в миллисекунды
            val zoneId = java.time.ZoneId.systemDefault()
            val triggerTime = targetDateTime.atZone(zoneId).toEpochSecond() * 1000
            
            Log.d("ReminderService", "Планируем будильник для привычки '$habitName' на $targetDateTime (${triggerTime}ms)")
            Log.d("ReminderService", "Текущее время в миллисекундах: ${System.currentTimeMillis()}")
            Log.d("ReminderService", "Разница во времени: ${triggerTime - System.currentTimeMillis()}ms")
            
            // Проверяем, что будильник не в прошлом
            if (triggerTime <= System.currentTimeMillis()) {
                Log.w("ReminderService", "Будильник планируется в прошлом! Пропускаем для привычки: $habitName")
                return
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val canSchedule = alarmManager.canScheduleExactAlarms()
                    Log.d("ReminderService", "canScheduleExactAlarms(): $canSchedule")
                    if (!canSchedule) {
                        Log.w("ReminderService", "Нет разрешения на точные будильники для привычки: $habitName")
                        // Открываем настройки для разрешения точных будильников
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return
                    }
                }
                
                Log.d("ReminderService", "Устанавливаем точный будильник для привычки: $habitName")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("ReminderService", "Будильник успешно установлен для привычки '$habitName' на $targetDateTime")
                
                // Проверяем, что будильник действительно установлен
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val nextAlarm = alarmManager.nextAlarmClock
                    Log.d("ReminderService", "Следующий будильник системы: $nextAlarm")
                }
                
            } catch (e: SecurityException) {
                Log.e("ReminderService", "Ошибка безопасности при установке будильника для привычки: $habitName", e)
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e("ReminderService", "Общая ошибка при установке будильника для привычки: $habitName", e)
                e.printStackTrace()
            }
            
            Log.d("ReminderService", "=== scheduleReminderForDay завершен для привычки: $habitName ===")
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
            createNotificationChannel(context)
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
            Log.d("ReminderService", "Отправлено тестовое уведомление через канал $channelId")
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
                    putExtra("reminder_sound_uri", habit.reminderSoundUri)
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
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d("ReminderService", "Тестовый будильник установлен на ${System.currentTimeMillis() + (secondsFromNow * 1000L)}")
                } catch (e: SecurityException) {
                    Log.e("ReminderService", "Ошибка безопасности при установке тестового будильника", e)
                    e.printStackTrace()
                } catch (e: Exception) {
                    Log.e("ReminderService", "Общая ошибка при установке тестового будильника", e)
                    e.printStackTrace()
                }
            }
        }
        
        // Метод для быстрого тестирования уведомлений через 5 секунд
        fun scheduleQuickTestReminder(context: Context, habit: Habit) {
            Log.d("ReminderService", "=== Быстрый тест уведомления ===")
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("habit_id", habit.id)
                putExtra("habit_name", habit.name)
                putExtra("reminder_time", "09:00")
                putExtra("reminder_days", "1")
                putExtra("reminder_sound_uri", habit.reminderSoundUri)
                putExtra("time_idx", 999)
            }
            
            val requestCode = habit.id * 1000 + 999999 // уникальный для быстрого теста
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val triggerTime = System.currentTimeMillis() + 5000L // через 5 секунд
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!(alarmManager.canScheduleExactAlarms())) {
                        Log.w("ReminderService", "Нет разрешения на точные будильники для быстрого теста")
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
                Log.d("ReminderService", "Быстрый тест: будильник установлен на ${triggerTime} (через 5 секунд)")
                Log.d("ReminderService", "Текущее время: ${System.currentTimeMillis()}")
                Log.d("ReminderService", "Разница: ${triggerTime - System.currentTimeMillis()}ms")
                
            } catch (e: SecurityException) {
                Log.e("ReminderService", "Ошибка безопасности при установке быстрого теста", e)
                e.printStackTrace()
            } catch (e: Exception) {
                Log.e("ReminderService", "Общая ошибка при установке быстрого теста", e)
                e.printStackTrace()
            }
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "=== onReceive вызван ===")
        Log.d("ReminderReceiver", "Intent action: ${intent.action}")
        Log.d("ReminderReceiver", "Intent extras: ${intent.extras?.keySet()?.joinToString()}")
        Log.d("ReminderReceiver", "Context: ${context.packageName}")
        Log.d("ReminderReceiver", "Текущее время: ${System.currentTimeMillis()}")
        Log.d("ReminderReceiver", "Intent data: ${intent.dataString}")
        Log.d("ReminderReceiver", "Intent type: ${intent.type}")
        Log.d("ReminderReceiver", "Intent flags: ${intent.flags}")
        
        val habitId = intent.getIntExtra("habit_id", 0)
        val habitName = intent.getStringExtra("habit_name") ?: "Привычка"
        val reminderTime = intent.getStringExtra("reminder_time") ?: "09:00"
        val reminderDays = intent.getStringExtra("reminder_days") ?: ""
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        val timeIdx = intent.getIntExtra("time_idx", 0)
        
        Log.d("ReminderReceiver", "Параметры: habitId=$habitId, habitName=$habitName, time=$reminderTime, dayOfWeek=$dayOfWeek, timeIdx=$timeIdx")
        
        if (habitId == 0) {
            Log.w("ReminderReceiver", "habitId равен 0, возможно проблема с передачей данных")
        }
        
        try {
            Log.d("ReminderReceiver", "Показываем уведомление для привычки: $habitName")
            showNotification(context, habitId, habitName)
            Log.d("ReminderReceiver", "Планируем следующее уведомление для привычки: $habitName")
            scheduleNextReminder(context, habitId, habitName, reminderTime, reminderDays, dayOfWeek, timeIdx)
            Log.d("ReminderReceiver", "Уведомление успешно обработано для привычки: $habitName")
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Ошибка в onReceive для привычки: $habitName", e)
            e.printStackTrace()
        }
    }
    
    private fun showNotification(context: Context, habitId: Int, habitName: String) {
        Log.d("ReminderReceiver", "showNotification: habitId=$habitId, habitName=$habitName")
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId = ReminderService.CHANNEL_ID
        Log.d("ReminderReceiver", "Используем канал: $channelId")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            Log.d("ReminderReceiver", "Существующий канал: ${existingChannel?.name}")
            if (existingChannel == null) {
                // Создаём канал с текущим звуком из PreferencesManager
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
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Напоминание о привычке")
            .setContentText("Не забудьте выполнить: $habitName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        Log.d("ReminderReceiver", "Отправляем уведомление с ID: $habitId")
        notificationManager.notify(habitId, notification)
        Log.d("ReminderReceiver", "Уведомление отправлено для привычки: $habitName через канал $channelId")
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
        Log.d("ReminderReceiver", "scheduleNextReminder: habitId=$habitId, habitName=$habitName, prevDayOfWeek=$prevDayOfWeek")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val days = if (reminderDays.isNotBlank()) {
            reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else {
            setOf(1, 2, 3, 4, 5, 6, 7)
        }
        Log.d("ReminderReceiver", "Дни недели для уведомлений: $days")
        
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
        if (!found) {
            Log.w("ReminderReceiver", "Не найден следующий день недели для привычки: $habitName")
            return
        }
        Log.d("ReminderReceiver", "Следующий день недели: $nextDay")
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
        
        // Найти дату следующего дня недели, используя системное время устройства
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
        
        Log.d("ReminderReceiver", "Следующее уведомление для привычки '$habitName' на $targetDateTime (${triggerTime}ms)")
        Log.d("ReminderReceiver", "Разница во времени: ${triggerTime - System.currentTimeMillis()}ms")
        
        // Проверяем, что будильник не в прошлом
        if (triggerTime <= System.currentTimeMillis()) {
            Log.w("ReminderReceiver", "Следующее уведомление планируется в прошлом! Пропускаем для привычки: $habitName")
            return
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!(alarmManager.canScheduleExactAlarms())) {
                    Log.w("ReminderReceiver", "Нет разрешения на точные будильники для следующего уведомления")
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
            Log.d("ReminderReceiver", "Следующее уведомление успешно запланировано для привычки: $habitName")
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "Ошибка безопасности при планировании следующего уведомления", e)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Общая ошибка при планировании следующего уведомления", e)
            e.printStackTrace()
        }
    }
} 