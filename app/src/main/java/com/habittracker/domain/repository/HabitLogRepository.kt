package com.habittracker.domain.repository

import com.habittracker.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow

interface HabitLogRepository {
    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>>
    suspend fun insertLog(log: HabitLog)
    suspend fun updateLog(log: HabitLog)
    suspend fun deleteLog(log: HabitLog)
    suspend fun getLogByDate(habitId: Int, date: String): HabitLog?
    suspend fun getLogByDateAndTime(habitId: Int, date: String, reminderTime: String): HabitLog?
} 