package com.habittracker.domain.model


data class Habit(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val createdAt: String = "",
    val reminderEnabled: Boolean = false,
    val reminderTimes: String = "",
    val reminderDays: String = "",
    val reminderSoundUri: String = ""
)

class HabitBuilder {
    private var id: Int = 0
    private var name: String = ""
    private var description: String = ""
    private var createdAt: String = ""
    private var reminderEnabled: Boolean = false
    private var reminderTimes: String = ""
    private var reminderDays: String = ""
    private var reminderSoundUri: String = ""

    fun setId(id: Int) = apply { this.id = id }
    fun setName(name: String) = apply { this.name = name }
    fun setDescription(description: String) = apply { this.description = description }
    fun setCreatedAt(createdAt: String) = apply { this.createdAt = createdAt }
    fun setReminderEnabled(reminderEnabled: Boolean) = apply { this.reminderEnabled = reminderEnabled }
    fun setReminderTimes(reminderTimes: String) = apply { this.reminderTimes = reminderTimes }
    fun setReminderDays(reminderDays: String) = apply { this.reminderDays = reminderDays }
    fun setReminderSoundUri(reminderSoundUri: String) = apply { this.reminderSoundUri = reminderSoundUri }

    fun build(): Habit {
        require(name.isNotBlank()) { "Имя привычки не может быть пустым" }
        return Habit(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            reminderEnabled = reminderEnabled,
            reminderTimes = reminderTimes,
            reminderDays = reminderDays,
            reminderSoundUri = reminderSoundUri
        )
    }
} 