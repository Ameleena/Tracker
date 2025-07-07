package com.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.habittracker.data.db.HabitDatabase
import com.habittracker.domain.model.Habit
import com.habittracker.domain.usecase.AddHabitUseCase
import com.habittracker.domain.usecase.GetAllHabitsUseCase
import com.habittracker.ui.viewmodels.HabitViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntegrationHabitFlowTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: HabitDatabase
    private lateinit var viewModel: HabitViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = HabitDatabase.getDatabase(context)
        val addHabitUseCase = AddHabitUseCase(db.habitDao())
        val getAllHabitsUseCase = GetAllHabitsUseCase(db.habitDao())
        viewModel = HabitViewModel(
            getAllHabitsUseCase,
            addHabitUseCase,
            get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAddHabitAndObserveFlow() = runTest {
        val habit = Habit(name = "Интеграционный тест")
        viewModel.addHabit(habit.name, habit.description)
        val habits = viewModel.habits.first()
        assert(habits.any { it.name == "Интеграционный тест" })
    }
} 