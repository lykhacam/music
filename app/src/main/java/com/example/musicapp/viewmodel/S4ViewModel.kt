package com.example.myapplication.viewmodel

import android.animation.ObjectAnimator
import androidx.lifecycle.*

class S4ViewModel(private val state: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_IS_PLAYING = "KEY_IS_PLAYING"
    }

    private val _isPlaying = MutableLiveData(state.get(KEY_IS_PLAYING) ?: false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    private lateinit var visualizerAnimators: List<ObjectAnimator>

    private val _isLiked = MutableLiveData(false)
    val isLiked: LiveData<Boolean> = _isLiked

    fun togglePlayPause() {
        val playNow = !(_isPlaying.value ?: false)
        _isPlaying.value = playNow
        state[KEY_IS_PLAYING] = playNow
    }

    fun toggleLike() {
        _isLiked.value = !(_isLiked.value ?: false)
    }

    fun setPlayingOnlyUI(play: Boolean) {
        _isPlaying.postValue(play)
        state[KEY_IS_PLAYING] = play
    }

}
