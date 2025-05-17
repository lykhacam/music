package com.example.myapplication.model

import java.io.Serializable

class Playlist() : Serializable {
    var id: Int = 0
    var name: String? = null
    var description: String? = null
    var image: String? = null
    var songs: List<Song> = emptyList()
}
