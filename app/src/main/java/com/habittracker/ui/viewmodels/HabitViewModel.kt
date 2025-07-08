package com.habittracker.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.habittracker.ReminderService
import com.habittracker.domain.model.Habit
import com.habittracker.domain.model.HabitLog
import com.habittracker.domain.usecase.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.habittracker.domain.model.HabitBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import com.habittracker.domain.model.HabitStats
import com.habittracker.domain.model.HabitStatsCardData
import com.habittracker.domain.model.calculateHabitStats
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

data class HabitStatsCardData(
    val habit: Habit,
    val stats: HabitStats
)

class HabitViewModel(
    private val getAllHabitsUseCase: GetAllHabitsUseCase,
    private val addHabitUseCase: AddHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitsWithRemindersUseCase: GetHabitsWithRemindersUseCase,
    private val getLogsForHabitUseCase: GetLogsForHabitUseCase,
    private val addHabitLogUseCase: AddHabitLogUseCase,
    private val updateHabitLogUseCase: UpdateHabitLogUseCase,
    private val deleteHabitLogUseCase: DeleteHabitLogUseCase,
    private val getLogByDateUseCase: GetLogByDateUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // StateFlow для ошибок (асинхронная обработка ошибок)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // StateFlow — реактивный поток данных (асинхронность)
    val habits: StateFlow<List<Habit>> = getAllHabitsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _habitStats = MutableStateFlow<List<HabitStatsCardData>>(emptyList())
    val habitStats: StateFlow<List<HabitStatsCardData>> = _habitStats

    // Пример отмены корутины
    private var loadJob: Job? = null
    fun loadHabitsWithContext() {
        loadJob?.cancel() // Отмена предыдущей корутины
        loadJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                val habits = withContext(Dispatchers.IO) {
                    getAllHabitsUseCase().firstOrNull() ?: emptyList()
                }
                // ... обновить UI
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Асинхронная загрузка и обновление статистики
    init {
        viewModelScope.launch {
            getAllHabitsUseCase().collect { habitsList ->
                val statsList = withContext(Dispatchers.IO) {
                    habitsList.map { habit ->
                        val logs = getLogsForHabitUseCase(habit.id).firstOrNull() ?: emptyList()
                        HabitStatsCardData(habit, calculateHabitStats(habit, logs))
                    }
                }
                _habitStats.value = statsList
                if (habitsList.isNotEmpty()) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadHabits() {
        // Данные уже загружаются автоматически через StateFlow
    }

    fun addHabit(name: String, description: String, reminderEnabled: Boolean = false, reminderTimes: String = "", reminderDays: String = "", context: Context? = null) {
        val habit = Habit(
            id = 0,
            name = name,
            description = description,
            createdAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            reminderEnabled = reminderEnabled,
            reminderTimes = reminderTimes,
            reminderDays = reminderDays
        )
        viewModelScope.launch { 
            addHabitUseCase(habit)
            // Устанавливаем напоминание после сохранения привычки
            context?.let { ctx ->
                ReminderService.createNotificationChannel(ctx)
                if (reminderEnabled) {
                    ReminderService.scheduleReminder(ctx, habit)
                }
            }
        }
    }

    fun updateHabit(habit: Habit, context: Context? = null) {
        viewModelScope.launch { 
            updateHabitUseCase(habit)
            // Обновляем напоминание
            context?.let { ctx ->
                ReminderService.cancelReminder(ctx, habit)
                if (habit.reminderEnabled) {
                    ReminderService.scheduleReminder(ctx, habit)
                }
            }
        }
    }

    fun deleteHabit(habitId: Int, context: Context? = null) {
        viewModelScope.launch { 
            val habit = habits.value.find { it.id == habitId }
            habit?.let { 
                deleteHabitUseCase(it)
                // Отменяем напоминание
                context?.let { ctx ->
                    ReminderService.cancelReminder(ctx, it)
                }
            }
        }
    }

    fun getLogsForHabit(habitId: Int): StateFlow<List<HabitLog>> =
        getLogsForHabitUseCase(habitId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // suspend-функция — асинхронная операция
    fun addLog(log: HabitLog) {
        viewModelScope.launch { 
            addHabitLogUseCase(log)
            recalculateStats()
        }
    }

    fun deleteLog(log: HabitLog) {
        viewModelScope.launch { 
            deleteHabitLogUseCase(log)
            recalculateStats()
        }
    }

    private fun recalculateStats() {
        viewModelScope.launch {
            val habitsList = getAllHabitsUseCase().firstOrNull() ?: emptyList()
            val statsList = habitsList.map { habit ->
                val logs = getLogsForHabitUseCase(habit.id).firstOrNull() ?: emptyList()
                HabitStatsCardData(habit, calculateHabitStats(habit, logs))
            }
            _habitStats.value = statsList
        }
    }

    fun getHabitsWithReminders(): StateFlow<List<Habit>> =
        getHabitsWithRemindersUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHabitsWithRemindersList(): Flow<List<Habit>> =
        getHabitsWithRemindersUseCase()

    fun addHabitAsyncWithBuilder(
        name: String,
        description: String = "",
        reminderEnabled: Boolean = false,
        reminderTimes: String = "",
        reminderDays: String = "",
        context: Context? = null,
        reminderSoundUri: String = ""
    ) {
        android.util.Log.d("HabitViewModel", "addHabitAsyncWithBuilder вызван: name=$name, reminderEnabled=$reminderEnabled, reminderTimes=$reminderTimes")
        
        val habit = HabitBuilder()
            .setName(name)
            .setDescription(description)
            .setCreatedAt(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            .setReminderEnabled(reminderEnabled)
            .setReminderTimes(reminderTimes)
            .setReminderDays(reminderDays)
            .setReminderSoundUri(reminderSoundUri)
            .build()
        
        viewModelScope.launch {
            try {
                // Сначала сохраняем привычку в базу данных
                android.util.Log.d("HabitViewModel", "Сохраняем привычку в базу данных: ${habit.name}")
                addHabitUseCase(habit)
                
                // Ждем немного, чтобы база данных обновилась
                kotlinx.coroutines.delay(200)
                
                // Получаем все привычки и находим последнюю добавленную с таким именем
                val allHabits = getAllHabitsUseCase().firstOrNull() ?: emptyList()
                val savedHabit = allHabits.findLast { it.name == name }
                
                android.util.Log.d("HabitViewModel", "Найдена сохраненная привычка: ${savedHabit?.name} (ID: ${savedHabit?.id})")
                
                context?.let { ctx ->
                    if (savedHabit != null) {
                        android.util.Log.d("HabitViewModel", "Создаем канал и планируем уведомления для привычки: ${savedHabit.name}")
                        ReminderService.createNotificationChannel(ctx)
                        if (reminderEnabled && reminderTimes.isNotBlank()) {
                            android.util.Log.d("HabitViewModel", "Планируем уведомления для привычки: ${savedHabit.name}")
                            ReminderService.scheduleReminder(ctx, savedHabit)
                        } else {
                            android.util.Log.d("HabitViewModel", "Уведомления отключены или время не задано для привычки: ${savedHabit.name}")
                        }
                    } else {
                        android.util.Log.w("HabitViewModel", "Не удалось найти сохраненную привычку, используем исходную")
                        // Если не удалось найти сохраненную привычку, используем исходную
                        ReminderService.createNotificationChannel(ctx)
                        if (reminderEnabled && reminderTimes.isNotBlank()) {
                            ReminderService.scheduleReminder(ctx, habit)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HabitViewModel", "Ошибка при добавлении привычки", e)
            }
        }
    }

    // Пример асинхронной параллельной загрузки данных и цитаты
    fun loadHabitsAndQuoteParallel(quoteRepository: com.habittracker.domain.repository.QuoteRepository) {
        viewModelScope.launch {
            try {
                _isLoading.value = true // Асинхронная загрузка
                val habitsDeferred = async { getAllHabitsUseCase().firstOrNull() ?: emptyList() }
                val quoteDeferred = async { quoteRepository.getRandomQuote() }
                val habits = habitsDeferred.await()
                val quote = quoteDeferred.await()
                // Здесь можно обновить UI или StateFlow с цитатой
                // Например: _quote.value = quote
            } catch (e: Exception) {
                // Обработка ошибок асинхронно
                // _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class HabitViewModelFactory(
    private val getAllHabitsUseCase: GetAllHabitsUseCase,
    private val addHabitUseCase: AddHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitsWithRemindersUseCase: GetHabitsWithRemindersUseCase,
    private val getLogsForHabitUseCase: GetLogsForHabitUseCase,
    private val addHabitLogUseCase: AddHabitLogUseCase,
    private val updateHabitLogUseCase: UpdateHabitLogUseCase,
    private val deleteHabitLogUseCase: DeleteHabitLogUseCase,
    private val getLogByDateUseCase: GetLogByDateUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(getAllHabitsUseCase, addHabitUseCase, updateHabitUseCase, deleteHabitUseCase, getHabitsWithRemindersUseCase, getLogsForHabitUseCase, addHabitLogUseCase, updateHabitLogUseCase, deleteHabitLogUseCase, getLogByDateUseCase, getHabitByIdUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 