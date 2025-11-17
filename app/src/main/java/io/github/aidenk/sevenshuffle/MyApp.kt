package io.github.aidenk.sevenshuffle

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {

    lateinit var dataManager: DataManager

    override fun onCreate() {
        super.onCreate()

        dataManager = DataManager(applicationContext)

        val sharedPreferenceManger = SharedPreferenceManger(this)

        // Apply the saved theme when the app starts
        val flags = sharedPreferenceManger.themeFlag
        val idx = sharedPreferenceManger.theme
            .takeIf { it in flags.indices } ?: 2  // 2 = FOLLOW_SYSTEM
        AppCompatDelegate.setDefaultNightMode(flags[idx])
    }
}