package com.habittracker.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habittracker.ReminderService
import com.habittracker.ui.components.TimePickerDialog
import com.habittracker.ui.theme.habit_reminder
import com.habittracker.ui.viewmodels.HabitViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitViewModel: HabitViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    val reminderTimes = remember { mutableStateListOf<String>() }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var reminderSoundUri by remember { mutableStateOf("") }
    var reminderSoundTitle by remember { mutableStateOf("Системный по умолчанию") }
    val ringtonePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                reminderSoundUri = uri.toString()
                val ringtone = RingtoneManager.getRingtone(context, uri)
                reminderSoundTitle = ringtone?.getTitle(context) ?: "Пользовательский звук"
            } else {
                reminderSoundUri = ""
                reminderSoundTitle = "Системный по умолчанию"
            }
        }
    }

    val daysOfWeek = listOf(
        "Пн" to 1, "Вт" to 2, "Ср" to 3, "Чт" to 4,
        "Пт" to 5, "Сб" to 6, "Вс" to 7
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = "Новая привычка",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Название привычки
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Название привычки") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Star, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Описание
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Info, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Секция напоминаний
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = habit_reminder
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Напоминания",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Переключатель напоминаний
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Включить напоминания")
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = habit_reminder,
                            checkedTrackColor = habit_reminder.copy(alpha = 0.3f)
                        )
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Список времён напоминаний
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminderTimes.forEach { time ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(time, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { reminderTimes.remove(time) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Удалить время")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка добавления времени
                    Button(
                        onClick = { showTimePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить время")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Кнопка выбора звука уведомления
                    Button(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выберите звук уведомления")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, if (reminderSoundUri.isNotBlank()) Uri.parse(reminderSoundUri) else null)
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Выбрать звук уведомления")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Звук: $reminderSoundTitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Дни недели
                    Text(
                        text = "Дни недели:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(daysOfWeek) { (dayName, dayNumber) ->
                            val isSelected = selectedDays.contains(dayNumber)
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - dayNumber
                                    } else {
                                        selectedDays + dayNumber
                                    }
                                },
                                label = { Text(dayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = habit_reminder.copy(alpha = 0.3f),
                                    selectedLabelColor = habit_reminder
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка сохранения
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    val reminderDaysString = if (selectedDays.isNotEmpty()) {
                        selectedDays.sorted().joinToString(",")
                    } else ""
                    val reminderTimesString = reminderTimes.joinToString(",")
                    
                    habitViewModel.addHabitAsyncWithBuilder(
                        name = name,
                        description = description,
                        reminderEnabled = reminderEnabled,
                        reminderTimes = if (reminderEnabled) reminderTimesString else "",
                        reminderDays = reminderDaysString,
                        context = context,
                        reminderSoundUri = reminderSoundUri
                    )
                    onNavigateBack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сохранить привычку")
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = "09:00", // По умолчанию для нового времени
            onTimeSelected = { newTime ->
                if (!reminderTimes.contains(newTime)) {
                    reminderTimes.add(newTime)
                    reminderTimes.sort() // Сортируем для порядка
                }
            },
            onDismiss = { showTimePicker = false }
        )
    }
} 