package com.example.anagramarama_app

import android.content.Context
import com.example.anagramarama_app.data.PastGame
import org.json.JSONObject

class DataManager(private val context: Context) {

    fun readJsonFromAssets(folder: String, fileName: String): String {
        val filePath = "$folder/$fileName"
        return context.assets.open(filePath).bufferedReader().use { it.readText() }
    }

    fun getPastGamesFromJson(jsonString: String): List<PastGame> {
        val jsonObject = JSONObject(jsonString)
        val pastGames = jsonObject.getJSONObject("gameInfo").getJSONArray("pastGames")

        val gamesList = mutableListOf<PastGame>()
        for (i in 0 until pastGames.length()) {
            val game = pastGames.getJSONObject(i)
            /*gamesList.add(
                PastGame(
                    date = game.getString("date"),
                    sevenLetterWord = game.getString("sevenLetterWord"),
                    howManyTotalWordsCouldGet = game.getInt("howManyTotalWordsCouldGet"),
                    howManyTotalWordsGot = game.getInt("howManyTotalWordsGot")
                )
            )*/
        }
        return gamesList
    }
}
