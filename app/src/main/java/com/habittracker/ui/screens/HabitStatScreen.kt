package com.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.model.HabitStatsCardData
import com.habittracker.ui.theme.*
import com.habittracker.ui.viewmodels.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt
import java.time.temporal.ChronoUnit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun HabitStatScreen(
    habitViewModel: HabitViewModel
) {
    val habitStats by habitViewModel.habitStats.collectAsState()
    val isLoading by habitViewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Статистика привычек",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            habitStats.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Нет привычек для анализа",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                items(habitStats) { cardData ->
                    HabitStatCard(cardData)
                }
            }
        }
    }
}

@Composable
fun HabitStatCard(
    cardData: HabitStatsCardData
) {
    val habit = cardData.habit
    val stats = cardData.stats
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Заголовок карточки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (habit.description.isNotBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Иконка напоминания
                if (habit.reminderEnabled) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Напоминание включено",
                        tint = habit_reminder,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Основная статистика
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    imageVector = Icons.Default.CheckCircle,
                    value = stats.completedCount.toString(),
                    label = "Выполнено",
                    color = habit_success
                )
                
                StatItem(
                    imageVector = Icons.Default.List,
                    value = stats.totalDays.toString(),
                    label = "Всего дней",
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    imageVector = Icons.Default.List,
                    value = "${stats.completionRate}%",
                    label = "Успешность",
                    color = if (stats.completionRate >= 80) habit_success 
                           else if (stats.completionRate >= 50) habit_warning 
                           else habit_missed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Дополнительная статистика
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Текущая серия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Текущая серия:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.currentStreak} дней",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = habit_streak
                    )
                }
                
                // Лучшая серия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Лучшая серия:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.bestStreak} дней",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = habit_streak
                    )
                }
                
                // Дата создания
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Создана:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(habit.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Прогресс-бар
            if (stats.totalDays > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = stats.completionRate / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = if (stats.completionRate >= 80) habit_success 
                           else if (stats.completionRate >= 50) habit_warning 
                           else habit_missed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class HabitStats(
    val completedCount: Int,
    val totalDays: Int,
    val completionRate: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

fun calculateHabitStats(habit: Habit, logs: List<HabitLog>): HabitStats {
    if (habit.createdAt.isBlank()) {
        return HabitStats(0, 0, 0, 0, 0)
    }
    
    val createdDate = try {
        LocalDate.parse(habit.createdAt)
    } catch (e: Exception) {
        LocalDate.now()
    }
    
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(createdDate, today).toInt() + 1
    
    val completedDates = logs.map { log ->
        try {
            LocalDate.parse(log.date)
        } catch (e: Exception) {
            null
        }
    }.filterNotNull().toSet()
    
    val completedCount = completedDates.size
    val completionRate = if (totalDays > 0) {
        ((completedCount.toFloat() / totalDays) * 100).roundToInt()
    } else 0
    
    // Вычисляем текущую серию
    var currentStreak = 0
    var currentDate = today
    while (completedDates.contains(currentDate)) {
        currentStreak++
        currentDate = currentDate.minusDays(1)
    }
    
    // Вычисляем лучшую серию
    var bestStreak = 0
    var tempStreak = 0
    var checkDate = createdDate
    
    while (checkDate <= today) {
        if (completedDates.contains(checkDate)) {
            tempStreak++
            bestStreak = max(bestStreak, tempStreak)
        } else {
            tempStreak = 0
        }
        checkDate = checkDate.plusDays(1)
    }
    
    return HabitStats(
        completedCount = completedCount,
        totalDays = totalDays,
        completionRate = completionRate,
        currentStreak = currentStreak,
        bestStreak = bestStreak
    )
}

@Suppress("NewApi")
private fun formatDate(dateString: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateString)
        date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    } catch (e: Exception) {
        dateString
    }
} 