package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.ListeningHistory
import com.example.myapplication.repository.FirebaseRepository

class RecommendationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseRepository()

    private val _topPreferences = MutableLiveData<Pair<String?, String?>>()
    val topPreferences: LiveData<Pair<String?, String?>> get() = _topPreferences

    private val _history = MutableLiveData<List<ListeningHistory>>()
    val history: LiveData<List<ListeningHistory>> get() = _history

    fun loadListeningHistory() {
        repository.getListeningHistory().observeForever { historyList ->
            _history.value = historyList
            analyzeListeningHistory(historyList)
        }
    }

    private fun analyzeListeningHistory(historyList: List<ListeningHistory>) {
        val artistCount = mutableMapOf<String, Double>()
        val categoryCount = mutableMapOf<String, Double>()

        for (item in historyList) {
            val weight = item.percentPlayed / 100.0
            if (item.artistId.isNotBlank()) {
                artistCount[item.artistId] = artistCount.getOrDefault(item.artistId, 0.0) + weight
            }
            if (item.categoryId.isNotBlank()) {
                categoryCount[item.categoryId] = categoryCount.getOrDefault(item.categoryId, 0.0) + weight
            }
        }

        val topArtist = artistCount.maxByOrNull { it.value }?.key
        val topCategory = categoryCount.maxByOrNull { it.value }?.key

        _topPreferences.value = Pair(topCategory, topArtist)
    }
}
