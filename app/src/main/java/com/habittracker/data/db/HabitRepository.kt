package com.habittracker.data.db

import com.habittracker.domain.model.Habit as DomainHabit
import com.habittracker.domain.repository.HabitRepository as DomainHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepository(private val habitDao: HabitDao) : DomainHabitRepository {
    override fun getAllHabits(): Flow<List<DomainHabit>> =
        habitDao.getAllHabits().map { list -> list.map { it.toDomain() } }

    override suspend fun insertHabit(habit: DomainHabit) =
        habitDao.insertHabit(habit.toData())

    override suspend fun updateHabit(habit: DomainHabit) =
        habitDao.updateHabit(habit.toData())

    override suspend fun deleteHabit(habit: DomainHabit) =
        habitDao.deleteHabit(habit.toData())

    override suspend fun getHabitById(habitId: Int): DomainHabit? =
        habitDao.getHabitById(habitId)?.toDomain()

    override fun getHabitsWithReminders(): Flow<List<DomainHabit>> =
        habitDao.getHabitsWithReminders().map { list -> list.map { it.toDomain() } }
}

fun Habit.toDomain() = DomainHabit(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    reminderEnabled = reminderEnabled,
    reminderTimes = reminderTimes,
    reminderDays = reminderDays
)

fun DomainHabit.toData() = Habit(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    reminderEnabled = reminderEnabled,
    reminderTimes = reminderTimes,
    reminderDays = reminderDays
) 