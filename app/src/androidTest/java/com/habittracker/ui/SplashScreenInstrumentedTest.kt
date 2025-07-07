package com.habittracker.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.habittracker.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenInstrumentedTest {
    @Test
    fun mainScreen_isDisplayedAfterSplash() {
        // Запускаем MainActivity
        ActivityScenario.launch(MainActivity::class.java)

        // Проверяем, что заголовок главного экрана отображается
        onView(withText("Мои привычки"))
            .check(matches(isDisplayed()))
    }
} 