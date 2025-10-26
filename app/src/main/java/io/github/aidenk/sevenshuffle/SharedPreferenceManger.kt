package io.github.aidenk.sevenshuffle


import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatDelegate

class SharedPreferenceManger(context: Context) {

    private val preference = context.getSharedPreferences(
        context.packageName,
        MODE_PRIVATE
    )
    private val editor = preference.edit()
    private val keyTheme = "theme"

    var theme: Int
        get() = preference.getInt(keyTheme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) {
            editor.putInt(keyTheme, value)
            editor.commit()
        }

    val themeFlag = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,           // Light
        AppCompatDelegate.MODE_NIGHT_YES,          // Dark
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // System default
    )
}
