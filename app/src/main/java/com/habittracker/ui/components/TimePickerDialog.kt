package com.habittracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val time = try {
        LocalTime.parse(currentTime)
    } catch (e: Exception) {
        LocalTime.of(9, 0)
    }
    
    var selectedHour by remember { mutableStateOf(time.hour) }
    var selectedMinute by remember { mutableStateOf(time.minute) }
    var manualTimeInput by remember { mutableStateOf(String.format("%02d:%02d", selectedHour, selectedMinute)) }
    var showManualInput by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Выберите время",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Time display
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Manual input toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ручной ввод",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = showManualInput,
                        onCheckedChange = { showManualInput = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                
                if (showManualInput) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Manual input field
                    OutlinedTextField(
                        value = manualTimeInput,
                        onValueChange = { input ->
                            // Ограничиваем ввод только цифрами
                            val digitsOnly = input.filter { it.isDigit() }
                            
                            // Форматируем в ЧЧ:ММ
                            val formatted = when {
                                digitsOnly.length <= 2 -> {
                                    val hour = digitsOnly.take(2).padStart(2, '0')
                                    "$hour:"
                                }
                                digitsOnly.length <= 4 -> {
                                    val hour = digitsOnly.take(2).padStart(2, '0')
                                    val minute = digitsOnly.drop(2).take(2).padStart(2, '0')
                                    "$hour:$minute"
                                }
                                else -> {
                                    val hour = digitsOnly.take(2).padStart(2, '0')
                                    val minute = digitsOnly.drop(2).take(2).padStart(2, '0')
                                    "$hour:$minute"
                                }
                            }
                            
                            manualTimeInput = formatted
                            
                            // Парсим время
                            val parts = formatted.split(":")
                            if (parts.size == 2) {
                                val hour = parts[0].toIntOrNull() ?: 0
                                val minute = parts[1].toIntOrNull() ?: 0
                                
                                if (hour in 0..23 && minute in 0..59) {
                                    selectedHour = hour
                                    selectedMinute = minute
                                }
                            }
                        },
                        label = { Text("ЧЧ:ММ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Time picker controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour picker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Часы",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            IconButton(
                                onClick = { selectedHour = (selectedHour + 1) % 24 },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Увеличить часы",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Text(
                                text = String.format("%02d", selectedHour),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = { 
                                    selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 
                                },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Уменьшить часы",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Separator
                        Text(
                            ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Minute picker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Минуты",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute + 1) % 60 },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Увеличить минуты",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            
                            Text(
                                text = String.format("%02d", selectedMinute),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(
                                onClick = { 
                                    selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59 
                                },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Уменьшить минуты",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick time presets
                Text(
                    "Быстрый выбор:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("06:00", "09:00", "12:00", "18:00", "21:00")) { presetTime ->
                        val (presetHour, presetMinute) = presetTime.split(":").map { it.toInt() }
                        FilterChip(
                            selected = selectedHour == presetHour && selectedMinute == presetMinute,
                            onClick = {
                                selectedHour = presetHour
                                selectedMinute = presetMinute
                                manualTimeInput = String.format("%02d:%02d", selectedHour, selectedMinute)
                            },
                            label = {
                                Text(
                                    presetTime,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.width(64.dp),
                                    textAlign = TextAlign.Center
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(newTime)
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Отмена")
            }
        }
    )
} 