package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

object RecommendationHelper {

    private val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val songsRef = db.getReference("songs")

    fun getArtistBasedRecommendations(callback: (List<Song>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(emptyList())

        // Bước 1: Lấy top 10 artist gần nhất
        db.getReference("users/$uid/listeningHistory/artists")
            .orderByValue()
            .limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val topArtists = snapshot.children.mapNotNull { it.key }
                    Log.d("RecommendationHelper", "🎧 Top Artists: $topArtists")

                    // Bước 2: Lấy toàn bộ bài hát
                    songsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(songSnapshot: DataSnapshot) {
                            val allSongs = songSnapshot.children.mapNotNull { it.getValue(Song::class.java) }

                            // Bước 3: Lọc các bài hát theo artist ưu tiên
                            val prioritySongs = allSongs.filter { song ->
                                song.artistNames.any { artist ->
                                    val key = artist
                                    .lowercase()
                                    .replace("[.#$\\[\\]()]".toRegex(), "_")
                                    .replace(" ", "_")

                                    topArtists.contains(key)
                                }
                            }.shuffled()

                            // Bước 4: Bổ sung nếu chưa đủ 50 bài
                            // 🔹 B4: Bổ sung bài hát bất kỳ nếu chưa đủ 50 bài (random)
                            val otherSongs = allSongs.filterNot { it in prioritySongs }.shuffled()
                            val finalList = (prioritySongs + otherSongs).take(50)

//
                            Log.d("RecommendationHelper", "✅ Recommended songs: ${prioritySongs.map { it.title }}")
                            callback(finalList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("RecommendationHelper", "❌ Error loading songs", error.toException())
                            callback(emptyList())
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RecommendationHelper", "❌ Error loading artists", error.toException())
                    callback(emptyList())
                }
            })
    }
}
