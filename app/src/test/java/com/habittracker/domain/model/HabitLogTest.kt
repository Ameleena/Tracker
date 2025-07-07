package com.habittracker.domain.model

import org.junit.Test
import org.junit.Assert.*

class HabitLogTest {
    
    @Test
    fun `test habit log creation`() {
        val habitLog = HabitLog(
            id = 1,
            habitId = 1,
            date = "2024-01-01",
            isCompleted = true
        )
        
        assertEquals(1, habitLog.id)
        assertEquals(1, habitLog.habitId)
        assertEquals("2024-01-01", habitLog.date)
        assertTrue(habitLog.isCompleted)
    }
    
    @Test
    fun `test habit log not completed`() {
        val habitLog = HabitLog(
            id = 2,
            habitId = 1,
            date = "2024-01-02",
            isCompleted = false
        )
        
        assertEquals(2, habitLog.id)
        assertFalse(habitLog.isCompleted)
    }
    
    @Test
    fun `test habit log with default values`() {
        val habitLog = HabitLog(
            habitId = 2,
            date = "2024-01-03"
        )
        
        assertEquals(0, habitLog.id)
        assertEquals(2, habitLog.habitId)
        assertEquals("2024-01-03", habitLog.date)
        assertFalse(habitLog.isCompleted) // default value
    }
} 