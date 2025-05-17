package com.example.myapplication.utils

import android.util.Log
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object HistoryUtils {

    /**
     * Lưu lịch sử nghe bài hát vào Firebase nếu đã nghe đủ phần trăm quy định.
     *
     * @param song Bài hát đã phát
     * @param currentPosition Vị trí đang phát hiện tại (ms)
     * @param duration Tổng thời lượng bài hát (ms)
     * @param minPercent Phần trăm tối thiểu để tính là đã nghe (mặc định: 30%)
     */
    fun saveListeningHistory(song: Song, currentPosition: Int, duration: Int, minPercent: Int = 30) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("HistoryUtils", "Người dùng chưa đăng nhập, không thể lưu lịch sử")
            return
        }

        if (duration <= 0) {
            Log.w("HistoryUtils", "Thời lượng bài hát không hợp lệ: $duration ms")
            return
        }

        val percentPlayed = (currentPosition * 100) / duration
        if (percentPlayed < minPercent) {
            Log.d("HistoryUtils", "Bỏ qua '${song.title}' vì chỉ nghe $percentPlayed% (<$minPercent%)")
            return
        }

        val artistId = song.artistNames.firstOrNull().orEmpty()
        val categoryId = song.categoryIds.firstOrNull().orEmpty()

        val historyData = mapOf(
            "songId" to song.id,
            "percentPlayed" to percentPlayed,
            "timestamp" to System.currentTimeMillis(),
            "artistId" to artistId,
            "categoryId" to categoryId
        )

        FirebaseDatabase.getInstance()
            .getReference("users/$uid/listeningHistory")
            .push()
            .setValue(historyData)
            .addOnSuccessListener {
                Log.d("HistoryUtils", "✅ Đã lưu lịch sử: ${song.title} ($percentPlayed%)")
            }
            .addOnFailureListener { error ->
                Log.e("HistoryUtils", "❌ Lỗi khi lưu lịch sử: ${error.message}", error)
            }
    }
}
