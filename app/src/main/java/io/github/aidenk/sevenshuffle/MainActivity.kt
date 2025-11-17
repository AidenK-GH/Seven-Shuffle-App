package io.github.aidenk.sevenshuffle

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.DigitsKeyListener
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.BufferedReader
import kotlin.random.Random
import android.content.res.Configuration
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import io.github.aidenk.sevenshuffle.SharedPreferenceManger.InputMode

class MainActivity : AppCompatActivity() {

    // File name for the word list in the assets folder
    private val fileNameWordList = "ag_list.txt"
    private val fileNameJSONAllWordMap = "all_word_map.json"

    //private val themeTitleList = arrayOf("Light", "Dark", "System Default")

    // flags
    private var isThereAGameCurrentlyRunning = false

    private lateinit var prefs: SharedPreferenceManger
    private var currentInputMode: InputMode = InputMode.BUTTONS
    private var currentTimerMode: SharedPreferenceManger.TimerMode =
        SharedPreferenceManger.TimerMode.MIN_10

    // Data structure to store words categorized by their length
    private val wordListsByLength: MutableMap<Int, MutableList<WordEntry>> = mutableMapOf() // keeps the words for current game
    private lateinit var sevenLetterWord: String // 7 letter word of the current game
    private lateinit var allWordsMap: MutableMap<String, List<String>>
    // ^ map that keeps the Library of all the words. key is the letters that used to make words in value:list.
    private lateinit var listSevenLetterWords: List<String> // list of all 7 letter words. faster than reading asset file everytime.

    private val userInputSequence = mutableListOf<Pair<Button, Char>>()
    private lateinit var letterButtons: List<Button>
    private lateinit var pbLenTextViewList: List<TextView>

    // UI elements
    private lateinit var userInputButtonsTextView: TextView
    private lateinit var lettersTextView: TextView
    private lateinit var answersTempTextView: TextView
    private lateinit var newGameBtn: Button
    private lateinit var solveEndGameBtn: Button
    private lateinit var shuffleBtn: Button
    private lateinit var cleanBtn: Button
    private lateinit var checkBtn: Button
    private lateinit var backBtn: Button
    //private lateinit var helpBtn: Button
    //private lateinit var gameHistoryBtn: Button
    //private lateinit var settingsBtn: Button
    private lateinit var userInputEditText: EditText
    //private lateinit var checkInputTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var inputLayout2Layout: LinearLayout
    //private lateinit var progressBarTextview: TextView

    // Timer setup for a 5-minute countdown
    //private lateinit var gameTimer: CountDownTimer //old timer
    //private val gameDurationInMillis: Long = 10 * 60 * 1000 //for old timer
    // Timer
    private var gameTimer: CountDownTimer? = null

    // animation fade color speed
    private val fadeColorSpeedMS: Long = 1000

    // Managers
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Managers
        dataManager = (application as MyApp).dataManager
        prefs = SharedPreferenceManger(this)

        // Load saved input mode
        currentInputMode = prefs.inputMode
        currentTimerMode = prefs.timerMode

        // Initialize data structure
        allWordsMap = MapManager.allWordsMap
        listSevenLetterWords = MapManager.listSevenLetterWords

        // Initialize UI elements
        lettersTextView = findViewById(R.id.letters_textview)
        answersTempTextView = findViewById(R.id.answers_temp_textview)
        newGameBtn = findViewById(R.id.new_game_btn)
        solveEndGameBtn = findViewById(R.id.solve_end_game_btn)
        shuffleBtn = findViewById(R.id.shuffle_btn)
        cleanBtn = findViewById(R.id.clean_btn)
        checkBtn = findViewById(R.id.check_btn)
        backBtn = findViewById(R.id.back_to_menu_button)
        //helpBtn = findViewById(R.id.help_btn)
        //gameHistoryBtn = findViewById(R.id.game_history_btn)
        //settingsBtn = findViewById(R.id.settings_btn)
        userInputEditText = findViewById(R.id.userInput_EditText)
        //checkInputTextView = findViewById(R.id.checkInput_textview)
        timerTextView = findViewById(R.id.timer_textview)
        inputLayout2Layout = findViewById(R.id.input_type_2_layout)

        //progressBarTextview = findViewById(R.id.progressBar_textview)
        pbLenTextViewList = listOf(
            findViewById(R.id.pb_len3),
            findViewById(R.id.pb_len4),
            findViewById(R.id.pb_len5),
            findViewById(R.id.pb_len6),
            findViewById(R.id.pb_len7)
        )

