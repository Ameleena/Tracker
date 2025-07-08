package com.habittracker.di

import android.app.Application
import androidx.room.Room
import com.habittracker.data.api.QuoteApi
import com.habittracker.data.db.HabitDao
import com.habittracker.data.db.HabitDatabase
import com.habittracker.data.db.HabitLogDao
import com.habittracker.data.db.HabitLogRepository
import com.habittracker.data.db.HabitRepository
import com.habittracker.data.local.PreferencesManager
import com.habittracker.data.repository.QuoteRepositoryImpl
import com.habittracker.domain.repository.QuoteRepository
import com.habittracker.domain.repository.HabitLogRepository as DomainHabitLogRepository
import com.habittracker.domain.repository.HabitRepository as DomainHabitRepository
import com.habittracker.domain.usecase.*
import com.habittracker.ui.viewmodels.HabitViewModel
import com.habittracker.ui.viewmodels.QuoteViewModel
import com.habittracker.ui.viewmodels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

val appModule = module {
    
    // Preferences
    single { PreferencesManager(androidContext()) }
    
    // Database
    single {
        HabitDatabase.getDatabase(androidContext())
    }
    
    single<HabitDao> { get<HabitDatabase>().habitDao() }
    single<HabitLogDao> { get<HabitDatabase>().habitLogDao() }
    
    // OkHttpClient
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    
    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl("https://api.quotable.io/")
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single<QuoteApi> { get<Retrofit>().create(QuoteApi::class.java) }
    
    // Repositories
    single<DomainHabitRepository> { HabitRepository(get()) }
    single<DomainHabitLogRepository> { HabitLogRepository(get()) }
    single<QuoteRepository> { QuoteRepositoryImpl(get(), get()) }
    
    // Use Cases
    single { GetAllHabitsUseCase(get()) }
    single { AddHabitUseCase(get()) }
    single { UpdateHabitUseCase(get()) }
    single { DeleteHabitUseCase(get()) }
    single { GetHabitByIdUseCase(get()) }
    single { GetHabitsWithRemindersUseCase(get()) }
    single { GetLogsForHabitUseCase(get()) }
    single { AddHabitLogUseCase(get()) }
    single { UpdateHabitLogUseCase(get()) }
    single { DeleteHabitLogUseCase(get()) }
    single { GetLogByDateUseCase(get()) }
    single { GetRandomQuoteUseCase(get()) }
    
    // ViewModels
    viewModel { HabitViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { QuoteViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

// Пример тестового DI-модуля для unit-тестов
// val testAppModule = module {
//     single<QuoteRepository> { FakeQuoteRepository() } // подмена на фейковую реализацию
// } 