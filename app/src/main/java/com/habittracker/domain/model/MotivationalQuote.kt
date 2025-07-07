package com.habittracker.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class MotivationalQuote(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val category: String = "motivation"
) 