        userInputButtonsTextView = findViewById(R.id.userInput_Buttons_textview)
        letterButtons = listOf(
            findViewById(R.id.input_1_btn),
            findViewById(R.id.input_2_btn),
            findViewById(R.id.input_3_btn),
            findViewById(R.id.input_4_btn),
            findViewById(R.id.input_5_btn),
            findViewById(R.id.input_6_btn),
            findViewById(R.id.input_7_btn)
        )

        timerTextView.text = "--:--"
        //setupTimer()
        updateInputModeUI()
        disableAllControlsExceptNewGame()

        // Set up button actions
        newGameBtn.setOnClickListener {
            if (!isThereAGameCurrentlyRunning) {
                startNewGame()
            } else {
                showNewGameConfirmationDialog()
            }
        }

        /*
        helpBtn.setOnClickListener {
            showHelpDialog()
        }

        gameHistoryBtn.setOnClickListener {
            showPastGamesDialog()
        }

        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }
        */
        shuffleBtn.setOnClickListener {
            if (isThereAGameCurrentlyRunning) {
                lettersTextView.text = shuffleWord(sevenLetterWord)
            }
        }

        cleanBtn.setOnClickListener {
            if (isThereAGameCurrentlyRunning) {
                //cleanUserInput()
                backspaceLastInput()
            }
        }

        solveEndGameBtn.setOnClickListener {
            if (isThereAGameCurrentlyRunning) {
                endGame()
            }
        }

        checkBtn.setOnClickListener {
            if (isThereAGameCurrentlyRunning) {
                checkUserInput()
            }
        }

        backBtn.setOnClickListener {
            if (!isThereAGameCurrentlyRunning) {
                // behave exactly like the system Back button
                onBackPressedDispatcher.onBackPressed()
            } else {
                showGoBackToMenuConfirmationDialog()
            }
        }
    }

