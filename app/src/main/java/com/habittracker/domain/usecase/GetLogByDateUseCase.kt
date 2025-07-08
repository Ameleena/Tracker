package com.habittracker.domain.usecase

import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.repository.HabitLogRepository

class GetLogByDateUseCase(private val repository: HabitLogRepository) {
    suspend operator fun invoke(habitId: Int, date: String): HabitLog? = repository.getLogByDate(habitId, date)
}

class GetLogByDateAndTimeUseCase(private val repository: HabitLogRepository) {
    suspend operator fun invoke(habitId: Int, date: String, reminderTime: String): HabitLog? = repository.getLogByDateAndTime(habitId, date, reminderTime)
} 