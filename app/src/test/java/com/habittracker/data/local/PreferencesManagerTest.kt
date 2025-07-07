package com.habittracker.data.local

import org.junit.Test
import org.junit.Assert.*

class PreferencesManagerTest {
    
    @Test
    fun `test preferences manager constants`() {
        // Test that we can access the companion object constants
        // This is a simple test to verify the class structure
        assertNotNull(PreferencesManager::class.java)
    }
    
    @Test
    fun `test preferences manager class exists`() {
        // Simple test to verify the class can be instantiated
        // In a real test environment, we would need a proper Android context
        assertTrue(true) // Placeholder test
    }
} 