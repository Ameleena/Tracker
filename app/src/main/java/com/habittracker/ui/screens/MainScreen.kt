package com.habittracker.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habittracker.ui.viewmodels.HabitViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.automirrored.filled.List

@Composable
fun MainScreen(
    habitViewModel: HabitViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "habits",
                    onClick = { navController.navigate("habits") },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) },
                    label = { Text("Привычки") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "motivation",
                    onClick = { navController.navigate("motivation") },
                    icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Мотивация") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "stats",
                    onClick = { navController.navigate("stats") },
                    icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Статистика") }
                )
                NavigationBarItem(
                    selected = navController.currentDestination?.route == "settings",
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Настройки") }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "habits",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("habits") {
                HabitListScreen(
                    habitViewModel = habitViewModel,
                    onNavigateToLog = { habitId ->
                        navController.navigate("log/$habitId")
                    },
                    onNavigateToStats = {
                        navController.navigate("stats")
                    },
                    onNavigateToAdd = {
                        navController.navigate("add")
                    },
                    onNavigateToEdit = { habit ->
                        navController.navigate("edit/${habit.id}")
                    }
                )
            }
            
            composable("motivation") {
                MotivationalQuoteScreen(viewModel = koinViewModel())
            }
            
            composable("log/{habitId}") { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
                HabitLogScreen(
                    habitId = habitId,
                    habitViewModel = habitViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("edit/{habitId}") { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
                val habits by habitViewModel.habits.collectAsState()
                val habit = habits.find { it.id == habitId }
                
                habit?.let { foundHabit ->
                    EditHabitScreen(
                        habit = foundHabit,
                        viewModel = habitViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            
            composable("stats") {
                HabitStatScreen(
                    habitViewModel = habitViewModel
                )
            }
            
            composable("add") {
                AddHabitScreen(
                    habitViewModel = habitViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
} 