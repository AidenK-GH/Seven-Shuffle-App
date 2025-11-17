package io.github.aidenk.sevenshuffle

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DataManager(private val context: Context) {

    companion object {
        private const val FOLDER = "data"
        private const val FILE = "Game_History_Board.json"
        private const val SEED = """{"gameInfo":{"pastGames":[]}}"""
        private const val MAX_PAST_GAMES = 100
    }

    // Scope for background work (can be app-wide singleton)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dir: File get() = File(context.filesDir, FOLDER)
    private val file: File get() = File(dir, FILE)

    private fun ensureFile(): File {
        if (!dir.exists()) dir.mkdirs()
        if (!file.exists()) file.writeText(SEED)
        return file
    }

    /** Read the persisted JSON (always returns valid structure) */
    fun readHistoryJson(): String = ensureFile().readText(Charsets.UTF_8)

    /** Write the whole JSON */
    suspend fun writeHistoryJson(json: String) {
        ensureFile().writeText(json, Charsets.UTF_8)
        //Log.d("FileWrite", "Saved JSON to ${file.absolutePath}")
    }

    /** Append one game to pastGames */
    suspend fun addGame(game: Map<String, Any?>) {
        val root = try {
            JSONObject(readHistoryJson())
        } catch (_: Exception) {
            JSONObject(SEED)
        }
        val gameInfo =
            root.optJSONObject("gameInfo") ?: JSONObject().also { root.put("gameInfo", it) }
        val pastGames =
            gameInfo.optJSONArray("pastGames") ?: JSONArray().also { gameInfo.put("pastGames", it) }

        // Enforce max size BEFORE adding the new one
        if (pastGames.length() >= MAX_PAST_GAMES) {
            // number of items to remove so we end up with MAX_PAST_GAMES - 1
            val toRemove = pastGames.length() - MAX_PAST_GAMES + 1
            for (i in 0 until toRemove) {
                // Oldest = index 0, repeated removal keeps trimming from the front
                pastGames.remove(0)
            }
        }

        pastGames.put(JSONObject(game))
        writeHistoryJson(root.toString())
    }

    /** Parse JSON into your model list */
    fun getPastGames(): List<PastGame> {
        val json = readHistoryJson()
        val root = JSONObject(json)
        val gameInfo = root.optJSONObject("gameInfo") ?: return emptyList()
        val arr = gameInfo.optJSONArray("pastGames") ?: return emptyList()

        val out = mutableListOf<PastGame>()
        for (i in 0 until arr.length()) {
            val g = arr.getJSONObject(i)
            out.add(
                PastGame(
                    date = g.getString("date"),
                    sevenLetterWord = g.getString("sevenLetterWord"),
                    totalWordsCouldGet = g.getInt("howManyTotalWordsCouldGet"),
                    totalWordsGot = g.getInt("howManyTotalWordsGot")
                )
            )
        }
        return out
    }

    /** Optional: quick path for debugging in Logcat */
    fun historyPath(): String = ensureFile().absolutePath
}
