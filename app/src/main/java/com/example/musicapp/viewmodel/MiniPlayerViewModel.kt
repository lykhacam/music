package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Song

class MiniPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _hasStartedPlaying = MutableLiveData(false)
    val hasStartedPlaying: LiveData<Boolean> = _hasStartedPlaying

    private val _songList = MutableLiveData<List<Song>>()
    val songList: LiveData<List<Song>> get() = _songList

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> get() = _currentIndex

    fun setSong(song: Song) {
        _currentSong.value = song
    }

    fun setPlaying(value: Boolean) {
        _isPlaying.value = value
    }

    fun togglePlayPause() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }

    fun clearSong() {
        _currentSong.value = null
        _isPlaying.value = false
        _hasStartedPlaying.value = false
    }

    fun setHasStartedPlaying(started: Boolean) {
        _hasStartedPlaying.value = started
    }

    fun setSongList(list: List<Song>) {
        _songList.value = list
    }

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index
        _currentSong.value = _songList.value?.getOrNull(index)
    }
}
