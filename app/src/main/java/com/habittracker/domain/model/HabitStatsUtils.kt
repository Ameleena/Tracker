package com.habittracker.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

// Статистика по привычке
data class HabitStats(
    val completedCount: Int,
    val totalDays: Int,
    val completionRate: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

data class HabitStatsCardData(
    val habit: Habit,
    val stats: HabitStats
)

fun calculateHabitStats(habit: Habit, logs: List<HabitLog>): HabitStats {
    if (habit.createdAt.isBlank()) {
        return HabitStats(0, 0, 0, 0, 0)
    }
    val createdDate = try {
        LocalDate.parse(habit.createdAt)
    } catch (e: Exception) {
        LocalDate.now()
    }
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(createdDate, today).toInt() + 1
    val completedDates = logs.mapNotNull {
        try {
            LocalDate.parse(it.date)
        } catch (e: Exception) {
            null
        }
    }.toSet()
    val completedCount = completedDates.size
    val completionRate = if (totalDays > 0) {
        ((completedCount.toFloat() / totalDays) * 100).roundToInt()
    } else 0
    // Текущая серия
    var currentStreak = 0
    var currentDate = today
    while (completedDates.contains(currentDate)) {
        currentStreak++
        currentDate = currentDate.minusDays(1)
    }
    // Лучшая серия
    var bestStreak = 0
    var tempStreak = 0
    var checkDate = createdDate
    while (checkDate <= today) {
        if (completedDates.contains(checkDate)) {
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
        bestStreak = bestStreak
    )
} 