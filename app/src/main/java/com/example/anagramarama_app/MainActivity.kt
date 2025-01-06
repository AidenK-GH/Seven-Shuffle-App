package com.example.anagramarama_app

import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.DigitsKeyListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import kotlin.random.Random
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anagramarama_app.data.PastGame
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    //private lateinit var dataManager: DataManager
    //private lateinit var dialogManager: DialogManager

    // File name for the word list in the assets folder
    private val fileNameWordList = "ag_list.txt"
    private val fileJsonNameGameHistoryBoard = "Game_History_Board.json"
    private var isThereAGameCurrentlyRunning = false

    // Data structure to store words categorized by their length
    private val wordListsByLength: MutableMap<Int, MutableList<WordEntry>> = mutableMapOf()
    private lateinit var sevenLetterWord: String

    // UI elements
    private lateinit var lettersTextView: TextView
    private lateinit var answersTempTextView: TextView
    private lateinit var newGameBtn: Button
    private lateinit var solveEndGameBtn: Button
    private lateinit var shuffleBtn: Button
    private lateinit var checkBtn: Button
    private lateinit var helpBtn: Button
    private lateinit var gameHistoryBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var userInputEditText: EditText
    private lateinit var checkInputTextView: TextView
    private lateinit var timerTextView: TextView

    // Timer setup for a 5-minute countdown
    private lateinit var gameTimer: CountDownTimer
    private val gameDurationInMillis: Long = 5 * 60 * 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //dataManager = DataManager(this)
        //dialogManager = DialogManager(this)

        // Initialize UI elements
        lettersTextView = findViewById(R.id.letters_textview)
        answersTempTextView = findViewById(R.id.answers_temp_textview)
        newGameBtn = findViewById(R.id.new_game_btn)
        solveEndGameBtn = findViewById(R.id.solve_end_game_btn)
        shuffleBtn = findViewById(R.id.shuffle_btn)
        checkBtn = findViewById(R.id.check_btn)
        helpBtn = findViewById(R.id.help_btn)
        gameHistoryBtn = findViewById(R.id.game_history_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        userInputEditText = findViewById(R.id.userInput_EditText)
        checkInputTextView = findViewById(R.id.checkInput_textview)
        timerTextView = findViewById(R.id.timer_textview)

        setupTimer()

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

    // Data class to track each word and whether it has been guessed
    data class WordEntry(val word: String, var isGuessed: Boolean = false)

    // Starts a new game by resetting game data and setting up word lists
    private fun startNewGame() {
        isThereAGameCurrentlyRunning = true
        sevenLetterWord = selectRandomlyASevenLetterWordFromList()
        setupWordLists()
        setupTimer()
        gameTimer.start()

        // Shuffle and display the selected seven-letter word
        lettersTextView.text = shuffleWord(sevenLetterWord)
        updateInputFilter()
        updateAnswersTempTextView()
    }

    // Populates the word lists by length from the available words that can be formed from sevenLetterWord
    private fun setupWordLists() {
        wordListsByLength.clear()
        val allWords = readWordsFromAssets()

        for (word in allWords) {
            if (canBeFormedFrom(word, sevenLetterWord)) {
                val length = word.length
                wordListsByLength.getOrPut(length) { mutableListOf() }.add(WordEntry(word))
            }
        }
    }

    // Updates input filter to allow only characters from the seven-letter word
    private fun updateInputFilter() {
        val allowedCharacters = sevenLetterWord.toCharArray().toSet().joinToString("")
        userInputEditText.keyListener = DigitsKeyListener.getInstance(allowedCharacters)
    }

    // Checks user input against words in wordListsByLength and updates the UI if guessed correctly
    private fun checkUserInput() {
        val userInput = userInputEditText.text.toString().trim()

        val wordLength = userInput.length
        val entries = wordListsByLength[wordLength]
        val wordEntry = entries?.find { it.word == userInput && !it.isGuessed }

        if (wordEntry != null) {
            wordEntry.isGuessed = true
            checkInputTextView.text = "CORRECT"
            updateAnswersTempTextView()

            // Check if user has won after guessing correctly
            if (hasUserWon()) {
                endGame()
                checkInputTextView.text = "You WON"
            }
        } else {
            checkInputTextView.text = "WRONG"
        }

        userInputEditText.text.clear()
    }

    // Updates the answersTempTextView with the list of all words categorized by length, with colored words
    private fun updateAnswersTempTextView() {
        val spannableBuilder = SpannableStringBuilder()

        wordListsByLength.keys.sorted().forEach { length ->
            val words = wordListsByLength[length] ?: emptyList()
            val totalWords = words.size
            val guessedWordsCount = words.count { it.isGuessed }

            // Add header with count
            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")

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

        answersTempTextView.text = spannableBuilder
    }

    // Ends the game and displays all words categorized by length, with guessed words in color
    private fun endGame() {
        isThereAGameCurrentlyRunning = false
        gameTimer.cancel()

        val spannableBuilder = SpannableStringBuilder()

        wordListsByLength.keys.sorted().forEach { length ->
            val words = wordListsByLength[length] ?: emptyList()
            val totalWords = words.size
            val guessedWordsCount = words.count { it.isGuessed }

            // Add header with count
            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")

            // Add all words with guessed words in color and unguessed in default color
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

    private fun saveGameToHistory() {
        val currentGame = mapOf(
            "date" to getCurrentDate(),
            "sevenLetterWord" to sevenLetterWord,
            "howManyTotalWordsCouldGet" to wordListsByLength.values.sumOf { it.size },
            "howManyTotalWordsGot" to wordListsByLength.values.sumOf { it.count { word -> word.isGuessed } }
        )

        // Read the existing JSON file
        val jsonString = readJsonFileFromAssets("data", fileJsonNameGameHistoryBoard)
        val jsonObject = JSONObject(jsonString)
        val pastGames = jsonObject.getJSONObject("gameInfo").getJSONArray("pastGames")

        // Add the current game to the array
        pastGames.put(JSONObject(currentGame))

        // Write the updated JSON back to the file
        val updatedJsonString = jsonObject.toString()
        writeJsonToFile("data", fileJsonNameGameHistoryBoard, updatedJsonString)
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
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showPastGamesDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.past_games_board, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        // Read the JSON file
        val jsonString = readJsonFileFromAssets("data", fileJsonNameGameHistoryBoard)
        val jsonObject = JSONObject(jsonString)
        val pastGames = jsonObject.getJSONObject("gameInfo").getJSONArray("pastGames")

        // Parse JSON data
        val gamesList = mutableListOf<PastGame>()
        for (i in 0 until pastGames.length()) {
            val game = pastGames.getJSONObject(i)
            gamesList.add(
                PastGame(
                    date = game.getString("date"),
                    sevenLetterWord = game.getString("sevenLetterWord"),
                    totalWordsCouldGet = game.getInt("howManyTotalWordsCouldGet"),
                    totalWordsGot = game.getInt("howManyTotalWordsGot")
                )
            )
        }

        // Set up RecyclerView
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PastGamesAdapter(gamesList)
        recyclerView.adapter = adapter

        // Handle the exit button
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Reads all lines from the word list file in assets
    private fun readWordsFromAssets(): List<String> {
        val inputStream = assets.open(fileNameWordList)
        val reader = BufferedReader(inputStream.reader())
        return reader.readLines()
    }

    // Randomly selects a 7-letter word from the word list
    private fun selectRandomlyASevenLetterWordFromList(): String {
        val words = readWordsFromAssets().filter { it.length == 7 }
        return words[Random.nextInt(words.size)]
    }

    // Checks if subWord can be formed from baseWord by comparing character counts
    private fun canBeFormedFrom(subWord: String, baseWord: String): Boolean {
        val baseCharCounts = baseWord.groupingBy { it }.eachCount().toMutableMap()
        for (char in subWord) {
            val count = baseCharCounts[char] ?: 0
            if (count == 0) return false
            baseCharCounts[char] = count - 1
        }
        return true
    }

    // Checks if the player has won by verifying all words have been guessed
    private fun hasUserWon(): Boolean {
        return wordListsByLength.values.all { list -> list.all { it.isGuessed } }
    }

    // Shuffles the letters in a word and returns the shuffled string
    private fun shuffleWord(word: String): String {
        return " " + word.toList().shuffled().joinToString(" ")
    }

    // JSON
    private fun readJsonFileFromAssets(folder: String, fileName: String): String {
        val filePath = "$folder/$fileName"
        return applicationContext.assets.open(filePath).bufferedReader().use { it.readText() }
    }

    private fun writeJsonToFile(folder: String, fileName: String, jsonString: String) {
        val file = File(filesDir, "$folder/$fileName")
        file.parentFile?.mkdirs() // Create directories if they don't exist
        file.writeText(jsonString)
    }

}
