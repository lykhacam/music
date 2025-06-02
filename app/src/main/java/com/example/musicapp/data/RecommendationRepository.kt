package com.example.myapplication.data

import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecommendationRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app"
    )

    /**
     * 1. Lấy lịch sử nghe của user, tính điểm cho mỗi bài dựa trên percentPlayed,
     *    rồi build list đề xuất.
     * 2. Lấy top thịnh hành (ví dụ từ node "trendingSongs" trên Firebase).
     */
    fun fetchRecommendations(
        onResult: (personalized: List<Song>, trending: List<Song>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            // chưa login → không có lịch sử, chỉ trả trending
            fetchTrending { trending ->
                onResult(emptyList(), trending)
            }
            return
        }

        // 1. Lấy lịch sử nghe
        db.getReference("users").child(uid).child("listeningHistory")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull { it.getValue(HistoryEntry::class.java) }
                    // sort theo percentPlayed desc
                    val topHistoryIds = history
                        .filter { it.percentPlayed >= 30 }
                        .sortedByDescending { it.percentPlayed }
                        .map { it.songId }
                        .take(10)
                    // fetch Song objects cho topHistoryIds
                    fetchSongsByIds(topHistoryIds) { personalizedSongs ->
                        // nếu quá ít đề xuất, bổ sung trending ngẫu nhiên
                        fetchTrending { trending ->
                            val personalized = if (personalizedSongs.size >= 5) {
                                personalizedSongs
                            } else {
                                personalizedSongs + trending.shuffled().take(5 - personalizedSongs.size)
                            }
                            onResult(personalized, trending)
                        }
                    }
                }
                override fun onCancelled(e: DatabaseError) = onError(e.toException())
            })
    }

    private fun fetchTrending(onResult: (List<Song>) -> Unit) {
        db.getReference("trendingSongs")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val trending = s.children.mapNotNull { it.getValue(Song::class.java) }
                    onResult(trending)
                }
                override fun onCancelled(e: DatabaseError) { onResult(emptyList()) }
            })
    }

    private fun fetchSongsByIds(ids: List<String>, onResult: (List<Song>) -> Unit) {
        if (ids.isEmpty()) {
            onResult(emptyList()); return
        }
        db.getReference("songs")
            .orderByKey()
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val all = s.children.mapNotNull { it.getValue(Song::class.java) }
                    onResult(all.filter { it.id in ids })
                }
                override fun onCancelled(e: DatabaseError) = onResult(emptyList())
            })
    }

    data class HistoryEntry(
        val songId: String = "",
        val percentPlayed: Int = 0,
        val timestamp: Long = 0,
        val artistId: String = "",
        val categoryId: String = ""
    )
}
