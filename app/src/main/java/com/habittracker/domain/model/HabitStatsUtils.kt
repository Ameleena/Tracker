package com.habittracker.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

// Статистика по привычке
data class HabitStats(
    val completedCount: Int,
    val totalDays: Int,
    val completionRate: Int?,
    val currentStreak: Int,
    val bestStreak: Int,
    val overCompleted: Int = 0 // новое поле: перевыполнено на столько-то раз
)

data class HabitStatsCardData(
    val habit: Habit,
    val stats: HabitStats
)

fun calculateHabitStats(habit: Habit, logs: List<HabitLog>): HabitStats {
    if (habit.createdAt.isBlank()) {
        return HabitStats(0, 0, null, 0, 0)
    }
    val createdDate = try {
        LocalDate.parse(habit.createdAt)
    } catch (e: Exception) {
        LocalDate.now()
    }
    val today = LocalDate.now()
    val allDates = (0..ChronoUnit.DAYS.between(createdDate, today).toInt()).map { createdDate.plusDays(it.toLong()) }
    val reminderTimes = habit.reminderTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }
    val reminderDays = habit.reminderDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    val daysWithAnyLog = logs.mapNotNull { it.date }.toSet()
    val totalDays = daysWithAnyLog.size
    val completedCount = logs.count { it.isCompleted }
    if (reminderTimes.isEmpty()) {
        // Не считаем успешность для привычек без времени
        val completedDates = logs.filter { it.isCompleted }.mapNotNull { it.date }.toSet()
        var currentStreak = 0
        var currentDate = today
        while (completedDates.contains(currentDate.toString())) {
            currentStreak++
            currentDate = currentDate.minusDays(1)
        }
        var bestStreak = 0
        var tempStreak = 0
        var checkDate = createdDate
        while (checkDate <= today) {
            if (completedDates.contains(checkDate.toString())) {
                tempStreak++
                bestStreak = max(bestStreak, tempStreak)
            } else {
                tempStreak = 0
            }
            checkDate = checkDate.plusDays(1)
        }
        return HabitStats(
            completedCount = completedCount,
            totalDays = totalDays,
            completionRate = null,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            overCompleted = 0
        )
    }
    val totalPlanned = allDates.sumOf { date ->
        val dayOfWeek = date.dayOfWeek.value // 1=Пн, 7=Вс
        if (!habit.reminderEnabled || reminderTimes.isEmpty() || (reminderDays.isNotEmpty() && !reminderDays.contains(dayOfWeek))) 0
        else reminderTimes.size
    }
    val completionRate = if (totalPlanned > 0) ((completedCount.toFloat() / totalPlanned) * 100).roundToInt() else 0
    val overCompleted = when {
        totalPlanned == 0 && completedCount > 0 -> completedCount
        totalPlanned > 0 && completedCount > totalPlanned -> completedCount - totalPlanned
        else -> 0
    }
    // streak — по дням, если в дне выполнено хотя бы одно напоминание
    val completedDates = logs.filter { it.isCompleted }.mapNotNull { it.date }.toSet()
    var currentStreak = 0
    var currentDate = today
    while (completedDates.contains(currentDate.toString())) {
        currentStreak++
        currentDate = currentDate.minusDays(1)
    }
    var bestStreak = 0
    var tempStreak = 0
    var checkDate = createdDate
    while (checkDate <= today) {
        if (completedDates.contains(checkDate.toString())) {
            tempStreak++
            bestStreak = max(bestStreak, tempStreak)
        } else {
            tempStreak = 0
        }
        checkDate = checkDate.plusDays(1)
    }
    return HabitStats(
        completedCount = completedCount,
        totalDays = totalDays,
        completionRate = completionRate,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        overCompleted = overCompleted
    )
} 