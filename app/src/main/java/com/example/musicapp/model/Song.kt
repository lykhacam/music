package com.example.myapplication.model

import android.os.Parcelable
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
    val isFeatured: Boolean = false,
    val count: Int = 0,
    val isPlaying: Boolean = false,
    val isPriority: Boolean = false,
    var isDownloaded: Boolean = false
) : Parcelable
