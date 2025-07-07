package com.habittracker.domain.usecase

import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.repository.HabitLogRepository

class DeleteHabitLogUseCase(private val repository: HabitLogRepository) {
    suspend operator fun invoke(log: HabitLog) = repository.deleteLog(log)
} 