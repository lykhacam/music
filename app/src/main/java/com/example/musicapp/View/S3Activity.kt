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
import com.example.myapplication.fragment.MiniPlayerFragment
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song
import com.example.myapplication.service.MusicService
import com.example.myapplication.viewmodel.MiniPlayerViewModel
import com.example.myapplication.viewmodel.MiniPlayerViewModelFactory
import com.example.myapplication.viewmodel.SongViewModel

class S3Activity : AppCompatActivity() {

    private val songViewModel: SongViewModel by viewModels()
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel

    private lateinit var binding: ActivityScreen3Binding
    private lateinit var songAdapter: SongAdapter

    private var filteredSongs: List<Song> = emptyList()
    private var playlistId: String = ""
    private var isPlayingAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        miniPlayerViewModel = ViewModelProvider(
            application as ViewModelStoreOwner,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]

        setupBasicControls()

        supportFragmentManager.beginTransaction()
            .replace(binding.miniPlayerContainer.id, MiniPlayerFragment())
            .commit()

        miniPlayerViewModel.hasStartedPlaying.observe(this) {
            updateMiniPlayerVisibility(it)
        }

        playlistId = intent.getStringExtra("playlist_id") ?: ""
        val playlistImage = intent.getStringExtra("playlist_image")
        playlistImage?.let {
            Glide.with(this).load(it).into(binding.imgPlaylist)
        }

        setupRecyclerView()
        observeViewModel()

        binding.btnPlayAll.setOnClickListener {
            if (filteredSongs.isEmpty()) return@setOnClickListener

            if (isPlayingAll) {
                stopMusic()
            } else {
                val currentIndex = GlobalStorage.currentSongIndex
                if (currentIndex in filteredSongs.indices && GlobalStorage.currentSongList == filteredSongs) {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, MusicService::class.java).apply {
                            action = MusicService.ACTION_PLAY
                        }
                    )
                    miniPlayerViewModel.setPlaying(true)
                    isPlayingAll = true
                    binding.btnPlayAll.setImageResource(R.drawable.ic_pause)
                } else {
                    playEntirePlaylist(filteredSongs, 0)
                }
            }
        }
    }

    private fun setupBasicControls() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
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
        songViewModel.songs.observe(this) { songs ->
            filteredSongs = songs.filter { it.playlistIds.contains(playlistId) }
            songAdapter.updateList(filteredSongs)

            val playingId = GlobalStorage.currentSongList
                .getOrNull(GlobalStorage.currentSongIndex)
                ?.id

            val matched = filteredSongs.any { it.id == playingId }

            songAdapter.setCurrentlyPlaying(if (matched) playingId else null)
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
        startService(Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_PAUSE
        })

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

    override fun onResume() {
        super.onResume()

        startService(Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_UPDATE_MINI_PLAYER
        })

        val currentId = GlobalStorage.currentSongList
            .getOrNull(GlobalStorage.currentSongIndex)
            ?.id

        val matched = filteredSongs.any { it.id == currentId }
        songAdapter.setCurrentlyPlaying(if (matched) currentId else null)
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(playStateReceiver, IntentFilter("ACTION_UPDATE_PLAY_STATE"), Context.RECEIVER_NOT_EXPORTED)
        registerReceiver(songUpdateReceiver, IntentFilter("ACTION_UPDATE_S4"), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(playStateReceiver)
        unregisterReceiver(songUpdateReceiver)
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

    private val songUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_UPDATE_S4") {
                val newPlayingId = intent.getStringExtra("id")
                val matched = filteredSongs.any { it.id == newPlayingId }
                songAdapter.setCurrentlyPlaying(if (matched) newPlayingId else null)
            }
        }
    }
}
