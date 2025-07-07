package com.habittracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun testStringOperations() {
        val testString = "Hello World"
        assertTrue(testString.contains("Hello"))
        assertFalse(testString.contains("Goodbye"))
    }
    
    @Test
    fun testListOperations() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)
        assertTrue(list.contains(3))
        assertFalse(list.contains(6))
    }
} 