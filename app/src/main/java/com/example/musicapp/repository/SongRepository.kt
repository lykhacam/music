package com.example.myapplication.repository

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SongRepository(private val app: Application) {

    private val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val userUid get() = FirebaseAuth.getInstance().currentUser?.uid

    fun toggleFavorite(uid: String, songId: String, onComplete: (Boolean) -> Unit) {
        val ref = db.getReference("users/$uid/favorites/$songId")
        ref.get().addOnSuccessListener {
            if (it.exists()) {
                ref.removeValue().addOnCompleteListener { onComplete(false) }
            } else {
                ref.setValue(true).addOnCompleteListener { onComplete(true) }
            }
        }
    }

    fun updateRecentlyPlayed(uid: String, songId: String) {
        val ref = db.getReference("users/$uid/recentlyPlayed")
        ref.get().addOnSuccessListener { snapshot ->
            val map = snapshot.children.associateBy({ it.key!! }, { it.value as? Long ?: System.currentTimeMillis() }).toMutableMap()

            map[songId] = System.currentTimeMillis()

            while (map.size > 20) {
                val oldest = map.minByOrNull { it.value }?.key
                if (oldest != null) map.remove(oldest)
            }

            ref.setValue(map)
        }.addOnFailureListener {
            Log.w("SongRepository", "⚠️ updateRecentlyPlayed failed: ${it.message}")
        }
    }

    // Nếu bạn chỉ truyền id và cần fetch bài hát
    // fun getSongById(songId: String, onResult: (Song?) -> Unit)
}
