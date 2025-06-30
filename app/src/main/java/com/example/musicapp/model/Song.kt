package com.example.myapplication.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    var id: String = "",
    var title: String = "",
    var image: String = "",
    var url: String = "",
    var moodIds: List<String> = listOf(),
    var suitableTimeIds: List<String> = listOf(),
    var duration: Int = 0,
    var artistNames: List<String> = emptyList(),
    var categoryIds: List<String> = listOf(),
    var count: Int = 0,
    var playlistIds: List<String> = emptyList(),

    @get:Exclude var isPlaying: Boolean = false,
    @get:Exclude var isFeatured: Boolean = false,
    @get:Exclude var isPriority: Boolean = false,
    @get:Exclude var isDownloaded: Boolean = false,
    @get:Exclude var isLiked: Boolean = false
) : Parcelable
