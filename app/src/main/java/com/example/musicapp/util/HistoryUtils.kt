package com.example.myapplication.utils

import android.util.Log
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object HistoryUtils {

    fun saveListeningHistory(
        song: Song,
        currentPosition: Int,
        duration: Int
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("HistoryUtils", "⚠️ Người dùng chưa đăng nhập")
            return
        }

        if (duration <= 0) {
            Log.w("HistoryUtils", "⚠️ Thời lượng bài hát không hợp lệ: $duration ms")
            return
        }

        val percentPlayed = (currentPosition * 100) / duration
        if (percentPlayed < 80) {
            Log.d("HistoryUtils", "⏳ Chưa đủ 80% → không lưu")
            return
        }

        val artistRaw = song.artistNames.firstOrNull().orEmpty()
        val artistId = sanitizeKey(artistRaw)

        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val userRef = db.getReference("users/$uid/listeningHistory")

        // Ghi thời gian và giới hạn 10 tác giả gần nhất
        val timestamp = System.currentTimeMillis()
        val artistTimeRef = userRef.child("artistTimestamps").child(artistId)
        artistTimeRef.setValue(timestamp).addOnSuccessListener {
            userRef.child("artistTimestamps").get().addOnSuccessListener { snapshot ->
                val sorted = snapshot.children
                    .mapNotNull { it.key?.let { k -> k to (it.getValue(Long::class.java) ?: 0L) } }
                    .sortedByDescending { it.second }
                    .take(10)
                    .associate { it.first to it.second }

                userRef.child("artistTimestamps").setValue(sorted)
                Log.d("HistoryUtils", "🕓 Lưu artist timestamp='$artistRaw' ($artistId) = $timestamp")
            }
        }

        // Lưu tổng phần trăm đã nghe của artist
        val artistRef = userRef.child("artists").child(artistId)
        artistRef.get().addOnSuccessListener { snapshot ->
            val oldPercent = snapshot.value?.let {
                if (it is Long || it is Int) (it as Number).toInt() else 0
            } ?: 0
            val newPercent = (oldPercent + percentPlayed).coerceAtMost(100)
            artistRef.setValue(newPercent)
            Log.d("HistoryUtils", "🎤 Lưu artist='$artistRaw' ($artistId) = $newPercent%")
        }.addOnFailureListener {
            Log.e("HistoryUtils", "❌ Lỗi khi đọc artist='$artistId'", it)
        }
    }

    // Hàm lọc key hợp lệ cho Firebase (tránh crash do ký tự đặc biệt)
    private fun sanitizeKey(key: String): String {
        return key.lowercase()
            .replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "_")
            .replace("/", "_")
    }
}
