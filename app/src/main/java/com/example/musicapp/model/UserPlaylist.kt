package com.example.myapplication.model

data class UserPlaylist(
    val name: String = "",
    val songIds: List<String> = emptyList()
)
