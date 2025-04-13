package com.example.myapplication.model

data class Playlist(
    val name: String,
    val description: String,
    val imageResId: Int,
    val songs: List<Song> = emptyList()
)