//    // onPause onResume
//    override fun onPause() {
//        super.onPause()
//
//        if (isGameRunning) {
//            isPausedBySystem = true
//            pauseGame()   // stops the CountDownTimer but keeps timeLeftMs
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        if (isGameRunning && isPausedBySystem) {
//            resumeGameIfNeeded()
//            isPausedBySystem = false
//        }
//    }
    // Sets up a new timer instance according to preferences and game size
    private fun setupTimer(totalWordsThisGame: Int) {
        gameTimer?.cancel()

        currentTimerMode = prefs.timerMode  // refresh in case user changed settings

        val durationMillis: Long = when (currentTimerMode) {
            SharedPreferenceManger.TimerMode.MIN_5 -> 5L * 60L * 1000L
            SharedPreferenceManger.TimerMode.MIN_10 -> 10L * 60L * 1000L
            SharedPreferenceManger.TimerMode.PER_WORD_10S -> {
                val seconds = totalWordsThisGame * SharedPreferenceManger.SECONDS_PER_WORD
                (seconds.coerceAtLeast(30)) * 1000L  // optional: minimum 30 seconds
            }
            SharedPreferenceManger.TimerMode.NONE -> 0L
        }

        // If timer disabled
        if (currentTimerMode == SharedPreferenceManger.TimerMode.NONE || durationMillis <= 0L) {
            timerTextView.text = "∞"
            gameTimer = null
            return
        }

        gameTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                endGame()
            }
        }
    }

    /*
    private fun setupTimer() {
        if (::gameTimer.isInitialized) gameTimer.cancel()
        gameTimer = object : CountDownTimer(gameDurationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                endGame()
            }
        }
    }*/

    // Starts a new game by resetting game data and setting up word lists
    private fun startNewGame() {
        isThereAGameCurrentlyRunning = true
        sevenLetterWord = selectRandomlyASevenLetterWordFromList()
        organizeWordsListsFromMapIntoWordListsByLength()

        //setupTimer()
        //gameTimer.start()
        // total words available for this game
        val totalWords = wordListsByLength.values.sumOf { it.size }
        setupTimer(totalWords)
        gameTimer?.start()

        // Shuffle and display the selected seven-letter word
        lettersTextView.text = shuffleWord(sevenLetterWord)
        //updateInputFilter() // not good, shows a number-keyboard at the start
        updateAnswersTempTextView()

        userInputSequence.clear()
        enableGameControls()
        updateUserInputFieldFromButtons()
        updateInputModeUI()

        val dumySevenLetterWord = sevenLetterWord.toList().shuffled().joinToString("")

        letterButtons.forEachIndexed { index, button ->
            val letter = dumySevenLetterWord[index]
            button.text = letter.toString()
            button.isSelected = false
            tintInputButton(button)

            button.setOnClickListener {
                val existingIndex = userInputSequence.indexOfFirst { it.first == button }

                if (existingIndex == -1) {
                    userInputSequence.add(button to letter)
                    button.isSelected = true
                    tintInputButton(button)
                }
                // this code would allow user if a button is selected, clicking on it again makes it remove from the input
                // dad didn't like it, so removed
//                else {
//                    userInputSequence.removeAt(existingIndex)
//                    button.isSelected = false
//                    tintInputButton(button)
//                }

                updateUserInputFieldFromButtons()
            }
        }
    }

    private fun updateInputModeUI() {
        when (currentInputMode) {
            InputMode.KEYBOARD -> {
                userInputEditText.isEnabled = true
                userInputEditText.visibility = View.VISIBLE
                userInputButtonsTextView.visibility = View.GONE
                inputLayout2Layout.visibility = View.GONE
            }
            InputMode.BUTTONS -> {
                userInputEditText.isEnabled = false
                userInputEditText.visibility = View.GONE
                userInputButtonsTextView.visibility = View.VISIBLE
                inputLayout2Layout.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUserInputFieldFromButtons() {
        val currentWord = userInputSequence.map { it.second }.joinToString("")
        userInputButtonsTextView.text = currentWord
    }

    // Updates input filter to allow only characters from the seven-letter word
    private fun updateInputFilter() {
        val allowedCharacters = sevenLetterWord.toCharArray().toSet().joinToString("")
        userInputEditText.keyListener = DigitsKeyListener.getInstance(allowedCharacters)
    }

    // Looks at which input type we are (buttons or keyboard) and collects the input accordingly
    private fun collectUserInput(): String {
        return when (currentInputMode) {
            InputMode.KEYBOARD -> {
                userInputEditText.text.toString().trim()
            }
            InputMode.BUTTONS -> {
                userInputSequence.map { it.second }.joinToString("")
            }
        }
    }

    // Checks user input against words in wordListsByLength and updates the UI if guessed correctly
    private fun checkUserInput() {
        val userInput: String = when (currentInputMode) {
            InputMode.KEYBOARD -> {
                userInputEditText.text.toString().trim()
            }

            InputMode.BUTTONS -> {
                collectUserInput()
            }
        }  //by default gets buttons input

        val wordLength = userInput.length
        val entries = wordListsByLength[wordLength]
        val wordEntry = entries?.find { it.word == userInput && !it.isGuessed }

        if (wordEntry != null) {
            wordEntry.isGuessed = true
            //checkInputTextView.text = "CORRECT"
            flashErrorBackground(true)
            updateAnswersTempTextView()

            // Check if user has won after guessing correctly
            if (hasUserWon()) {
                endGame()
                //checkInputTextView.text = "You WON"
            }
        } else {
            //checkInputTextView.text = "WRONG"
            flashErrorBackground(false)
        }

        cleanUserInput()
    }

    private fun flashErrorBackground(isPlayRight: Boolean) {
        val textViewButtons = findViewById<TextView>(R.id.userInput_Buttons_textview)
        val editTextKeyboard = findViewById<EditText>(R.id.userInput_EditText)

        // Start color and end color (normal background color)
        val startColor = ContextCompat.getColor(
            this,
            if (isPlayRight) android.R.color.holo_green_light else android.R.color.holo_red_light
        )
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val endColor = //ContextCompat.getColor(this, R.color.Accent_Garden) // define in colors.xml same as your drawable
            if (isDark) {
                // DARK THEME
                ContextCompat.getColor(this, R.color.Primary_Text_Garden)//R.color.Primary_Text_Garden
            } else {
                // LIGHT / DEFAULT THEME
                ContextCompat.getColor(this, R.color.Accent_Garden)//R.color.Accent_Garden
            }

        // Animate color change
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnimation.duration = fadeColorSpeedMS // ms (adjust speed)
        colorAnimation.addUpdateListener { animator ->
            editTextKeyboard.setBackgroundColor(animator.animatedValue as Int)
            textViewButtons.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun cleanUserInput() {
        when (currentInputMode) {
            InputMode.BUTTONS -> {
                if (userInputSequence.isNotEmpty()) {
                    //buttons
                    userInputButtonsTextView.text = ""
                    userInputSequence.clear()
                    resetButtons()
                }
            }

            InputMode.KEYBOARD -> {
                val text = userInputEditText.text
                if (text.isNotEmpty()) {
                    //keyboard
                    userInputEditText.text.clear()
                }
            }
        }
    }

    private fun backspaceLastInput() {
        when (currentInputMode) {
            InputMode.BUTTONS -> {
                if (userInputSequence.isNotEmpty()) {
                    // Remove last selected (Button, Char)
                    val (btn, _) = userInputSequence.removeAt(userInputSequence.size - 1)

                    // Reset just this button’s UI state
                    btn.isSelected = false
                    tintInputButton(btn)

                    // Reflect the removal in the user-input preview
                    updateUserInputFieldFromButtons()
                }
            }

            InputMode.KEYBOARD -> {
                val text = userInputEditText.text
                if (text.isNotEmpty()) {
                    // Remove last character
                    text.delete(text.length - 1, text.length)
                }
            }
        }
    }

    // Call this whenever a button's selection changes or after a theme change
    private fun tintInputButton(button: Button) {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val colorRes =
            if (isDark) {
                // DARK THEME
                if (button.isSelected) R.color.Highlight_Selection_Garden else R.color.Accent_Garden
            } else {
                // LIGHT / DEFAULT THEME
                if (button.isSelected) R.color.Highlight_Selection_Garden else R.color.Buttons_Garden
            }

        button.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
    }

    private val SUPPORTED_LENGTHS = listOf(3, 4, 5, 6, 7)
    private fun updateAnswersTempTextView() {
        val spannableBuilder = SpannableStringBuilder()

        SUPPORTED_LENGTHS.forEachIndexed { index, length ->
            val words = wordListsByLength[length] ?: emptyList()
            val totalWords = words.size
            val guessedWordsCount = words.count { it.isGuessed }

            // Always update the correct TextView for this length
            pbLenTextViewList.getOrNull(index)?.text =
                "$length($guessedWordsCount/$totalWords)"

            // If you want to *skip* groups that have no words, you can bail out here:
            if (totalWords == 0) {
                // No words of this length – don't add to the big text block
                return@forEachIndexed
            }

            // ===== answersTempTextView content =====
            // Header
            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")

            // Words
            words.forEachIndexed { i, wordEntry ->
                val wordText = if (wordEntry.isGuessed)
                    wordEntry.word
                else
                    "_ ".repeat(wordEntry.word.length).trim()

                val start = spannableBuilder.length
                spannableBuilder.append(wordText)

                val color = ContextCompat.getColor(
                    answersTempTextView.context,
                    if (wordEntry.isGuessed) R.color.default_button_color
                    else R.color.default_word_color
                )

                spannableBuilder.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    spannableBuilder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                if (i < words.size - 1) {
                    spannableBuilder.append(", ")
                }
            }

            spannableBuilder.append("\n\n")
        }

        answersTempTextView.text = spannableBuilder
    }

    // Ends the game and displays all words categorized by length, with guessed words in color
    private fun endGame() {
        isThereAGameCurrentlyRunning = false
        gameTimer?.cancel()

        resetButtons()
        disableAllControlsExceptNewGame()

        val spannableBuilder = SpannableStringBuilder()
        wordListsByLength.keys.sorted().forEach { length ->
            val words = wordListsByLength[length] ?: emptyList()
            val totalWords = words.size
            val guessedWordsCount = words.count { it.isGuessed }

            // Add header with count
            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")

            // Add all words with guessed words in color and guessed in default color
            words.forEachIndexed { index, wordEntry ->
                val start = spannableBuilder.length
                spannableBuilder.append(wordEntry.word)

                // Set color only for guessed words
                if (wordEntry.isGuessed) {
                    val color = ContextCompat.getColor(answersTempTextView.context, R.color.default_button_color)
                    spannableBuilder.setSpan(
                        ForegroundColorSpan(color),
                        start,
                        spannableBuilder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                // Add a comma and space after each word, except the last one
                if (index < words.size - 1) {
                    spannableBuilder.append(", ")
                }
            }

            // Add a new line between length groups
            spannableBuilder.append("\n\n")
        }
        answersTempTextView.text = spannableBuilder

        // Save the game to the JSON file
        saveGameToHistory()
    }

    private fun resetButtons() {
        letterButtons.forEach {
            it.isSelected = false
            tintInputButton(it)
        }
    }

    private fun saveGameToHistory() {
//        val currentGame = mapOf(
//            "date" to getCurrentDate(),
//            "sevenLetterWord" to sevenLetterWord,
//            "howManyTotalWordsCouldGet" to wordListsByLength.values.sumOf { it.size },
//            "howManyTotalWordsGot" to wordListsByLength.values.sumOf { it.count { w -> w.isGuessed } }
//        )
//        dataManager.addGame(currentGame)

        lifecycleScope.launch {
            dataManager.addGame(
                mapOf(
                    "date" to getCurrentDate(),
                    "sevenLetterWord" to sevenLetterWord,
                    "howManyTotalWordsCouldGet" to wordListsByLength.values.sumOf { it.size },
                    "howManyTotalWordsGot" to wordListsByLength.values.sumOf { it.count { w -> w.isGuessed } }
                )
            )
        }
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }



    //------ Dialog ---------------------------------------------------------------------
    // Shows a dialog to confirm if the user wants to start a new game
    private fun showNewGameConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.ask_new_game, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.no_button).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.new_game_btn).setOnClickListener {
            startNewGame()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showGoBackToMenuConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.ask_go_back_to_menu_dialog, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.no_button).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.yes_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dialog.show()
    }

    //------ words -------------------------------------------------------------------
    //------ from map
    /**
     * Returns all 2^n subsets of the given 7-letter word.
     * - Each subset's letters are sorted alphabetically (e.g., "cba" -> "abc").
     * - The list includes the empty string "" (for mask == 0), so you'll get 128 items for 7 letters.
     * - If the word has repeated letters, some subset strings may be identical (kept as-is).
     */
    private fun allSortedSubsetsFast(): List<String> {
        require(sevenLetterWord.length == 7) { "Word must be exactly 7 letters." }

        // Sort the input once so any left-to-right selection yields a sorted subset
        val sorted = sevenLetterWord.toCharArray().apply { sort() }
        val subsetsList = ArrayList<String>(1 shl 7) // 128
        val sb = StringBuilder(7)

        for (mask in 0 until (1 shl 7)) { // 0..127
            sb.setLength(0)
            // Append characters in index order -> subset is already sorted
            for (i in 0 until 7) {
                if ((mask and (1 shl i)) != 0) sb.append(sorted[i])
            }
            subsetsList.add(sb.toString())
        }
        // Remove duplicates, keep first occurrence order
        return subsetsList.distinct()
    }

    private fun organizeWordsListsFromMapIntoWordListsByLength(){
        wordListsByLength.clear()
        val subsetKeys = allSortedSubsetsFast()

        for (key in subsetKeys) {
            val words = allWordsMap[key] ?: continue  // skip if no words for this key
            val length = key.length
            for (word in words) {
                wordListsByLength
                    .getOrPut(length) { mutableListOf() }
                    .add(WordEntry(word))
            }
        }

        // Sort each list alphabetically
        for ((_, list) in wordListsByLength) {
            list.sortBy { it.word }
        }
    }

    //------ from txt
    // Randomly selects a 7-letter word from the word list
    private fun selectRandomlyASevenLetterWordFromList(): String {
        return listSevenLetterWords[Random.nextInt(listSevenLetterWords.size)]
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------

    // Checks if the player has won by verifying all words have been guessed
    private fun hasUserWon(): Boolean {
        return wordListsByLength.values.all { list -> list.all { it.isGuessed } }
    }

    // Shuffles the letters in a word and returns the shuffled string
    private fun shuffleWord(word: String): String {
        return " " + word.toList().shuffled().joinToString(" ")
    }

    // -----------------------------------------------------------------
    // Enables all in-game controls (letters + action buttons)
    // and leaves "New Game" enabled as well.
    private fun enableGameControls() {
        // Action buttons used during a game
        val actionButtons = listOf(
            solveEndGameBtn, shuffleBtn, cleanBtn, checkBtn
            //,helpBtn, gameHistoryBtn, settingsBtn
        )

        // Enable action buttons
        actionButtons.forEach { btn ->
            btn.isEnabled = true
            btn.alpha = 1f
        }

        // Enable letter buttons
        letterButtons.forEach { btn ->
            btn.isEnabled = true
            btn.alpha = 1f
        }

        // Input widgets follow your current input mode
        updateInputModeUI()

        // Always keep New Game enabled/visible
        newGameBtn.isEnabled = true
        newGameBtn.alpha = 1f
    }

    // Disables everything except "New Game" (and the Android Back remains as usual)
    private fun disableAllControlsExceptNewGame() {
        val actionButtons = listOf(
            solveEndGameBtn, shuffleBtn, cleanBtn, checkBtn
            //,helpBtn, gameHistoryBtn, settingsBtn
        )

        // Disable action buttons
        actionButtons.forEach { btn ->
            btn.isEnabled = false
            btn.alpha = 0.5f
        }

        // Disable letter buttons
        letterButtons.forEach { btn ->
            btn.isEnabled = false
            btn.alpha = 0.5f
        }

        // Disable text input regardless of mode
        userInputEditText.isEnabled = false
        userInputButtonsTextView.isEnabled = false

        // Keep New Game active
        newGameBtn.isEnabled = true
        newGameBtn.alpha = 1f
    }

}