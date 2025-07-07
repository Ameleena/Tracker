package com.habittracker.domain.model

import org.junit.Test
import org.junit.Assert.*

class HabitTest {
    
    @Test
    fun `test habit creation`() {
        val habit = Habit(
            id = 1,
            name = "Читать",
            description = "Читать 30 минут в день",
            createdAt = "2024-01-01"
        )
        
        assertEquals(1, habit.id)
        assertEquals("Читать", habit.name)
        assertEquals("Читать 30 минут в день", habit.description)
        assertEquals("2024-01-01", habit.createdAt)
    }
    
    @Test
    fun `test habit with empty description`() {
        val habit = Habit(
            id = 2,
            name = "Спорт",
            description = "",
            createdAt = "2024-01-02"
        )
        
        assertEquals(2, habit.id)
        assertEquals("Спорт", habit.name)
        assertEquals("", habit.description)
    }
} 