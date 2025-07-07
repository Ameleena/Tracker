package com.habittracker.domain.repository

import com.habittracker.domain.model.MotivationalQuote

interface QuoteRepository {
    suspend fun getRandomQuote(): MotivationalQuote
    suspend fun getQuotesByCategory(category: String): List<MotivationalQuote>
} 