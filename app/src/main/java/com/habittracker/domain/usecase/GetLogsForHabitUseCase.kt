package com.habittracker.domain.usecase

import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.repository.HabitLogRepository
import kotlinx.coroutines.flow.Flow

class GetLogsForHabitUseCase(private val repository: HabitLogRepository) {
    operator fun invoke(habitId: Int): Flow<List<HabitLog>> = repository.getLogsForHabit(habitId)
} 