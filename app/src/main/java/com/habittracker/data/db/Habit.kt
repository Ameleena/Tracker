package com.habittracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val createdAt: String = "", // Дата создания в формате ISO
    val reminderEnabled: Boolean = false,
    val reminderTimes: String = "", // Времена напоминаний через запятую, например "09:00,14:00,20:00"
    val reminderDays: String = "", // Дни недели для напоминания (1,2,3,4,5,6,7)
    val reminderSoundUri: String = "" // Uri звука уведомления
) 