package com.habittracker.data.repository

import com.habittracker.data.api.QuoteApi
import com.habittracker.data.db.QuoteDao
import com.habittracker.domain.model.MotivationalQuote
import com.habittracker.domain.repository.QuoteRepository

class QuoteRepositoryImpl(
    private val api: QuoteApi,
    private val quoteDao: QuoteDao
) : QuoteRepository {
    
    // Локальный кэш цитат для разнообразия
    private val fallbackQuotes = listOf(
        MotivationalQuote("1", "Маленькие шаги каждый день приводят к большим изменениям.", "Неизвестный автор", "motivation"),
        MotivationalQuote("2", "Успех - это не случайность, это результат ежедневных усилий.", "Неизвестный автор", "success"),
        MotivationalQuote("3", "Привычка - это то, что ты делаешь, даже когда не хочешь.", "Неизвестный автор", "motivation"),
        MotivationalQuote("4", "Лучшее время начать - сейчас. Второе лучшее время - завтра.", "Неизвестный автор", "motivation"),
        MotivationalQuote("5", "Дисциплина - это мост между целями и достижениями.", "Джим Рон", "discipline"),
        MotivationalQuote("6", "Каждый день - это новая возможность стать лучше.", "Неизвестный автор", "motivation"),
        MotivationalQuote("7", "Привычки формируют характер, характер определяет судьбу.", "Неизвестный автор", "motivation"),
        MotivationalQuote("8", "Последовательность важнее совершенства.", "Неизвестный автор", "motivation"),
        MotivationalQuote("9", "Ты не можешь изменить свою жизнь за один день, но можешь изменить направление за один день.", "Неизвестный автор", "motivation"),
        MotivationalQuote("10", "Привычки - это сложные проценты самосовершенствования.", "Джеймс Клир", "motivation")
    )
    
    private var lastUsedIndex = -1
    
    override suspend fun getRandomQuote(): MotivationalQuote {
        return try {
            val response = api.getRandomQuote()
            val quote = MotivationalQuote(
                id = response._id,
                text = response.content,
                author = response.author,
                category = response.tags.firstOrNull() ?: "motivation"
            )
            // Кэшируем цитату
            quoteDao.insertQuotes(listOf(quote))
            quote
        } catch (e: Exception) {
            // В случае ошибки пробуем взять из кэша Room
            quoteDao.getRandomQuote() ?: getRandomFallbackQuote()
        }
    }
    
    private fun getRandomFallbackQuote(): MotivationalQuote {
        var randomIndex: Int
        do {
            randomIndex = (0 until fallbackQuotes.size).random()
        } while (randomIndex == lastUsedIndex && fallbackQuotes.size > 1)
        
        lastUsedIndex = randomIndex
        return fallbackQuotes[randomIndex]
    }
    
    override suspend fun getQuotesByCategory(category: String): List<MotivationalQuote> {
        return try {
            val response = api.getQuotesByCategory(category)
            val quotes = response.results.map { quoteResponse ->
                MotivationalQuote(
                    id = quoteResponse._id,
                    text = quoteResponse.content,
                    author = quoteResponse.author,
                    category = quoteResponse.tags.firstOrNull() ?: "motivation"
                )
            }
            // Кэшируем цитаты
            quoteDao.insertQuotes(quotes)
            quotes
        } catch (e: Exception) {
            // В случае ошибки пробуем взять из кэша Room
            val cached = quoteDao.getRandomQuoteByCategory(category)
            if (cached != null) listOf(cached) else fallbackQuotes.filter { it.category == category || category == "motivation" }
        }
    }
} 