package io.github.aidenk.sevenshuffle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.DigitsKeyListener
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.BufferedReader
import android.content.res.Configuration

class MainMenuActivity : AppCompatActivity() {

    // UI
    private lateinit var newGameBtn: Button
    private lateinit var gameHistoryBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var helpBtn: Button

    // Managers
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        dataManager = (application as MyApp).dataManager

        newGameBtn = findViewById(R.id.new_game_btn)
        gameHistoryBtn = findViewById(R.id.game_history_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        helpBtn = findViewById(R.id.help_btn)

        newGameBtn.setOnClickListener { moveToGameActivity() }
        gameHistoryBtn.setOnClickListener { showPastGamesDialog() }
        settingsBtn.setOnClickListener { showSettingsDialog() }
        helpBtn.setOnClickListener { showHelpDialog() }
    }

    private fun moveToGameActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        //finish() // remove this if you want Back to return to menu
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.setting_window, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }

        // --- Theme spinner --------------------
        val themeSpinner = dialogView.findViewById<Spinner>(R.id.themeSpinner)
        val sharedPreferenceManger = SharedPreferenceManger(this)
        val checkedTheme = sharedPreferenceManger.theme
        val themeTitleList = resources.getStringArray(R.array.theme_titles)

        // Set up adapter for spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            themeTitleList
        )
        themeSpinner.adapter = adapter

        // Set spinner to saved value
        themeSpinner.setSelection(checkedTheme)

        // Listen for user changes
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != sharedPreferenceManger.theme) {
                    sharedPreferenceManger.theme = position
                    AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[position])
                    dialog.dismiss()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- InputMode spinner --------------------
        val inputModeSpinner = dialogView.findViewById<Spinner>(R.id.input_mode_Spinner)
        val inputModeTitles = resources.getStringArray(R.array.input_mode_list)
        // Map spinner positions to enum values:
        val inputModeValues = arrayOf(SharedPreferenceManger.InputMode.BUTTONS, SharedPreferenceManger.InputMode.KEYBOARD)
        // 0 -> Buttons, 1 -> Keyboard (match your string array order)

        val inputModeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            inputModeTitles
        )
        inputModeSpinner.adapter = inputModeAdapter

        // Set spinner to current saved input mode
        val currentMode = sharedPreferenceManger.inputMode
        val currentIndex = inputModeValues.indexOf(currentMode).coerceAtLeast(0)
        inputModeSpinner.setSelection(currentIndex)

        inputModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newMode = inputModeValues[position]
                if (newMode != sharedPreferenceManger.inputMode) {
                    sharedPreferenceManger.inputMode = newMode
                    // No need for AppCompatDelegate here
                    //dialog.dismiss()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- Timer spinner --------------------
        val timerSpinner = dialogView.findViewById<Spinner>(R.id.timer_options_spinner)
        val timerTitles = resources.getStringArray(R.array.timer_options_list)

        // Order must match arrays.xml:
        val timerValues = arrayOf(
            SharedPreferenceManger.TimerMode.MIN_5,
            SharedPreferenceManger.TimerMode.MIN_10,
            SharedPreferenceManger.TimerMode.PER_WORD_10S,
            SharedPreferenceManger.TimerMode.NONE
        )

        val timerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            timerTitles
        )
        timerSpinner.adapter = timerAdapter

        // Set spinner to current saved timer mode
        val currentTimerMode = sharedPreferenceManger.timerMode
        val currentTimerIndex = timerValues.indexOf(currentTimerMode).coerceAtLeast(0)
        timerSpinner.setSelection(currentTimerIndex)

        timerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newMode = timerValues[position]
                if (newMode != sharedPreferenceManger.timerMode) {
                    sharedPreferenceManger.timerMode = newMode
                    // No need for AppCompatDelegate here
                    //dialog.dismiss()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        dialog.show()
    }

    private fun showPastGamesDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.past_games_board, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // "Loading..." TextView
        val loadingTextView = dialogView.findViewById<TextView>(R.id.loading_TextView)
        // hide list initially
        loadingTextView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // Trigger async load
        dataManager.getPastGamesAsync { gamesList ->
            // This callback runs on the MAIN thread
            if (gamesList.isNotEmpty()) {
                loadingTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = PastGamesAdapter(gamesList.asReversed())
            } else {
                //recyclerView.visibility = View.GONE
                loadingTextView.text = "No past games yet. Play one and check back!"
            }
        }

        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showHelpDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.help_info, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }

        val linkTextView = dialogView.findViewById<TextView?>(R.id.github_textview)
        linkTextView?.apply {
            text = "https://github.com/AidenK-GH/Seven-Shuffle-App/tree/master"
            linksClickable = true
            movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }

        dialog.show()
    }
}


