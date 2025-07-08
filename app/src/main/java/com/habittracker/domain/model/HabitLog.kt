package com.habittracker.domain.model

// Бизнес-модель HabitLog (без аннотаций Room)
data class HabitLog(
    val id: Int = 0,
    val habitId: Int,
    val date: String,
    val isCompleted: Boolean = false,
    val reminderTime: String? = null
) 