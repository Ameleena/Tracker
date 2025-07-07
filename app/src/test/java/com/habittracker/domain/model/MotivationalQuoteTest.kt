package com.habittracker.domain.model

import org.junit.Test
import org.junit.Assert.*

class MotivationalQuoteTest {
    
    @Test
    fun `test motivational quote creation`() {
        val quote = MotivationalQuote(
            id = "quote_1",
            text = "Успех - это способность шагать от одной неудачи к другой, не теряя энтузиазма.",
            author = "Уинстон Черчилль",
            category = "motivation"
        )
        
        assertEquals("quote_1", quote.id)
        assertEquals("Успех - это способность шагать от одной неудачи к другой, не теряя энтузиазма.", quote.text)
        assertEquals("Уинстон Черчилль", quote.author)
        assertEquals("motivation", quote.category)
    }
    
    @Test
    fun `test motivational quote with long text`() {
        val longText = "Это очень длинная мотивационная цитата, которая содержит много слов и должна правильно обрабатываться в приложении."
        val quote = MotivationalQuote(
            id = "quote_2",
            text = longText,
            author = "Неизвестный автор",
            category = "inspiration"
        )
        
        assertEquals(longText, quote.text)
        assertEquals("inspiration", quote.category)
    }
} 