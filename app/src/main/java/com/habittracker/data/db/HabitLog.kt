package com.habittracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "habit_logs", indices = [Index(value = ["habitId", "date", "reminderTime"], unique = true)])
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String,
    val isCompleted: Boolean = false,
    val reminderTime: String? = null
) 