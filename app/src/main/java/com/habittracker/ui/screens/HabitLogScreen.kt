package com.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habittracker.R
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitLog
import com.habittracker.ui.theme.habit_success
import com.habittracker.ui.theme.habit_missed
import com.habittracker.ui.viewmodels.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitLogScreen(
    habitId: Int,
    onNavigateBack: () -> Unit,
    habitViewModel: HabitViewModel = viewModel()
) {
    val habits by habitViewModel.habits.collectAsState()
    val logs by habitViewModel.getLogsForHabit(habitId).collectAsState()
    val habit = habits.find { it.id == habitId }
    
    var showDeleteDialog by remember { mutableStateOf<HabitLog?>(null) }

    LaunchedEffect(Unit) {
        habitViewModel.loadHabits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        habit?.name ?: stringResource(R.string.habit_log_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Информация о привычке
            habit?.let { h ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = h.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (h.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = h.description,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Кнопка отметки на сегодня
            val today = LocalDate.now()
            val isTodayCompleted = logs.any { log ->
                LocalDate.parse(log.date) == today
            }
            
            Button(
                onClick = {
                    if (!isTodayCompleted) {
                        val log = HabitLog(
                            id = 0,
                            habitId = habitId,
                            date = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            isCompleted = true
                        )
                        habitViewModel.addLog(log)
                    }
                },
                enabled = !isTodayCompleted,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTodayCompleted) 
                        MaterialTheme.colorScheme.secondary 
                    else 
                        habit_success
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isTodayCompleted) stringResource(R.string.today_completed) else stringResource(R.string.mark_completion),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Заголовок истории
            Text(
                text = stringResource(R.string.completion_history),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Список логов
            if (logs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_completion_records),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs.sortedByDescending { it.date }) { log ->
                        LogItem(
                            log = log,
                            onDeleteClick = { showDeleteDialog = log }
                        )
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления
    showDeleteDialog?.let { log ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_record)) },
            text = { 
                Text(
                    stringResource(R.string.delete_record_message, formatDate(log.date))
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        habitViewModel.deleteLog(log)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun LogItem(
    log: HabitLog,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (log.isCompleted) 
                                habit_success
                            else 
                                habit_missed,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = formatDate(log.date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            }
        }
    }
}

@Suppress("NewApi")
private fun formatDate(dateString: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateString)
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        when (date) {
            today -> "Сегодня"
            yesterday -> "Вчера"
            else -> {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                date.format(formatter)
            }
        }
    } catch (e: Exception) {
        dateString
    }
} 