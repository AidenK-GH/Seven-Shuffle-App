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
import java.io.File
import kotlin.random.Random
import android.content.res.Configuration

class MainActivity : AppCompatActivity() {

    // File name for the word list in the assets folder
    private val fileNameWordList = "ag_list.txt"
    private val fileNameJSONAllWordMap = "all_word_map.json"

    private val themeTitleList = arrayOf("Light", "Dark", "System Default")

    // flags
    private var isThereAGameCurrentlyRunning = false
    enum class InputMode {
        KEYBOARD,
        BUTTONS
        // Add more later, like SWIPE, VOICE, etc.
    }
    private var currentInputMode: InputMode = InputMode.BUTTONS

    // Data structure to store words categorized by their length
    private val wordListsByLength: MutableMap<Int, MutableList<WordEntry>> = mutableMapOf() // keeps the words for current game
    private lateinit var sevenLetterWord: String // 7 letter word of the current game
    private val allWordsMap: MutableMap<String, List<String>> = mutableMapOf()
    // ^ map that keeps the Library of all the words. key is the letters that used to make words in value:list.
    private lateinit var listSevenLetterWords: List<String> // list of all 7 letter words. faster than reading asset file everytime.

    private val userInputSequence = mutableListOf<Pair<Button, Char>>()
    private lateinit var letterButtons: List<Button>

    // UI elements
    private lateinit var userInputButtonsTextView: TextView
    private lateinit var lettersTextView: TextView
    private lateinit var answersTempTextView: TextView
    private lateinit var progressBarTextview: TextView
    private lateinit var newGameBtn: Button
    private lateinit var solveEndGameBtn: Button
    private lateinit var shuffleBtn: Button
    private lateinit var cleanBtn: Button
    private lateinit var checkBtn: Button
    private lateinit var helpBtn: Button
    private lateinit var gameHistoryBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var userInputEditText: EditText
    private lateinit var checkInputTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var inputLayout2Layout: LinearLayout

    // Timer setup for a 5-minute countdown
    private lateinit var gameTimer: CountDownTimer
    private val gameDurationInMillis: Long = 5 * 60 * 1000

    // animation fade color speed
    private val fadeColorSpeedMS: Long = 1000

    // Managers
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Managers
        dataManager = DataManager(this)

        // Initialize data structure
        allWordsMap.putAll(loadAllWordMapFromAssets())
        listSevenLetterWords = readWordsFromAssets().filter { it.length == 7 };

