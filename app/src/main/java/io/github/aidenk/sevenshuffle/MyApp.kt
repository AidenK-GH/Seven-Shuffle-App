package io.github.aidenk.sevenshuffle

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        //CrashLogger.init(this) //uses the CrashLogger file

        val sharedPreferenceManger = SharedPreferenceManger(this)

        // Apply the saved theme when the app starts
        val flags = sharedPreferenceManger.themeFlag
        val idx = sharedPreferenceManger.theme
            .takeIf { it in flags.indices } ?: 2  // 2 = FOLLOW_SYSTEM
        AppCompatDelegate.setDefaultNightMode(flags[idx])
    }
}