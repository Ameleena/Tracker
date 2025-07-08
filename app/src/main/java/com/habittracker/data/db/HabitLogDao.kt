package com.habittracker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: HabitLog)

    @Update
    suspend fun updateLog(log: HabitLog)

    @Delete
    suspend fun deleteLog(log: HabitLog)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun getLogByDate(habitId: Int, date: String): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date AND reminderTime = :reminderTime LIMIT 1")
    suspend fun getLogByDateAndTime(habitId: Int, date: String, reminderTime: String): HabitLog?
} 