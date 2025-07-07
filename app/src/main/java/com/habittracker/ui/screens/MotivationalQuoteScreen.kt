package com.habittracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habittracker.domain.model.MotivationalQuote
import com.habittracker.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// --- MVI State ---
data class QuoteState(
    val isLoading: Boolean = false,
    val quote: MotivationalQuote? = null,
    val error: String? = null
)

// --- MVI Intent ---
sealed class QuoteIntent {
    object LoadQuote : QuoteIntent()
    object Retry : QuoteIntent()
}

// --- MVI ViewModel ---
class QuoteMviViewModel(private val repository: QuoteRepository) : ViewModel() {
    private val _state = MutableStateFlow(QuoteState())
    val state: StateFlow<QuoteState> = _state

    fun processIntent(intent: QuoteIntent) {
        when (intent) {
            is QuoteIntent.LoadQuote, is QuoteIntent.Retry -> loadQuote()
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val quote = repository.getRandomQuote()
                _state.value = QuoteState(isLoading = false, quote = quote)
            } catch (e: Exception) {
                _state.value = QuoteState(isLoading = false, error = e.message)
            }
        }
    }
}

// --- MVI Composable UI ---
@Composable
fun MotivationalQuoteMviScreen(viewModel: QuoteMviViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.processIntent(QuoteIntent.LoadQuote) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ошибка: ${state.error}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.processIntent(QuoteIntent.Retry) }) {
                    Text("Повторить")
                }
            }
            state.quote != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.quote.text, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("— ${state.quote.author}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.processIntent(QuoteIntent.LoadQuote) }) {
                    Text("Ещё цитату")
                }
            }
        }
    }
}

@Composable
fun MotivationalQuoteScreen(
    viewModel: QuoteMviViewModel
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.processIntent(QuoteIntent.LoadQuote) }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ошибка: ${state.error}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.processIntent(QuoteIntent.Retry) }) {
                    Text("Повторить")
                }
            }
            state.quote != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.quote.text, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("— ${state.quote.author}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.processIntent(QuoteIntent.LoadQuote) }) {
                    Text("Ещё цитату")
                }
            }
        }
    }
}

@Composable
fun QuoteCard(quote: MotivationalQuote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = """"${quote.text}"""",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
} 