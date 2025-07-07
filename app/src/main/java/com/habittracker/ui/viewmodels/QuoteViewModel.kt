package com.habittracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.domain.model.MotivationalQuote
import com.habittracker.domain.usecase.GetRandomQuoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase
) : ViewModel() {
    
    private val _quote = MutableStateFlow<MotivationalQuote?>(null)
    val quote: StateFlow<MotivationalQuote?> = _quote
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadRandomQuote() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = getRandomQuoteUseCase()
                _quote.value = result
            } catch (e: Exception) {
                // Ошибка уже обработана в репозитории
                // Просто показываем текущую цитату или загружаем новую
                loadRandomQuote()
            } finally {
                _isLoading.value = false
            }
        }
    }
} 