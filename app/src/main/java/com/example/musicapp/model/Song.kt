package com.example.myapplication.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String = "",
    val title: String = "",
    val image: String = "",
    val url: String = "",
    val duration: Int = 0,
    val artistNames: List<String> = emptyList(),
    val categoryIds: List<String> = listOf(),
    val count: Int = 0,

    @get:Exclude var isPlaying: Boolean = false,
    @get:Exclude var isFeatured: Boolean = false,
    @get:Exclude var isPriority: Boolean = false,
    @get:Exclude var isDownloaded: Boolean = false
) : Parcelable
