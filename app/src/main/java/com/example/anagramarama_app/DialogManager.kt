package com.example.anagramarama_app

import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anagramarama_app.R
import com.example.anagramarama_app.data.PastGame
//import com.example.anagramarama_app.data.PastGamesAdapter

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
}
