package com.example.anagramarama_app.data

data class PastGame(
    val date: String,
    val sevenLetterWord: String,
    val totalWordsCouldGet: Int,
    val totalWordsGot: Int
)