package com.example.myapplication.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.R
import com.example.myapplication.model.Song

class SongViewModel : ViewModel() {
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    init {
        loadSongs()
    }

    private fun loadSongs() {
        _songs.value = listOf(
            Song("Bye Bye", "Marshmello, Juice WRLD", "2:09", R.drawable.sample_avatar),
            Song("Stay", "The Kid LAROI, Justin Bieber", "2:21", R.drawable.sample_avatar),
            Song("Blinding Lights", "The Weeknd", "3:20", R.drawable.sample_avatar),
            Song("Heat Waves", "Glass Animals", "3:58", R.drawable.sample_avatar),
            Song("Levitating", "Dua Lipa, DaBaby", "3:23", R.drawable.sample_avatar) ,
            Song("Bye Bye", "Marshmello, Juice WRLD", "2:09", R.drawable.sample_avatar),
            Song("Stay", "The Kid LAROI, Justin Bieber", "2:21", R.drawable.sample_avatar),
            Song("Blinding Lights", "The Weeknd", "3:20", R.drawable.sample_avatar),
            Song("Heat Waves", "Glass Animals", "3:58", R.drawable.sample_avatar),
            Song("Levitating", "Dua Lipa, DaBaby", "3:23", R.drawable.sample_avatar)
        )
    }
}
