package io.github.aidenk.sevenshuffle

data class PastGame(
    val date: String,
    val sevenLetterWord: String,
    val totalWordsCouldGet: Int,
    val totalWordsGot: Int
)