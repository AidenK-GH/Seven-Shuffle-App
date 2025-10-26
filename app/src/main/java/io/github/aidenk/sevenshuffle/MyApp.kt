package io.github.aidenk.sevenshuffle

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferenceManger = SharedPreferenceManger(this)

        // Apply the saved theme when the app starts
        AppCompatDelegate.setDefaultNightMode(
            sharedPreferenceManger.themeFlag[sharedPreferenceManger.theme]
        )
    }
}