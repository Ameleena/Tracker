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
import kotlinx.coroutines.launch
import java.time.DayOfWeek

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
    val coroutineScope = rememberCoroutineScope()
    val reminderTimes = habit?.reminderTimes?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    val reminderDays = habit?.reminderDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList() // 1=Пн, 7=Вс

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

            // Кнопки отметки на сегодня по времени
            val today = LocalDate.now()
            val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayOfWeek = today.dayOfWeek.value // 1=Пн, 7=Вс
            if (reminderTimes.isNotEmpty() && reminderDays.contains(dayOfWeek)) {
                // Кнопки по времени (запланированные)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Отметить выполнение по времени:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    reminderTimes.forEach { time ->
                        val isCompleted = logs.any { log -> log.date == todayStr && log.reminderTime == time && log.isCompleted }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val canMark = habitViewModel.canMarkReminderTime(habitId, todayStr, time)
                                    if (canMark) {
                                        val log = HabitLog(
                                            id = 0,
                                            habitId = habitId,
                                            date = todayStr,
                                            isCompleted = true,
                                            reminderTime = time
                                        )
                                        habitViewModel.addLog(log)
                                    }
                                }
                            },
                            enabled = !isCompleted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted)
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
                                if (isCompleted) "${time} — выполнено" else "${time} — отметить",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    // Кнопка для ручного выполнения (вне расписания) — всегда активна
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val log = HabitLog(
                                    id = 0,
                                    habitId = habitId,
                                    date = todayStr,
                                    isCompleted = true,
                                    reminderTime = null
                                )
                                habitViewModel.addLog(log)
                            }
                        },
                        enabled = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = habit_success
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
                            "Вне расписания — отметить",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (reminderTimes.isNotEmpty() && !reminderDays.contains(dayOfWeek)) {
                // Сегодня не входит в расписание — показываем только кнопку "Вне расписания"
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val log = HabitLog(
                                id = 0,
                                habitId = habitId,
                                date = todayStr,
                                isCompleted = true,
                                reminderTime = null
                            )
                            habitViewModel.addLog(log)
                        }
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = habit_success
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
                        "Вне расписания — отметить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (reminderTimes.isEmpty()) {
                // Для привычек без времени — одна кнопка, всегда активна
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val log = HabitLog(
                                id = 0,
                                habitId = habitId,
                                date = todayStr,
                                isCompleted = true,
                                reminderTime = null
                            )
                            habitViewModel.addLog(log)
                        }
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = habit_success
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
                        "Выполнить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Заголовок истории
            Text(
                text = stringResource(R.string.completion_history),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Новый алгоритм истории
            val createdAt = habit?.createdAt?.let { LocalDate.parse(it) } ?: today
            val endDate = today
            val allDates = generateSequence(createdAt) { it.plusDays(1) }
                .takeWhile { !it.isAfter(endDate) }
                .toList()
                .reversed()
            val logsByDate = logs.groupBy { it.date }
            allDates.forEach { date ->
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dayOfWeekHistory = date.dayOfWeek.value // 1=Пн, 7=Вс
                val hasReminder = reminderDays.contains(dayOfWeekHistory)
                val dayLogs = logsByDate[dateStr] ?: emptyList()
                val hasAnyLog = dayLogs.isNotEmpty()
                if ((reminderTimes.isNotEmpty() && hasReminder) || hasAnyLog || reminderTimes.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = formatDate(dateStr),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            if (reminderTimes.isNotEmpty() && hasReminder) {
                                reminderTimes.forEach { time ->
                                    val log = dayLogs.find { it.reminderTime == time && it.isCompleted }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (log != null) habit_success else habit_missed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = time + if (log != null) " — выполнено" else " — не выполнено",
                                            color = if (log != null) habit_success else habit_missed,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                            // Для привычек без времени или "вне расписания" — считаем все логи без времени
                            val manualLogs = dayLogs.filter { it.reminderTime == null || it.reminderTime == "" }
                            if (manualLogs.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = habit_success,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Выполнено (x${manualLogs.size})",
                                        color = habit_success,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
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
                val timeText = log.reminderTime?.let { "${formatDate(log.date)}, $it" } ?: formatDate(log.date)
                Text(
                    text = timeText,
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