package com.example.anagramarama_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//data class WordEntry(val word: String, var isGuessed: Boolean = false)
import com.example.anagramarama_app.WordEntry

class WordAdapter : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    private val wordList = mutableListOf<WordEntry>()

    // Updates the list with a new set of words and refreshes the RecyclerView
    fun submitList(newList: List<WordEntry>) {
        wordList.clear()
        wordList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_square, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val wordEntry = wordList[position]
        // Display the word if guessed, otherwise show an empty placeholder
        holder.wordTextView.text = if (wordEntry.isGuessed) wordEntry.word else ""
    }

    override fun getItemCount(): Int = wordList.size

    // ViewHolder class to hold the TextView for each word square
    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wordTextView: TextView = view.findViewById(R.id.word_text_view)
    }
}
