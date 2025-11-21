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

    // Theme
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

    // InputMode
    enum class InputMode {
        KEYBOARD,
        BUTTONS
        // Add more later, like SWIPE, VOICE, etc.
    }
    private val keyInputMode = "input_mode"
    var inputMode: InputMode
        get() {
            val stored = preference.getString(keyInputMode, InputMode.BUTTONS.name)
            return try {
                InputMode.valueOf(stored ?: InputMode.BUTTONS.name)
            } catch (e: IllegalArgumentException) {
                InputMode.BUTTONS
            }
        }
        set(value) {
            editor.putString(keyInputMode, value.name).apply()
        }

    /*
    This gives you:
    prefs.timerMode with values: MIN_5, MIN_10, PER_WORD_10S, NONE
    A shared constant SECONDS_PER_WORD = 10
     */
    // -------- Timer --------
    enum class TimerMode {
        MIN_5,
        MIN_10,
        PER_WORD_10S,
        NONE
    }

    private val keyTimerMode = "timer_mode"
    var timerMode: TimerMode
        get() {
            val stored = preference.getString(keyTimerMode, TimerMode.MIN_10.name)
            return try {
                TimerMode.valueOf(stored ?: TimerMode.MIN_10.name)
            } catch (e: IllegalArgumentException) {
                TimerMode.MIN_10
            }
        }
        set(value) {
            editor.putString(keyTimerMode, value.name).apply()
        }

    companion object {
        // for "10 seconds per word" option
        const val SECONDS_PER_WORD: Int = 6
    }

}
