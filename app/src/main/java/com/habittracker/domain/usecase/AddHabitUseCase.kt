package com.habittracker.domain.usecase

import com.habittracker.domain.model.Habit
import com.habittracker.domain.repository.HabitRepository

class AddHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habit: Habit) = repository.insertHabit(habit)
} 