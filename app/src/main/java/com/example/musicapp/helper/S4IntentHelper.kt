package com.example.myapplication.helper

import android.content.Intent
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song

object S4IntentHelper {

    /**
     * Trích xuất bài hát từ Intent.
     * Trả về Pair(songList, index) nếu tìm được, hoặc null nếu lỗi.
     */
    fun getSongFromIntent(intent: Intent): Pair<List<Song>, Int>? {
        val songId = intent.getStringExtra("song_id") ?: return null
        val source = intent.getStringExtra("source") ?: "unknown"

        val list: List<Song> = when (source) {
            "top50" -> GlobalStorage.top50Songs
            "search" -> GlobalStorage.searchResults
            "home" -> GlobalStorage.currentSongList
            else -> emptyList()
        }

        val index = list.indexOfFirst { it.id == songId }
        return if (index != -1) Pair(list, index) else null
    }
}
