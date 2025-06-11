package com.example.myapplication.View

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.SongApproveAdapter
import com.example.myapplication.model.Song
import com.google.firebase.database.*

class AdminApproveActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongApproveAdapter
    private val pendingList = mutableListOf<Pair<String, Song>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_approve)

        recyclerView = findViewById(R.id.recyclerPendingSongs)
        adapter = SongApproveAdapter(
            pendingList,
            onApprove = { songId, song -> approveSong(songId, song) },
            onReject = { songId -> rejectSong(songId) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadPendingSongs()
    }

    private fun loadPendingSongs() {
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("pendingUploads")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pendingList.clear()
                for (child in snapshot.children) {
                    val song = child.getValue(Song::class.java)
                    if (song != null) {
                        pendingList.add(child.key!! to song)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminApproveActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun approveSong(songId: String, song: Song) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        val songsRef = db.getReference("songs/$songId")
        val pendingRef = db.getReference("pendingUploads/$songId")

        val fullSongData = mapOf(
            "id" to songId,
            "title" to song.title,
            "artistNames" to (song.artistNames ?: listOf()),
            "categoryIds" to (song.categoryIds ?: listOf()),
            "duration" to (song.duration ?: 0),
            "image" to song.image.orEmpty(),
            "url" to song.url.orEmpty(),
            "count" to 0,
        )

        songsRef.setValue(fullSongData)
            .addOnSuccessListener {
                pendingRef.removeValue()
                Toast.makeText(this, "✅ Đã duyệt bài: ${song.title}", Toast.LENGTH_SHORT).show()
                loadPendingSongs()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Lỗi khi duyệt bài: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun rejectSong(songId: String) {
        val db = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
        db.getReference("pendingUploads/$songId").removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "❌ Đã từ chối bài hát", Toast.LENGTH_SHORT).show()
                loadPendingSongs()
            }
    }
}