        // Initialize UI elements
        lettersTextView = findViewById(R.id.letters_textview)
        answersTempTextView = findViewById(R.id.answers_temp_textview)
        progressBarTextview = findViewById(R.id.progressBar_textview)
        newGameBtn = findViewById(R.id.new_game_btn)
        solveEndGameBtn = findViewById(R.id.solve_end_game_btn)
        shuffleBtn = findViewById(R.id.shuffle_btn)
        cleanBtn = findViewById(R.id.clean_btn)
        checkBtn = findViewById(R.id.check_btn)
        helpBtn = findViewById(R.id.help_btn)
        gameHistoryBtn = findViewById(R.id.game_history_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        userInputEditText = findViewById(R.id.userInput_EditText)
        checkInputTextView = findViewById(R.id.checkInput_textview)
        timerTextView = findViewById(R.id.timer_textview)
        inputLayout2Layout = findViewById(R.id.input_type_2_layout)

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

        setupTimer()
        updateInputModeUI()

        // Set up button actions
        newGameBtn.setOnClickListener {
            if (!isThereAGameCurrentlyRunning) {
                startNewGame()
            } else {
                showNewGameConfirmationDialog()
            }
        }

        helpBtn.setOnClickListener {
            showHelpDialog()
        }

        gameHistoryBtn.setOnClickListener {
            showPastGamesDialog()
        }

        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }

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
    }

    // Starts a new game by resetting game data and setting up word lists
    private fun startNewGame() {
        isThereAGameCurrentlyRunning = true
        sevenLetterWord = selectRandomlyASevenLetterWordFromList()
        organizeWordsListsFromMapIntoWordListsByLength()

        setupTimer()
        gameTimer.start()

        // Shuffle and display the selected seven-letter word
        lettersTextView.text = shuffleWord(sevenLetterWord)
        updateInputFilter()
        updateAnswersTempTextView()

        userInputSequence.clear()
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
                } else {
                    userInputSequence.removeAt(existingIndex)
                    button.isSelected = false
                    tintInputButton(button)
                }

                updateUserInputFieldFromButtons()
            }
        }
    }

    private fun loadAllWordMapFromAssets(): MutableMap<String, List<String>> {
        // 1. Read file as string
        val jsonText = assets.open(fileNameJSONAllWordMap)
            .bufferedReader()
            .use { it.readText() }

        // 2. Parse with org.json
        val jsonObject = JSONObject(jsonText)

        // 3. Build map
        val map = mutableMapOf<String, List<String>>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val jsonArray = jsonObject.getJSONArray(key)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            map[key] = list
        }
        return map
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
            checkInputTextView.text = "CORRECT"
            flashErrorBackground(true)
            updateAnswersTempTextView()

            // Check if user has won after guessing correctly
            if (hasUserWon()) {
                endGame()
                checkInputTextView.text = "You WON"
            }
        } else {
            checkInputTextView.text = "WRONG"
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

    // Updates the answersTempTextView with the list of all words categorized by length, with colored words
    private fun updateAnswersTempTextView() {
        val spannableBuilder = SpannableStringBuilder()
        var textForProgressBar = ""

        wordListsByLength.keys.sorted().forEach { length ->
            val words = wordListsByLength[length] ?: emptyList()
            val totalWords = words.size
            val guessedWordsCount = words.count { it.isGuessed }

            // Add header with count
            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")
            textForProgressBar += "$length($guessedWordsCount/$totalWords) "

            // Add words with color
            words.forEachIndexed { index, wordEntry ->
                val wordText = if (wordEntry.isGuessed) wordEntry.word else "_ ".repeat(wordEntry.word.length).trim()
                val start = spannableBuilder.length
                spannableBuilder.append(wordText)

                // Set color for guessed words
                val color = ContextCompat.getColor(
                    answersTempTextView.context,
                    if (wordEntry.isGuessed) R.color.guessed_word_color else R.color.default_word_color
                )
                spannableBuilder.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    spannableBuilder.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Add a comma and space after each word, except the last one
                if (index < words.size - 1) {
                    spannableBuilder.append(", ")
                }
            }

            // Add a new line between length groups
            spannableBuilder.append("\n\n")
        }

        progressBarTextview.text = textForProgressBar
        answersTempTextView.text = spannableBuilder
    }

    // Ends the game and displays all words categorized by length, with guessed words in color
    private fun endGame() {
        isThereAGameCurrentlyRunning = false
        gameTimer.cancel()

        resetButtons()

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
                    val color = ContextCompat.getColor(answersTempTextView.context, R.color.guessed_word_color)
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

        // Save the game to the JSON file
        saveGameToHistory()

        answersTempTextView.text = spannableBuilder
        checkInputTextView.text = "Game ended"
    }

    private fun resetButtons() {
        letterButtons.forEach {
            it.isSelected = false
            tintInputButton(it)
        }
    }

    private fun saveGameToHistory() {
        val currentGame = mapOf(
            "date" to getCurrentDate(),
            "sevenLetterWord" to sevenLetterWord,
            "howManyTotalWordsCouldGet" to wordListsByLength.values.sumOf { it.size },
            "howManyTotalWordsGot" to wordListsByLength.values.sumOf { it.count { w -> w.isGuessed } }
        )
        dataManager.addGame(currentGame)
    }

    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // Sets up a new timer instance for a 5-minute countdown
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
    }

    //------ Dialog ------------------------------------------------------------------------------------------------------------------------
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

    // Shows a dialog
    private fun showHelpDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.help_info, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // Shows a dialog
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.setting_window, null)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }

        val toggleInputBtn = dialogView.findViewById<Button>(R.id.toggle_input_mode_btn)
        toggleInputBtn.text = when (currentInputMode) {
            InputMode.KEYBOARD -> "Switch to Buttons"
            InputMode.BUTTONS -> "Switch to Keyboard"
        }

        val themeSpinner = dialogView.findViewById<Spinner>(R.id.themeSpinner)
        val sharedPreferenceManger = SharedPreferenceManger(this)
        var checkedTheme = sharedPreferenceManger.theme

        // Set up adapter for spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            themeTitleList
        )
        Log.d("spinner", "themeSpinner $themeSpinner")
        Log.d("spinner", "adapter $adapter")
        themeSpinner.adapter = adapter

        // Set spinner to saved value
        themeSpinner.setSelection(checkedTheme)

        // Listen for user changes
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                if (position != sharedPreferenceManger.theme) {
                    sharedPreferenceManger.theme = position
                    AppCompatDelegate.setDefaultNightMode(
                        sharedPreferenceManger.themeFlag[position]
                    )
                    dialog.dismiss()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        toggleInputBtn.setOnClickListener {
            currentInputMode = when (currentInputMode) {
                InputMode.KEYBOARD -> InputMode.BUTTONS
                InputMode.BUTTONS -> InputMode.KEYBOARD
            }
            dialog.dismiss()
            updateInputModeUI()  // ⬅️ This applies the visual change after dialog closes
        }

        dialog.show()
    }

    private fun showPastGamesDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.past_games_board, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
        val gamesList = dataManager.getPastGames()

        if (gamesList.isNotEmpty()) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = PastGamesAdapter(gamesList)
        } else {
            recyclerView.visibility = View.GONE
            val tv = TextView(this).apply {
                text = "No past games yet. Play one and check back!"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(24, 24, 24, 24)
            }
            (recyclerView.parent as? ViewGroup)?.addView(tv)
        }

        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    //------ words ------------------------------------------------------------------------------------------------------------------------

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

        // ✅ Sort each list alphabetically
        for ((_, list) in wordListsByLength) {
            list.sortBy { it.word }
        }
    }

    //------ from txt

    // Reads all lines from the word list file in assets
    private fun readWordsFromAssets(): List<String> {
        val inputStream = assets.open(fileNameWordList)
        val reader = BufferedReader(inputStream.reader())
        return reader.readLines()
    }

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

}
