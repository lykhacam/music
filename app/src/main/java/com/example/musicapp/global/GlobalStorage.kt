package com.example.myapplication.global

import com.example.myapplication.model.Song

object GlobalStorage {
    var currentSongList: List<Song> = emptyList()
    var currentSongIndex: Int = 0
}
