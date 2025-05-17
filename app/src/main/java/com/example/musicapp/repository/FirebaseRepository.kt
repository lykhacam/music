package com.example.myapplication.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.model.ListeningHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FirebaseRepository {

    private val database = FirebaseDatabase.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    fun getListeningHistory(): LiveData<List<ListeningHistory>> {
        val historyLiveData = MutableLiveData<List<ListeningHistory>>()

        if (uid == null) {
            Log.w("FirebaseRepository", "Chưa đăng nhập, không thể lấy lịch sử")
            historyLiveData.value = emptyList()
            return historyLiveData
        }

        val ref = database.getReference("users/$uid/listeningHistory")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<ListeningHistory>()
                for (child in snapshot.children) {
                    val history = child.getValue(ListeningHistory::class.java)
                    if (history != null) {
                        historyList.add(history)
                    }
                }
                historyLiveData.value = historyList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseRepository", "Lỗi khi đọc listeningHistory: ${error.message}")
                historyLiveData.value = emptyList()
            }
        })

        return historyLiveData
    }
}
