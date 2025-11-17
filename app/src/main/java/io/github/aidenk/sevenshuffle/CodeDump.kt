package io.github.aidenk.sevenshuffle

import org.json.JSONObject
import java.io.BufferedReader

//
///*
//    This is where old code that no longer in use gets place
//    in-order not to crowd MainActivity.kt
//
// */
//class CodeDump {

    // ------- before DataManager --------------------------------------------------------
    /*
        private val fileJsonNameGameHistoryBoard = "Game_History_Board.json"
        private val folderNameWhereFileJsonNameGameHistoryBoardIs = "data"

    private fun saveGameToHistory() {
        val currentGame = mapOf(
            "date" to getCurrentDate(),
            "sevenLetterWord" to sevenLetterWord,
            "howManyTotalWordsCouldGet" to wordListsByLength.values.sumOf { it.size },
            "howManyTotalWordsGot" to wordListsByLength.values.sumOf { it.count { word -> word.isGuessed } }
        )

        // Read the existing JSON file
        val jsonString = readJsonFile("data", fileJsonNameGameHistoryBoard)
        val jsonObject = JSONObject(jsonString)
        if (jsonString !== "{}") {
            val pastGames = jsonObject.getJSONObject("gameInfo").getJSONArray("pastGames")

            // Add the current game to the array
            pastGames.put(JSONObject(currentGame))

            // Write the updated JSON back to the file
            val updatedJsonString = jsonObject.toString()
            writeJsonToFile(updatedJsonString) //"data", fileJsonNameGameHistoryBoard,
        }
    }

    private fun showPastGamesDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.past_games_board, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        // Read the JSON file
        val jsonString = readJsonFile("data", fileJsonNameGameHistoryBoard)
        val jsonObject = JSONObject(jsonString)
        Log.d("TEST", "jsonString = "+jsonString)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
        if(jsonString !== "{}"){
            val pastGames = jsonObject.getJSONObject("gameInfo").getJSONArray("pastGames")

            // Parse JSON data
            val gamesList = mutableListOf<PastGame>()
            for (i in 0 until pastGames.length()) {
                val game = pastGames.getJSONObject(i)
                gamesList.add(
                    PastGame(
                        sevenLetterWord = game.getString("sevenLetterWord"),
                        date = game.getString("date"),
                        totalWordsCouldGet = game.getInt("howManyTotalWordsCouldGet"),
                        totalWordsGot = game.getInt("howManyTotalWordsGot")
                    )
                )
            }

            // Set up RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = PastGamesAdapter(gamesList)
            recyclerView.adapter = adapter
        } else {
            // Hide RecyclerView
            recyclerView.visibility = View.GONE
            // Create a TextView programmatically
            val errorText = TextView(this).apply {
                text = "Sorry, couldn't find the JSON file. Error is being handled, please be patient."
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(24, 24, 24, 24)
            }

            // Add TextView to the same parent as RecyclerView
            (recyclerView.parent as? ViewGroup)?.addView(errorText)
        }

        // Handle the exit button
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    //--------- JSON ------------------------------------------------------------------------------------------
    private fun readJsonFileFromAssets(folder: String, fileName: String): String {
        val filePath = "$folder/$fileName"
        return applicationContext.assets.open(filePath).bufferedReader().use { it.readText() }
    }

    private fun readJsonFile(folder: String, fileName: String): String {

        //Log.d("PATH", "filesDir = ${filesDir.absolutePath}")
       // Log.d("PATH", "child = $folder/$fileName")

        val parent = filesDir.parentFile
       // Log.d("PATH", "parent = ${parent?.absolutePath}")

        if (parent != null) {
            parent.listFiles()?.forEach {
                Log.d("FILES", "found: ${it.absolutePath}")
            }
        }

        val file = File(filesDir, "$folder/$fileName")
        return if (file.exists()) {
            file.readText()
        } else {
            "{}"  // Return empty JSON if file doesn't exist
        }
    }

    private fun writeJsonToFile_old(folder: String, fileName: String, jsonString: String) {
        val file = File(filesDir, "$folder/$fileName")
        file.parentFile?.mkdirs() // Create directories if they don't exist
        file.writeText(jsonString)
    }

    private fun writeJsonToFile(jsonString: String) {
        val file = File(filesDir, "$folderNameWhereFileJsonNameGameHistoryBoardIs/$fileJsonNameGameHistoryBoard")
        file.parentFile?.mkdirs()
        file.writeText(jsonString)
        Log.d("FileWrite", "JSON saved to ${file.absolutePath}")
    }


    */

    /*
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
     */
//}
/*
//    // Reads all lines from the word list file in assets
//    private fun readWordsFromAssets(): List<String> {
//        val inputStream = assets.open(fileNameWordList)
//        val reader = BufferedReader(inputStream.reader())
//        return reader.readLines()
//    }
*/

/*
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
*/

/*answersTempTextView before the fix for when there isn't a X length
    // Updates the answersTempTextView with the list of all words categorized by length, with colored words
//    private fun updateAnswersTempTextView() {
//        val spannableBuilder = SpannableStringBuilder()
//        //var textForProgressBar = ""
//        var indexPbLenTextViewList = 0
//
//        wordListsByLength.keys.sorted().forEach { length ->
//            val words = wordListsByLength[length] ?: emptyList()
//            val totalWords = words.size
//            val guessedWordsCount = words.count { it.isGuessed }
//
//            // Add header with count
//            spannableBuilder.append("$length-letter words ($guessedWordsCount / $totalWords):\n")
//            //textForProgressBar += "$length($guessedWordsCount/$totalWords) "
//            pbLenTextViewList[indexPbLenTextViewList].text = "$length($guessedWordsCount/$totalWords)"
//            indexPbLenTextViewList++
//
//            // Add words with color
//            words.forEachIndexed { index, wordEntry ->
//                val wordText = if (wordEntry.isGuessed) wordEntry.word else "_ ".repeat(wordEntry.word.length).trim()
//                val start = spannableBuilder.length
//                spannableBuilder.append(wordText)
//
//                // Set color for guessed words
//                val color = ContextCompat.getColor(
//                    answersTempTextView.context,
//                    if (wordEntry.isGuessed) R.color.guessed_word_color else R.color.default_word_color
//                )
//                spannableBuilder.setSpan(
//                    ForegroundColorSpan(color),
//                    start,
//                    spannableBuilder.length,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                )
//
//                // Add a comma and space after each word, except the last one
//                if (index < words.size - 1) {
//                    spannableBuilder.append(", ")
//                }
//            }
//
//            // Add a new line between length groups
//            spannableBuilder.append("\n\n")
//        }
//
//        //progressBarTextview.text = textForProgressBar
//        answersTempTextView.text = spannableBuilder
//    }
    */



