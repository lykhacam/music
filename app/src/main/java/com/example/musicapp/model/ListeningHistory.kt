package com.example.myapplication.model

data class ListeningHistory(
    val songId: String = "",
    val percentPlayed: Int = 0,
    val timestamp: Long = 0L,
    val artistId: String = "",
    val categoryId: String = ""
)
