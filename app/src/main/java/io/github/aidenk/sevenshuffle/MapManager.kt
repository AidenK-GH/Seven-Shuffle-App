package io.github.aidenk.sevenshuffle

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader

object MapManager {

    // File name for the word list in the assets folder
    private val fileNameWordList = "ag_list.txt"
    private val fileNameJSONAllWordMap = "all_word_map.json"

    private lateinit var ver_context: Context

    // map that keeps the Library of all the words. key is the letters that used to make words in value:list.
    @Volatile lateinit var allWordsMap: MutableMap<String, List<String>>
    // list of all 7 letter words. faster than reading asset file everytime.
    @Volatile lateinit var listSevenLetterWords: List<String>

    fun startLoad(context: Context){
        ver_context = context
        allWordsMap = loadAllWordMapFromAssets()
        listSevenLetterWords = readWordsFromAssets().filter { it.length == 7 };
    }

    private fun loadAllWordMapFromAssets(): MutableMap<String, List<String>> {
        // 1. Read file as string
        val jsonText = ver_context.assets.open(fileNameJSONAllWordMap)
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

    // Reads all lines from the word list file in assets
    private fun readWordsFromAssets(): List<String> {
        val inputStream = ver_context.assets.open(fileNameWordList)
        val reader = BufferedReader(inputStream.reader())
        return reader.readLines()
    }
}