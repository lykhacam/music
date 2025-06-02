package com.example.myapplication.View

import android.Manifest
import android.animation.ObjectAnimator
import android.content.*
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.myapplication.MyApplication
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityScreen4Binding
import com.example.myapplication.databinding.IncludeHeaderControlsBinding
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song
import com.example.myapplication.service.MusicService
import com.example.myapplication.bottomsheet.SongMenuBottomSheet
import com.example.myapplication.utils.HistoryUtils
import com.example.myapplication.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class S4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen4Binding
    private val viewModel: S4ViewModel by viewModels()
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel
    private lateinit var headerBinding: IncludeHeaderControlsBinding
    private val REQUEST_RECORD_AUDIO_PERMISSION = 1001

    private var songList: ArrayList<Song> = arrayListOf()
    private var currentIndex = 0
    private lateinit var originalList: List<Song>
    private var isShuffle = false

    private lateinit var rotationAnimator: ObjectAnimator
    private var repeatMode = RepeatMode.NONE

    private var visualizer: Visualizer? = null

    private enum class RepeatMode { NONE, ALL, ONE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        getIntentData()
        setupUI()
        checkAndRequestBatteryOptimization()
        checkAudioPermission()

        if (songList.isNotEmpty()) {
            val fromMini = intent.getBooleanExtra("from_mini_player", false)
            if (fromMini) {
                updateUI(songList[currentIndex])
                requestSessionIdFromService()
                requestPlayStateFromService()
                miniPlayerViewModel.setHasStartedPlaying(true)
            } else {
                playSong(currentIndex)
            }
        }
    }
    private fun checkAndRequestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("PowerState", "Cannot request battery optimization: ${e.message}")
                }
            }
        }
    }

    private fun initViewModel() {
        miniPlayerViewModel = ViewModelProvider(
            applicationContext as MyApplication,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]
    }

    private fun getIntentData() {
        songList = intent.getParcelableArrayListExtra("song_list") ?: arrayListOf()
        currentIndex = intent.getIntExtra("current_index", 0)
        originalList = songList.map { it.copy() }
    }

    private fun setupUI() {
        headerBinding = IncludeHeaderControlsBinding.bind(binding.root.findViewById(R.id.iclHctl))
        setupHeaderControls()
        setupRotation()
        setupControls()
        observeViewModel()
    }

    private fun setupHeaderControls() {
        headerBinding.btnBack.setOnClickListener { finish() }
        headerBinding.btnMenu.setOnClickListener {
            val song = songList[currentIndex]
            SongMenuBottomSheet(
                context = this,
                song = song,
                onLike = {
                    handleLikeToggle(song)
                },
                onDownload = {
                    songList[currentIndex].isDownloaded = true
                }
            ).show(supportFragmentManager, "song_menu")
        }
    }

    private fun handleLikeToggle(song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance(
            "https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("users").child(uid).child("favorites")

        favRef.child(song.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    favRef.child(song.id).removeValue()
                    binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_heart_image, 0)
                    Toast.makeText(this@S4Activity, "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show()
                } else {
                    favRef.child("placeholder").removeValue()
                    favRef.child(song.id).setValue(true)
                    binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_heart_full, 0)
                    Toast.makeText(this@S4Activity, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@S4Activity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRotation() {
        rotationAnimator = ObjectAnimator.ofFloat(binding.songImage, View.ROTATION, 0f, 360f).apply {
            duration = 10000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun setupControls() {
        binding.btnPlay.setOnClickListener {
            val playing = viewModel.isPlaying.value ?: false
            sendActionToService(if (playing) MusicService.ACTION_PAUSE else MusicService.ACTION_PLAY)
            viewModel.togglePlayPause()
            miniPlayerViewModel.setPlaying(!playing)
        }

        binding.btnSkNext.setOnClickListener { playNext() }
        binding.btnSkBack.setOnClickListener { playPrevious() }
        binding.cbLike.setOnClickListener {
            handleLikeToggle(songList[currentIndex])
        }

        binding.sbPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = formatTime(progress)
            }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { sendSeekToService(it.progress) }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
        })

        binding.btnRandom.setOnClickListener {
            isShuffle = !isShuffle
            val color = if (isShuffle) R.color.green else R.color.gray
            binding.btnRandom.setColorFilter(ContextCompat.getColor(this, color))
            val currentSong = songList[currentIndex]
            songList = if (isShuffle) ArrayList(originalList.shuffled()) else ArrayList(originalList.map { it.copy() })
            currentIndex = songList.indexOfFirst { it.id == currentSong.id }.takeIf { it >= 0 } ?: 0
            GlobalStorage.currentSongList = songList
            GlobalStorage.currentSongIndex = currentIndex
        }

        binding.btnRepeat.setOnClickListener {
            repeatMode = when (repeatMode) {
                RepeatMode.NONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.NONE
            }
            updateRepeatButtonUI()
        }
    }

    private fun updateRepeatButtonUI() {
        val iconRes = when (repeatMode) {
            RepeatMode.NONE -> R.color.gray
            RepeatMode.ALL -> R.color.blue
            RepeatMode.ONE -> R.color.green
        }
        binding.btnRepeat.setImageResource(iconRes)
    }

    private fun observeViewModel() {
        viewModel.isPlaying.observe(this) {
            binding.btnPlay.setImageResource(if (it) R.drawable.pause else R.drawable.play)
        }
    }

    private fun playSong(index: Int) {
        val song = songList[index]
        updateUI(song)
        GlobalStorage.currentSongList = songList
        GlobalStorage.currentSongIndex = index
        miniPlayerViewModel.setSong(song)
        miniPlayerViewModel.setPlaying(true)
        miniPlayerViewModel.setHasStartedPlaying(true)
        startService(Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_START_NEW
            putExtra(MusicService.EXTRA_TITLE, song.title)
            putExtra(MusicService.EXTRA_ARTIST, song.artistNames.joinToString(", "))
            putExtra(MusicService.EXTRA_IMAGE, song.image)
            putExtra(MusicService.EXTRA_URL, song.url)
        })
        viewModel.setPlayingOnlyUI(true)
        updateRecentlyPlayed(song.id)
    }

    private fun updateRecentlyPlayed(songId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid).child("recentlyPlayed")

        val currentTime = System.currentTimeMillis()

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val map = currentData.value as? Map<String, Long> ?: emptyMap()
                val updatedMap = map.toMutableMap()
                updatedMap[songId] = currentTime
                val sorted = updatedMap.entries.sortedByDescending { it.value }.take(3)
                currentData.value = sorted.associate { it.key to it.value }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    Log.e("updateRecentlyPlayed", "Failed: ${error.message}")
                } else {
                    Log.d("updateRecentlyPlayed", "Recently played updated")
                }
            }
        })
    }

    private fun playNext() {
        saveHistoryBeforeSwitch()
        currentIndex = when (repeatMode) {
            RepeatMode.ONE -> currentIndex
            else -> (currentIndex + 1) % songList.size
        }
        playSong(currentIndex)
    }

    private fun playPrevious() {
        saveHistoryBeforeSwitch()
        currentIndex = when (repeatMode) {
            RepeatMode.ONE -> currentIndex
            else -> if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        }
        playSong(currentIndex)
    }

    private fun handleCompletion() {
        saveHistoryBeforeSwitch()
        when (repeatMode) {
            RepeatMode.ONE -> playSong(currentIndex)
            RepeatMode.ALL -> playNext()
            RepeatMode.NONE -> {
                if (currentIndex < songList.lastIndex) {
                    currentIndex += 1
                    playSong(currentIndex)
                } else {
                    viewModel.setPlayingOnlyUI(false)
                    miniPlayerViewModel.setPlaying(false)
                    sendActionToService(MusicService.ACTION_PAUSE)
                }
            }
        }
    }

    private fun saveHistoryBeforeSwitch() {
        val currentSong = songList.getOrNull(currentIndex) ?: return
        val progress = binding.sbPlay.progress.toFloat()
        val duration = binding.sbPlay.max.takeIf { it > 0 } ?: return
        val percentPlayed = ((progress / duration) * 100).toInt()

        Log.d("TestHistory", "➡️ saveListeningHistory: ${currentSong.title}")
        HistoryUtils.saveListeningHistory(currentSong, percentPlayed, duration)
    }



    private fun updateUI(song: Song) {
        binding.songTitle.text = song.title
        binding.songArtist.text = song.artistNames.joinToString(", ")
        Glide.with(this).load(song.image).circleCrop().into(binding.songImage)
        checkIfSongIsFavorite(song)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun sendSeekToService(position: Int) {
        startService(Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK_TO
            putExtra(MusicService.EXTRA_SEEK_POSITION, position)
        })
    }

    private fun sendActionToService(action: String) {
        startService(Intent(this, MusicService::class.java).apply { this.action = action })
    }

    private fun requestSessionIdFromService() {
        sendActionToService(MusicService.ACTION_REQUEST_SESSION_ID)
    }

    private fun requestPlayStateFromService() {
        sendActionToService(MusicService.ACTION_REQUEST_UPDATE_MINI_PLAYER)
    }

    private fun setupVisualizer(audioSessionId: Int) {
        releaseVisualizer()
        visualizer = Visualizer(audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}
                override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                    fft?.let { binding.visualizerView.updateFFT(it) }
                }
            }, Visualizer.getMaxCaptureRate() / 2, false, true)
            enabled = true
        }
    }

    private fun releaseVisualizer() {
        visualizer?.release()
        visualizer = null
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicService.BROADCAST_POSITION -> updateSeekbar(intent)
                MusicService.BROADCAST_SESSION_ID -> {
                    val sessionId = intent.getIntExtra("session_id", -1)
                    if (sessionId != -1) setupVisualizer(sessionId)
                }
                MusicService.BROADCAST_COMPLETE -> handleCompletion()
                "ACTION_UPDATE_PLAY_STATE" -> updatePlayButton(intent)
                "ACTION_UPDATE_S4" -> updateUIFromBroadcast(intent)
            }
        }
    }

    private fun updateSeekbar(intent: Intent) {
        val current = intent.getIntExtra("current_position", 0)
        val duration = intent.getIntExtra("duration", 0)
        binding.sbPlay.max = duration
        binding.sbPlay.progress = current
        binding.tvCurrentTime.text = formatTime(current)
        binding.tvTotalTime.text = formatTime(duration)
    }

    private fun updatePlayButton(intent: Intent) {
        val isPlaying = intent.getBooleanExtra("is_playing", false)
        viewModel.setPlayingOnlyUI(isPlaying)
    }

    private fun updateUIFromBroadcast(intent: Intent) {
        currentIndex = GlobalStorage.currentSongIndex
        binding.songTitle.text = intent.getStringExtra("title") ?: ""
        binding.songArtist.text = intent.getStringExtra("artist") ?: ""
        Glide.with(this).load(intent.getStringExtra("image") ?: "").circleCrop().into(binding.songImage)
    }

    private fun checkIfSongIsFavorite(song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users").child(uid).child("favorites")

        favRef.child(song.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val icon = if (snapshot.exists()) R.drawable.ic_heart_full else R.drawable.ic_heart_image
                binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(MusicService.BROADCAST_POSITION)
            addAction(MusicService.BROADCAST_SESSION_ID)
            addAction(MusicService.BROADCAST_COMPLETE)
            addAction("ACTION_UPDATE_PLAY_STATE")
            addAction("ACTION_UPDATE_S4")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        rotationAnimator.cancel()
        releaseVisualizer()
        super.onDestroy()
    }

    private fun checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Đã cấp quyền ghi âm", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền ghi âm để hiển thị visualizer", Toast.LENGTH_LONG).show()
            }
        }
    }
}
