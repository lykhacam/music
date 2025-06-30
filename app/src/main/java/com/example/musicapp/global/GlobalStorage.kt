package com.example.myapplication.global

import com.example.myapplication.model.Song
import com.example.myapplication.viewmodel.MiniPlayerViewModel

object GlobalStorage {
    var top50Songs: List<Song> = emptyList()
    var searchResults: List<Song> = emptyList()
    var currentSongList: List<Song> = emptyList()
    var currentSongIndex: Int = 0

    lateinit var miniPlayerViewModel: MiniPlayerViewModel
}
