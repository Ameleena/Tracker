package com.habittracker

import android.app.Application
import com.habittracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HabitTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@HabitTrackerApplication)
            modules(appModule)
        }
    }
} 