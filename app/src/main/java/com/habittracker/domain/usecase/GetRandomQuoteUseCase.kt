package com.habittracker.domain.usecase

import com.habittracker.domain.model.MotivationalQuote
import com.habittracker.domain.repository.QuoteRepository

class GetRandomQuoteUseCase(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(): MotivationalQuote = repository.getRandomQuote()
} 