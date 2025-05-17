package com.example.myapplication.View

import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.ActivityScreen3Binding
import com.example.myapplication.databinding.IncludeHeaderControlsBinding
import com.example.myapplication.fragment.MiniPlayerFragment
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Artist
import com.example.myapplication.model.Song
import com.example.myapplication.service.MusicService
import com.example.myapplication.viewmodel.ArtistViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModelFactory
import com.example.myapplication.viewmodel.SongViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class S3Activity : AppCompatActivity() {

    private val artistViewModel: ArtistViewModel by viewModels()
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel

    private lateinit var binding: ActivityScreen3Binding
    private lateinit var songAdapter: SongAdapter

    private var artistList: List<Artist> = emptyList()
    private var filteredSongs: List<Song> = emptyList()
    private var categoryId: String = ""
    private var isPlayingAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        miniPlayerViewModel = ViewModelProvider(
            application as ViewModelStoreOwner,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]

        setupHeaderControls()

        supportFragmentManager.beginTransaction()
            .replace(binding.miniPlayerContainer.id, MiniPlayerFragment())
            .commit()

        miniPlayerViewModel.hasStartedPlaying.observe(this) { started ->
            updateMiniPlayerVisibility(started)
        }

        updateMiniPlayerVisibility(miniPlayerViewModel.hasStartedPlaying.value ?: false)

        categoryId = intent.getStringExtra("category_id") ?: ""
        val categoryImage = intent.getStringExtra("category_image")
        categoryImage?.let { Glide.with(this).load(it).into(binding.imgPlaylist) }

        setupRecyclerView()
        observeViewModel()

        binding.btnPlayAll.setOnClickListener {
            if (filteredSongs.isEmpty()) return@setOnClickListener

            if (isPlayingAll) {
                stopMusic()
            } else {
                val currentIndex = GlobalStorage.currentSongIndex
                if (currentIndex in filteredSongs.indices && GlobalStorage.currentSongList == filteredSongs) {
                    val playIntent = Intent(this, MusicService::class.java).apply {
                        action = MusicService.ACTION_PLAY
                    }
                    ContextCompat.startForegroundService(this, playIntent)
                    miniPlayerViewModel.setPlaying(true)
                    isPlayingAll = true
                    binding.btnPlayAll.setImageResource(R.drawable.ic_pause)
                } else {
                    playEntirePlaylist(filteredSongs, 0)
                }
            }
        }
    }

    private fun setupHeaderControls() {
        val btnBack = binding.iclHctl.btnBack
        val btnMenu = binding.iclHctl.btnMenu

        btnBack.setOnClickListener { finish() }

        btnMenu.setOnClickListener {
            val currentSong = filteredSongs.getOrNull(GlobalStorage.currentSongIndex) ?: return@setOnClickListener
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val favRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(uid).child("favorites")

            favRef.child(currentSong.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        favRef.child(currentSong.id).removeValue()
                    } else {
                        favRef.child(currentSong.id).setValue(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun updateMiniPlayerVisibility(started: Boolean) {
        binding.miniPlayerContainer.visibility = if (started) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(emptyList()) { selectedSong ->
            val index = filteredSongs.indexOfFirst { it.id == selectedSong.id }
            playEntirePlaylist(filteredSongs, index)
            openS4Activity(filteredSongs, index)
        }

        binding.favRecycler.apply {
            layoutManager = LinearLayoutManager(this@S3Activity)
            adapter = songAdapter
        }
    }

    private fun observeViewModel() {
        artistViewModel.artists.observe(this) { artistList = it }
        songViewModel.songs.observe(this) { songs ->
            filteredSongs = songs.filter { it.categoryIds.contains(categoryId) }
            songAdapter.updateList(filteredSongs)
        }
    }

    private fun playEntirePlaylist(songs: List<Song>, index: Int) {
        GlobalStorage.currentSongList = songs
        GlobalStorage.currentSongIndex = index

        val currentSong = songs[index]

        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_START_NEW
            putExtra(MusicService.EXTRA_URL, currentSong.url)
            putExtra(MusicService.EXTRA_TITLE, currentSong.title)
            putExtra(MusicService.EXTRA_ARTIST, currentSong.artistNames.joinToString(", "))
            putExtra(MusicService.EXTRA_IMAGE, currentSong.image)
        }

        ContextCompat.startForegroundService(this, intent)

        miniPlayerViewModel.setHasStartedPlaying(true)
        miniPlayerViewModel.setSong(currentSong)
        miniPlayerViewModel.setPlaying(true)

        songAdapter.setCurrentlyPlaying(currentSong.id)
        isPlayingAll = true
        binding.btnPlayAll.setImageResource(R.drawable.ic_pause)
    }

    private fun stopMusic() {
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_PAUSE
        }
        startService(intent)

        miniPlayerViewModel.setPlaying(false)
        songAdapter.setCurrentlyPlaying(null)
        isPlayingAll = false
        binding.btnPlayAll.setImageResource(R.drawable.ic_play)
    }

    private fun openS4Activity(songList: List<Song>, index: Int) {
        val intent = Intent(this, S4Activity::class.java).apply {
            putParcelableArrayListExtra("song_list", ArrayList(songList))
            putExtra("current_index", index)
            putExtra("from_mini_player", false)
            putExtra("source", "playlist")
        }
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(songChangeReceiver, IntentFilter("ACTION_UPDATE_S4"), Context.RECEIVER_NOT_EXPORTED)
        registerReceiver(playStateReceiver, IntentFilter("ACTION_UPDATE_PLAY_STATE"), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(songChangeReceiver)
        unregisterReceiver(playStateReceiver)
    }

    private val songChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_UPDATE_S4") {
                val playingId = intent.getStringExtra("id")
                songAdapter.setCurrentlyPlaying(playingId)
            }
        }
    }

    private val playStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_UPDATE_PLAY_STATE") {
                val isPlaying = intent.getBooleanExtra("is_playing", false)
                isPlayingAll = isPlaying
                binding.btnPlayAll.setImageResource(
                    if (isPlayingAll) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        }
    }
}
