package com.example.myapplication.model

data class User(
    val email: String = "",
    val favorites: Map<String, Boolean> = emptyMap(),
    val recentlyPlayed: Map<String, Long> = emptyMap(),
    val playlists: Map<String, Any> = emptyMap(),
    val name: String = "",
    val avatarUrl: String = "",
    val isPremium: Boolean = false
)
