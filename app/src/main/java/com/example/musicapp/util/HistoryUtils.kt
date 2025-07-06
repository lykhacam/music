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
            return
        }

        if (duration <= 0) return

        val percentPlayed = (currentPosition * 100) / duration
        if (percentPlayed < 3) {
            return
        }

        val songId = song.id
        if (!songId.matches(Regex("^s\\d+$"))) {
            return
        }

        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val historyRef = db.getReference("users/$uid/listeningHistory")

        val data = mapOf(
            "percent" to percentPlayed,
            "timestamp" to System.currentTimeMillis()
        )

        historyRef.child(songId).setValue(data).addOnSuccessListener {
            triggerGenerateUserVector(uid)

            // Giới hạn tối đa 15 bài gần nhất
            historyRef.get().addOnSuccessListener { snapshot ->
                val entries = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: return@mapNotNull null
                    Pair(id, timestamp)
                }

                if (entries.size > 15) {
                    val toRemove = entries
                        .sortedBy { it.second } // cũ nhất trước
                        .take(entries.size - 15)

                    toRemove.forEach {
                        historyRef.child(it.first).removeValue()
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
