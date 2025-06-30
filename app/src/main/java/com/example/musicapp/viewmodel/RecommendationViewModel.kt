package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.RecommendationRepository
import com.example.myapplication.model.Song

class RecommendationViewModel : ViewModel() {

    private val _recommendations = MutableLiveData<List<Song>>()
    val recommendations: LiveData<List<Song>> get() = _recommendations

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> get() = _error

    fun loadRecommendations() {
        RecommendationRepository.fetchRecommendations(
            onResult = { songs ->
                _recommendations.postValue(songs) // SAFE for background thread
            },
            onError = {
                _error.postValue(it)
            }
        )
    }
}
