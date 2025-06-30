package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.Song
import com.example.myapplication.repository.SongRepository
import com.example.myapplication.utils.HistoryUtils

class S4ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SongRepository(application)

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex

    private val _songList = MutableLiveData<List<Song>>()
    val songList: LiveData<List<Song>> = _songList

    private val _isShuffle = MutableLiveData(false)
    val isShuffle: LiveData<Boolean> = _isShuffle

    enum class RepeatMode { NONE, ALL, ONE }
    private val _repeatMode = MutableLiveData(RepeatMode.NONE)
    val repeatMode: LiveData<RepeatMode> = _repeatMode

    private var originalList: List<Song> = emptyList()

    fun initSongs(list: List<Song>, index: Int) {
        originalList = list.map { it.copy() }
        _songList.value = list
        _currentIndex.value = index
        _currentSong.value = list[index]
    }

    fun togglePlayPause() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }

    fun setPlayingOnlyUI(isPlayingNow: Boolean) {
        _isPlaying.value = isPlayingNow
    }

    fun playSong(index: Int) {
        val list = _songList.value ?: return
        if (index in list.indices) {
            _currentIndex.value = index
            _currentSong.value = list[index]
        }
    }

    fun playNext() {
        val list = _songList.value ?: return
        val current = _currentIndex.value ?: 0

        _currentIndex.value = when (_repeatMode.value) {
            RepeatMode.ONE -> current
            else -> (current + 1) % list.size
        }

        _currentSong.value = list[_currentIndex.value!!]
    }

    fun playPrevious() {
        val list = _songList.value ?: return
        val current = _currentIndex.value ?: 0

        _currentIndex.value = when (_repeatMode.value) {
            RepeatMode.ONE -> current
            else -> if (current - 1 < 0) list.size - 1 else current - 1
        }

        _currentSong.value = list[_currentIndex.value!!]
    }

    fun toggleShuffle() {
        val shuffle = !(_isShuffle.value ?: false)
        _isShuffle.value = shuffle

        val current = _currentSong.value ?: return
        val newList = if (shuffle) originalList.shuffled() else originalList.map { it.copy() }

        _songList.value = newList
        _currentIndex.value = newList.indexOfFirst { it.id == current.id }.takeIf { it >= 0 } ?: 0
        _currentSong.value = newList[_currentIndex.value!!]
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
            else -> RepeatMode.NONE
        }
    }

    fun updateRecentlyPlayed(uid: String, songId: String) {
        repository.updateRecentlyPlayed(uid, songId)
    }

    fun toggleFavorite(uid: String, song: Song, onComplete: (Boolean) -> Unit) {
        repository.toggleFavorite(uid, song.id, onComplete)
    }

    fun saveListeningHistory(progress: Int, duration: Int) {
        val song = _currentSong.value ?: return
        HistoryUtils.saveListeningHistory(song, progress, duration)
    }

    fun getCurrentSongData(): Song? {
        return _currentSong.value
    }
    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun setShuffleMode(enabled: Boolean) {
        _isShuffle.value = enabled
    }


}
