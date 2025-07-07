package com.habittracker.domain.usecase

import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.repository.HabitLogRepository

class UpdateHabitLogUseCase(private val repository: HabitLogRepository) {
    suspend operator fun invoke(log: HabitLog) = repository.updateLog(log)
} 