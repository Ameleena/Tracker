package com.habittracker.domain.usecase

import com.habittracker.domain.model.Habit
import com.habittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class GetAllHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> = repository.getAllHabits()
} 