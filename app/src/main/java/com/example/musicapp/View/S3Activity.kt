package com.example.myapplication.View

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.ActivityScreen3Binding
import com.example.myapplication.model.Song

class S3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen3Binding
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        loadSongs()
    }

    private fun setupUI() {
        // Nhận dữ liệu từ Intent và hiển thị ảnh playlist nếu có
        val imageRes = intent.getIntExtra("playlist_image", -1)
        if (imageRes != -1) {
            binding.imgPlaylist.setImageResource(imageRes)
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(getDummySongs()) { song ->
            // Chuyển sang screen4
            val intent = Intent(this, S4Activity::class.java).apply {
                putExtra("title", song.title)
                putExtra("artist", song.artist)
                putExtra("imageResId", song.imageResId)
            }
            startActivity(intent)
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(this@S3Activity)
            adapter = songAdapter
        }
    }

    private fun loadSongs() {
        // Nếu bạn có danh sách bài hát thật theo playlist, hãy load theo name từ intent
        // Ở đây là danh sách cứng (dummy)
        songAdapter.setData(getDummySongs())
    }

    private fun getDummySongs(): List<Song> {
        return  listOf(
            Song("Sunflower", "Post Malone", "2:38", R.drawable.sample_avatar),
            Song("Let Her Go", "Passenger", "4:12", R.drawable.sample_avatar)
        )
    }
}
