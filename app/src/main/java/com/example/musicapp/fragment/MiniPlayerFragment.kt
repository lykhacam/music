package com.example.myapplication.fragment

import android.animation.ObjectAnimator
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.View.S4Activity
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song
import com.example.myapplication.service.MusicService
import com.example.myapplication.viewmodel.MiniPlayerViewModel

class MiniPlayerFragment : Fragment() {

    private lateinit var imgSong: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var progressBar: SeekBar

    private val miniPlayerViewModel: MiniPlayerViewModel by viewModels()
    private var currentTitle: String? = null
    private var currentArtist: String? = null
    private var currentImage: String? = null
    private var currentUrl: String? = null
    private var isPlaying: Boolean = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicService.ACTION_UPDATE_MINI_PLAYER -> {
                    currentTitle = intent.getStringExtra("title")
                    currentArtist = intent.getStringExtra("artist")
                    currentImage = intent.getStringExtra("image")
                    currentUrl = intent.getStringExtra("url")
                    updateMiniPlayerUI()
                }
                MusicService.BROADCAST_POSITION -> {
                    val position = intent.getIntExtra("current_position", 0)
                    val duration = intent.getIntExtra("duration", 0)
                    progressBar.max = duration
                    progressBar.progress = position
                }
                MusicService.BROADCAST_COMPLETE -> {
                    progressBar.progress = 0
                }
                "ACTION_UPDATE_PLAY_STATE" -> {
                    isPlaying = intent.getBooleanExtra("is_playing", false)
                    btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_mini_player, container, false).apply {
            imgSong = findViewById(R.id.imgSongMini)
            tvTitle = findViewById(R.id.tvTitleMini)
            tvArtist = findViewById(R.id.tvArtistMini)
            btnPlayPause = findViewById(R.id.btnPlayPauseMini)
            btnNext = findViewById(R.id.btnNextMini)
            btnPrevious = findViewById(R.id.btnPreviousMini)
            progressBar = findViewById(R.id.progressMini)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            val songList = GlobalStorage.currentSongList
            val index = GlobalStorage.currentSongIndex
            if (!currentUrl.isNullOrEmpty() && songList.isNotEmpty() && index in songList.indices) {
                val intent = Intent(requireContext(), S4Activity::class.java).apply {
                    putParcelableArrayListExtra("song_list", ArrayList(songList))
                    putExtra("current_index", index)
                    putExtra("from_mini_player", true)
                }
                startActivity(intent)
            }
        }

        btnPlayPause.setOnClickListener {
            val action = if (isPlaying) MusicService.ACTION_PAUSE else MusicService.ACTION_PLAY
            val intent = Intent(requireContext(), MusicService::class.java).apply { this.action = action }
            requireContext().startService(intent)
            isPlaying = !isPlaying
            btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        btnNext.setOnClickListener {
            val songList = GlobalStorage.currentSongList
            val currentIndex = GlobalStorage.currentSongIndex
            if (songList.isEmpty()) return@setOnClickListener

            val nextIndex = (currentIndex + 1) % songList.size
            val nextSong = songList[nextIndex]
            GlobalStorage.currentSongIndex = nextIndex

            playSong(nextSong)
        }

        btnPrevious.setOnClickListener {
            val songList = GlobalStorage.currentSongList
            val currentIndex = GlobalStorage.currentSongIndex
            if (songList.isEmpty()) return@setOnClickListener

            val prevIndex = if (currentIndex - 1 < 0) songList.lastIndex else currentIndex - 1
            val prevSong = songList[prevIndex]
            GlobalStorage.currentSongIndex = prevIndex

            playSong(prevSong)
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val newPos = seekBar?.progress ?: 0
                val intent = Intent(requireContext(), MusicService::class.java).apply {
                    action = MusicService.ACTION_SEEK_TO
                    putExtra(MusicService.EXTRA_SEEK_POSITION, newPos)
                }
                requireContext().startService(intent)
            }
        })

        miniPlayerViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            val anim = ObjectAnimator.ofFloat(view, "alpha", view.alpha, if (playing) 1f else 0f).apply {
                duration = 300
            }
            anim.start()
            view.visibility = if (playing) View.VISIBLE else View.GONE
        }
    }

    private fun playSong(song: Song) {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            action = MusicService.ACTION_START_NEW
            putExtra(MusicService.EXTRA_URL, song.url)
            putExtra(MusicService.EXTRA_TITLE, song.title)
            putExtra(MusicService.EXTRA_ARTIST, song.artistNames.joinToString(", "))
            putExtra(MusicService.EXTRA_IMAGE, song.image)
        }
        requireContext().startService(intent)

        // ✅ Gửi broadcast để S3Activity highlight
        val broadcastIntent = Intent("ACTION_UPDATE_S4").apply {
            putExtra("id", song.id)
        }
        requireContext().sendBroadcast(broadcastIntent)

        Log.d("MiniPlayerFragment", "🎯 Broadcast sent: ${song.title} (id=${song.id})")
    }

    private fun updateMiniPlayerUI() {
        tvTitle.text = currentTitle ?: ""
        tvArtist.text = currentArtist ?: ""
        if (!currentImage.isNullOrEmpty()) {
            Glide.with(this).load(currentImage).into(imgSong)
        } else {
            imgSong.setImageResource(R.drawable.img)
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(MusicService.ACTION_UPDATE_MINI_PLAYER)
            addAction(MusicService.BROADCAST_POSITION)
            addAction(MusicService.BROADCAST_COMPLETE)
            addAction("ACTION_UPDATE_PLAY_STATE")
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                requireContext().registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            Log.e("MiniPlayerFragment", "❌ Lỗi đăng ký receiver: ${e.message}")
        }

        requestCurrentSongInfo()
    }

    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e("MiniPlayerFragment", "❌ Lỗi hủy receiver: ${e.message}")
        }
    }

    private fun requestCurrentSongInfo() {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_UPDATE_MINI_PLAYER
        }
        requireContext().startService(intent)
    }
}
