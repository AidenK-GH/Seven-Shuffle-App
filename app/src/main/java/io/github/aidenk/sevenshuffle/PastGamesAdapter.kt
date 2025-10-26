package io.github.aidenk.sevenshuffle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.aidenk.sevenshuffle.PastGame

class PastGamesAdapter(private val gamesList: List<PastGame>) :
    RecyclerView.Adapter<PastGamesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.date_text)
        val wordTextView: TextView = itemView.findViewById(R.id.word_text)
        val progressTextView: TextView = itemView.findViewById(R.id.progress_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.past_game_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = gamesList[position]
        holder.dateTextView.text = "Date: ${game.date}"
        holder.wordTextView.text = "Word: ${game.sevenLetterWord}"
        holder.progressTextView.text =
            "Words: ${game.totalWordsGot}/${game.totalWordsCouldGet}"
    }

    override fun getItemCount(): Int = gamesList.size
}
