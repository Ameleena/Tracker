package com.habittracker.domain.usecase

import com.habittracker.domain.model.Habit
import com.habittracker.domain.repository.HabitRepository

class GetHabitByIdUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: Int): Habit? = repository.getHabitById(habitId)
} 