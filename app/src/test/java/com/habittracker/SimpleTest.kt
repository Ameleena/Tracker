package com.habittracker

import org.junit.Test
import org.junit.Assert.*

class SimpleTest {
    
    @Test
    fun `test basic arithmetic`() {
        assertEquals(4, 2 + 2)
        assertEquals(10, 5 * 2)
        assertEquals(3, 9 / 3)
    }
    
    @Test
    fun `test string operations`() {
        val text = "Hello World"
        assertTrue(text.contains("Hello"))
        assertFalse(text.contains("Goodbye"))
        assertEquals(11, text.length)
    }
    
    @Test
    fun `test list operations`() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)
        assertTrue(list.contains(3))
        assertFalse(list.contains(6))
        assertEquals(15, list.sum())
    }
    
    @Test
    fun `test boolean operations`() {
        assertTrue(true)
        assertFalse(false)
        assertTrue(5 > 3)
        assertFalse(2 > 5)
    }
} 