package com.example.myapplication.View

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.graphics.Color
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
import com.example.myapplication.bottomsheet.RepeatShuffleBottomSheet
import com.example.myapplication.bottomsheet.SongMenuBottomSheet
import com.example.myapplication.databinding.ActivityScreen4Binding
import com.example.myapplication.databinding.IncludeHeaderControlsBinding
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.helper.*
import com.example.myapplication.model.Song
import com.example.myapplication.receiver.S4BroadcastReceiver
import com.example.myapplication.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class S4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen4Binding
    private lateinit var headerBinding: IncludeHeaderControlsBinding

    private val viewModel: S4ViewModel by viewModels()
    private lateinit var miniPlayerViewModel: MiniPlayerViewModel

    private lateinit var visualizerHelper: AudioVisualizerHelper
    private lateinit var s4Receiver: S4BroadcastReceiver
    private lateinit var rotationAnimator: ObjectAnimator

    private val REQUEST_RECORD_AUDIO_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreen4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMiniPlayerViewModel()
        getIntentData()
        setupUI()
        observeViewModel()

        checkAndRequestBatteryOptimization()
        checkAudioPermission()

        val fromMini = intent.getBooleanExtra("from_mini_player", false)
        if (fromMini) {
            viewModel.currentSong.value?.let { updateUI(it) }
            MusicCommandHelper.requestSessionId(this)
            MusicCommandHelper.requestUpdateMiniPlayer(this)
            miniPlayerViewModel.setHasStartedPlaying(true)
        } else {
            playSong(viewModel.currentIndex.value ?: 0)
        }
    }

    private fun setupMiniPlayerViewModel() {
        miniPlayerViewModel = ViewModelProvider(
            applicationContext as MyApplication,
            MiniPlayerViewModelFactory(application)
        )[MiniPlayerViewModel::class.java]
    }

    private fun getIntentData() {
        val songList = intent.getParcelableArrayListExtra<Song>("song_list") ?: arrayListOf()
        val index = intent.getIntExtra("current_index", 0)
        viewModel.initSongs(songList, index)
    }

    private fun setupUI() {
        headerBinding = IncludeHeaderControlsBinding.bind(binding.root.findViewById(R.id.iclHctl))
        visualizerHelper = AudioVisualizerHelper(binding.visualizerView)
        setupHeader()
        setupRotation()
        setupControls()
    }

    private fun setupHeader() {
        headerBinding.btnBack.setOnClickListener { finish() }

        headerBinding.btnMenu.setOnClickListener {
            val song = viewModel.getCurrentSongData() ?: return@setOnClickListener
            SongMenuBottomSheet(
                context = this,
                song = song,
                onLike = { handleLikeToggle(song) },
                onDownload = {
                    song.isDownloaded = true
                    Toast.makeText(this, "Đã tải bài hát", Toast.LENGTH_SHORT).show()
                },
                onRemove = {
                    song.isDownloaded = false
                    Toast.makeText(this, "Đã xoá bài hát khỏi danh sách tải", Toast.LENGTH_SHORT).show()
                }
            ).show(supportFragmentManager, "song_menu")

        }
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
            MusicCommandHelper.sendPlay(this)
            viewModel.setPlayingOnlyUI(true)
            togglePlayPauseButtons(true)
            miniPlayerViewModel.setPlaying(true)
        }

        binding.btnPause.setOnClickListener {
            MusicCommandHelper.sendPause(this)
            viewModel.setPlayingOnlyUI(false)
            togglePlayPauseButtons(false)
            miniPlayerViewModel.setPlaying(false)
        }

        binding.btnSkNext.setOnClickListener { playNext() }
        binding.btnSkBack.setOnClickListener { playPrevious() }

        binding.cbLike.setOnClickListener {
            viewModel.currentSong.value?.let { handleLikeToggle(it) }
        }

        binding.sbPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) binding.tvCurrentTime.text = formatTime(progress)
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                sb?.let { MusicCommandHelper.sendSeek(this@S4Activity, it.progress) }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
        })

        binding.btnRepeat.setOnClickListener {
            val songList = viewModel.songList.value ?: return@setOnClickListener
            RepeatShuffleBottomSheet(
                context = this,
                mode = RepeatShuffleBottomSheet.Mode.REPEAT,
                songList = songList,
                onModeSelected = { mode ->
                    when (mode) {
                        "none" -> viewModel.setRepeatMode(S4ViewModel.RepeatMode.NONE)
                        "one" -> viewModel.setRepeatMode(S4ViewModel.RepeatMode.ONE)
                        "all" -> viewModel.setRepeatMode(S4ViewModel.RepeatMode.ALL)
                    }
                    updateRepeatButtonUI()
                }
            ).show(supportFragmentManager, "repeat_bottomsheet")
        }

        binding.btnRandom.setOnClickListener {
            val songList = viewModel.songList.value ?: return@setOnClickListener
            RepeatShuffleBottomSheet(
                context = this,
                mode = RepeatShuffleBottomSheet.Mode.SHUFFLE,
                songList = songList,
                onModeSelected = { mode ->
                    val isShuffle = mode == "on"
                    viewModel.setShuffleMode(isShuffle)

                    val iconRes = if (isShuffle) {
                        R.drawable.ic_shuffle_on
                    } else {
                        R.drawable.ic_shuffle_off
                    }
                    binding.btnRandom.setImageResource(iconRes)
                }
            ).show(supportFragmentManager, "shuffle_bottomsheet")
        }


    }

    private fun playSong(index: Int) {
        viewModel.playSong(index)
        val song = viewModel.getCurrentSongData() ?: return
        updateUI(song)
        incrementSongCount(song.id)

        GlobalStorage.currentSongList = viewModel.songList.value!!
        GlobalStorage.currentSongIndex = index

        miniPlayerViewModel.setSong(song)
        miniPlayerViewModel.setPlaying(true)
        miniPlayerViewModel.setHasStartedPlaying(true)

        MusicCommandHelper.startNewSong(this, song)

        viewModel.setPlayingOnlyUI(true)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.updateRecentlyPlayed(uid, song.id)
    }

    private fun playNext() {
        saveHistoryBeforeSwitch()
        viewModel.playNext()
        playSong(viewModel.currentIndex.value ?: 0)
    }

    private fun playPrevious() {
        saveHistoryBeforeSwitch()
        viewModel.playPrevious()
        playSong(viewModel.currentIndex.value ?: 0)
    }

    private fun handleLikeToggle(song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.toggleFavorite(uid, song) { isLiked ->
            val icon = if (isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_image
            binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
            Toast.makeText(this, if (isLiked) "Đã thêm vào yêu thích" else "Đã xoá khỏi yêu thích", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRepeatButtonUI() {
        when (viewModel.repeatMode.value) {
            S4ViewModel.RepeatMode.ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_all)
            }

            S4ViewModel.RepeatMode.ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
            }

            else -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            }
        }
    }


    private fun saveHistoryBeforeSwitch() {
        val progress = binding.sbPlay.progress
        val duration = binding.sbPlay.max.takeIf { it > 0 } ?: return
        viewModel.saveListeningHistory(progress, duration)
    }

    private fun observeViewModel() {
        viewModel.currentSong.observe(this) { updateUI(it) }
        viewModel.isPlaying.observe(this) { isPlaying ->
            togglePlayPauseButtons(isPlaying)
        }
    }


    private fun updateUI(song: Song) {
        binding.songTitle.text = song.title
        binding.songArtist.text = song.artistNames.joinToString(", ")
        Glide.with(this).load(song.image).circleCrop().into(binding.songImage)
        checkIfSongIsFavorite(song)
    }

    private fun checkIfSongIsFavorite(song: Song) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/favorites/${song.id}")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val icon = if (snapshot.exists()) R.drawable.ic_heart_full else R.drawable.ic_heart_image
                    binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onStart() {
        super.onStart()
        s4Receiver = S4BroadcastReceiver(
            context = this,
            binding = binding,
            viewModel = viewModel,
            playNext = { playNext() },
            playSong = { index -> playSong(index) }
        )
        IntentFilter().apply {
            addAction("BROADCAST_POSITION")
            addAction("BROADCAST_SESSION_ID")
            addAction("BROADCAST_COMPLETE")
            addAction("ACTION_UPDATE_PLAY_STATE")
            addAction("ACTION_UPDATE_S4")
        }.also {
            LocalBroadcastManager.getInstance(this).registerReceiver(s4Receiver, it)
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(s4Receiver)
    }

    override fun onDestroy() {
        rotationAnimator.cancel()
        visualizerHelper.release()
        super.onDestroy()
    }

    fun setupVisualizer(sessionId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 trở lên
            visualizerHelper.setup(sessionId)
        } else {
            // Android 9 trở xuống
            val legacyView = binding.legacyVisualizerView
            legacyView.visibility = View.VISIBLE
            legacyView.setColor(ContextCompat.getColor(this, R.color.yellow))
            legacyView.setPlayer(sessionId)
        }
    }


    fun pauseMusicExternally() {
        miniPlayerViewModel.setPlaying(false)
        MusicCommandHelper.sendPause(this)
    }

    private fun checkAndRequestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    })
                } catch (_: Exception) {}
            }
        }
    }

    private fun checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && results.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Đã cấp quyền ghi âm", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Cần quyền ghi âm để hiển thị hiệu ứng nhịp nhạc", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }
    private fun togglePlayPauseButtons(isPlaying: Boolean) {
        binding.btnPlay.visibility = if (isPlaying) View.GONE else View.VISIBLE
        binding.btnPause.visibility = if (isPlaying) View.VISIBLE else View.GONE
    }

    private fun incrementSongCount(songId: String) {
        val dbRef = FirebaseDatabase.getInstance("https://appmusicrealtime-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("songs")
            .child(songId)
            .child("count")

        dbRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCount + 1
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    android.util.Log.e("Firebase", "Update count failed: ${error.message}")
                } else {
                    android.util.Log.d("Firebase", "Count updated successfully")
                }
            }
        })
    }

}
