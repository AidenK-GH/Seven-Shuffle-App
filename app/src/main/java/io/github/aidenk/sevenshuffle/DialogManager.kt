package io.github.aidenk.sevenshuffle

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DialogManager(private val context: Context) {

    fun showHelpDialog() {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.help_info, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showPastGamesDialog(pastGames: List<PastGame>) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.past_games_board, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.board_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = PastGamesAdapter(pastGames)
        recyclerView.adapter = adapter

        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /*
    // Shows a dialog to confirm if the user wants to start a new game
    fun showNewGameConfirmationDialog() {
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

    fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.setting_window, null)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialogView.findViewById<Button>(R.id.exit_button).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
    */
}
