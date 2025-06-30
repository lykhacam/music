package com.example.myapplication.utils

import android.util.Log
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import java.io.IOException

object HistoryUtils {

    private val client = OkHttpClient()

    fun saveListeningHistory(song: Song, currentPosition: Int, duration: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.w("HistoryUtils", "⚠️ Người dùng chưa đăng nhập")
            return
        }

        if (duration <= 0) {
            Log.w("HistoryUtils", "⚠️ Thời lượng bài hát không hợp lệ: $duration")
            return
        }

        val percentPlayed = (currentPosition * 100) / duration
        if (percentPlayed < 3) {
            Log.d("HistoryUtils", "⏳ Dưới 3% → không lưu")
            return
        }

        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val historyRef = db.getReference("users/$uid/listeningHistory")
        val songId = song.id

        historyRef.child(songId).setValue(percentPlayed).addOnSuccessListener {
            Log.d("HistoryUtils", "✅ Đã lưu $songId = $percentPlayed%")

            // Gọi Cloud Function để cập nhật user_vector
            triggerGenerateUserVector(uid)

            // Giới hạn tối đa 15 bài
            historyRef.get().addOnSuccessListener { snapshot ->
                val entries = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val percent = child.getValue(Int::class.java) ?: 0
                    val time = child.ref.key?.hashCode() ?: 0
                    Triple(id, percent, time.toLong())
                }

                if (entries.size > 15) {
                    val toRemove = entries
                        .sortedWith(compareBy<Triple<String, Int, Long>> { it.second }.thenBy { it.third })
                        .take(entries.size - 15)

                    toRemove.forEach {
                        historyRef.child(it.first).removeValue()
                        Log.d("HistoryUtils", "🗑️ Xoá bài ${it.first} (${it.second}%)")
                    }
                }
            }
        }
    }

    private fun triggerGenerateUserVector(uid: String) {
        val url = "https://asia-southeast1-appmusicrealtime.cloudfunctions.net/generateUserVector?uid=$uid"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HistoryUtils", "❌ Lỗi gọi Cloud Function generateUserVector", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("HistoryUtils", "🌟 Cloud Function generateUserVector OK")
                } else {
                    Log.w("HistoryUtils", "⚠️ generateUserVector failed: ${response.code}")
                }
            }
        })
    }
}
