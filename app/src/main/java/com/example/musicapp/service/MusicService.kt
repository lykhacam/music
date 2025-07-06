package com.example.myapplication.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.View.S4Activity
import com.example.myapplication.global.GlobalStorage
import com.example.myapplication.model.Song
import com.example.myapplication.utils.HistoryUtils
import java.util.*

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var timer: Timer? = null
    private var isPlaying: Boolean = false
    private var currentSong: Song? = null
    private var lastPlayedSong: Song? = null
    private var lastPlayedDuration: Int = 0
    private var lastPlayedPosition: Int = 0

    companion object {
        const val ACTION_START_NEW = "ACTION_START_NEW"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SEEK_TO = "ACTION_SEEK_TO"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_REQUEST_UPDATE_MINI_PLAYER = "ACTION_REQUEST_UPDATE_MINI_PLAYER"
        const val ACTION_UPDATE_MINI_PLAYER = "ACTION_UPDATE_MINI_PLAYER"
        const val ACTION_REQUEST_SESSION_ID = "ACTION_REQUEST_SESSION_ID"

        const val BROADCAST_POSITION = "BROADCAST_POSITION"
        const val BROADCAST_SESSION_ID = "BROADCAST_SESSION_ID"
        const val BROADCAST_COMPLETE = "BROADCAST_COMPLETE"

        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_ARTIST = "EXTRA_ARTIST"
        const val EXTRA_IMAGE = "EXTRA_IMAGE"
        const val EXTRA_SEEK_POSITION = "EXTRA_SEEK_POSITION"

        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_NEW -> handleStartNew(intent)
            ACTION_PLAY -> resumeMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_SEEK_TO -> seekTo(intent.getIntExtra(EXTRA_SEEK_POSITION, 0))
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_REQUEST_UPDATE_MINI_PLAYER -> {
                sendCurrentSongToMiniPlayer()
                sendCurrentPlaybackPosition()
                sendPlayState()

                // ✅ Gửi lại ID bài hát hiện tại cho S3Activity
                currentSong?.let { song ->
                    sendBothBroadcast(Intent("ACTION_UPDATE_S4").apply {
                        putExtra("id", song.id)
                    })
                }
            }

            ACTION_REQUEST_SESSION_ID -> mediaPlayer?.audioSessionId?.let { sendSessionId(it) }
        }
        return START_NOT_STICKY
    }

    private fun sendBothBroadcast(intent: Intent) {
        sendBroadcast(intent)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun handleStartNew(intent: Intent) {
        val url = intent.getStringExtra(EXTRA_URL)
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val artist = intent.getStringExtra(EXTRA_ARTIST) ?: ""
        val image = intent.getStringExtra(EXTRA_IMAGE) ?: ""

        if (url != null) {
            currentSong = Song(
                id = UUID.randomUUID().toString(),
                title = title,
                artistNames = listOf(artist),
                image = image,
                url = url,
                duration = 0,
                categoryIds = listOf(category)
            )
            startNewSong(url)
        }
    }

    private fun startNewSong(url: String) {
        timer?.cancel()
        timer = null

        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                start()
                this@MusicService.isPlaying = true
                sendSessionId(audioSessionId)
                startSendingPosition()
                displayNotification()
                sendPlayState()
                sendCurrentSongToMiniPlayer()
                broadcastCurrentSongId()
            }
            setOnCompletionListener {
                currentSong?.let { song ->
                    Log.d("TestHistory", "➡️ Gọi saveListeningHistory: ${song.title}")
                    HistoryUtils.saveListeningHistory(song, duration, duration)
                } ?: Log.d("TestHistory", "⚠️ currentSong null tại onCompletion")
                sendBothBroadcast(Intent(BROADCAST_COMPLETE))
            }
            prepareAsync()
        }
    }

    private fun resumeMusic() {
        mediaPlayer?.start()
        isPlaying = true
        startSendingPosition()
        displayNotification()
        sendPlayState()
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
        displayNotification()
        sendPlayState()
    }

    private fun seekTo(positionInSeconds: Int) {
        mediaPlayer?.seekTo(positionInSeconds * 1000)
    }

    private fun sendSessionId(sessionId: Int) {
        sendBothBroadcast(Intent(BROADCAST_SESSION_ID).putExtra("session_id", sessionId))
    }

    private fun startSendingPosition() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            val currentPos = it.currentPosition / 1000
                            val duration = it.duration / 1000

                            lastPlayedSong = currentSong
                            lastPlayedPosition = currentPos
                            lastPlayedDuration = duration

                            sendBothBroadcast(Intent(BROADCAST_POSITION).apply {
                                putExtra("current_position", currentPos)
                                putExtra("duration", duration)
                            })

                            currentSong?.let { song ->
                                sendBothBroadcast(Intent("ACTION_UPDATE_S4").apply {
                                    putExtra("title", song.title)
                                    putExtra("artist", song.artistNames.joinToString(", "))
                                    putExtra("image", song.image)
                                    putExtra("url", song.url)
                                    putExtra("id", song.id)
                                    putExtra("current_position", currentPos)
                                    putExtra("duration", duration)
                                })
                            }
                        }
                    }
                } catch (e: IllegalStateException) {
                    cancel()
                }
            }
        }, 0, 1000)
    }

    private fun sendCurrentSongToMiniPlayer() {
        if (!isPlaying || currentSong == null) {
            Log.d("MusicService", "🚫 Không gửi MiniPlayer vì không có bài hát đang phát.")
            return
        }

        sendBothBroadcast(Intent(ACTION_UPDATE_MINI_PLAYER).apply {
            putExtra("title", currentSong!!.title)
            putExtra("artist", currentSong!!.artistNames.joinToString(", "))
            putExtra("image", currentSong!!.image)
            putExtra("url", currentSong!!.url)
        })
    }


    private fun sendCurrentPlaybackPosition() {
        mediaPlayer?.let {
            sendBothBroadcast(Intent(BROADCAST_POSITION).apply {
                putExtra("current_position", it.currentPosition / 1000)
                putExtra("duration", it.duration / 1000)
            })
        }
    }

    private fun sendPlayState() {
        sendBothBroadcast(Intent("ACTION_UPDATE_PLAY_STATE").putExtra("is_playing", isPlaying))
    }

    override fun onDestroy() {
        timer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(true)

        // 🧹 Reset dữ liệu toàn cục
        GlobalStorage.currentSongList = emptyList()
        GlobalStorage.currentSongIndex = -1
        currentSong = null
        isPlaying = false

        // 📣 Gửi lệnh ẩn MiniPlayer
        sendBothBroadcast(Intent("ACTION_STOP_MINI_PLAYER"))

        super.onDestroy()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo nhạc đang phát"
                setShowBadge(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun displayNotification() {
        val song = currentSong ?: return
        Thread {
            val bitmap = try {
                Glide.with(this)
                    .asBitmap()
                    .load(song.image)
                    .submit(512, 512)
                    .get()
            } catch (e: Exception) {
                BitmapFactory.decodeResource(resources, R.drawable.img)
            }
            val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(song.title)
                .setContentText(song.artistNames.joinToString(", "))
                .setSmallIcon(R.drawable.ic_music_note)
                .setLargeIcon(bitmap)
                .setContentIntent(getContentIntent())
                .addAction(R.drawable.ic_previous, "Previous", getBroadcastIntent(ACTION_PREVIOUS))
                .addAction(playPauseIcon, "Play/Pause", getBroadcastIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY))
                .addAction(R.drawable.ic_next, "Next", getBroadcastIntent(ACTION_NEXT))
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }.start()
    }

    private fun getBroadcastIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(this, action.hashCode(), intent, flags)
    }


    private fun getContentIntent(): PendingIntent {
        val intent = Intent(this, S4Activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putParcelableArrayListExtra("song_list", ArrayList(GlobalStorage.currentSongList))
            putExtra("current_index", GlobalStorage.currentSongIndex)
            putExtra("from_mini_player", true)
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun playNext() {
        Log.d("TestHistory", "➡️ playNext() được gọi")
        lastPlayedSong?.let { song ->
            HistoryUtils.saveListeningHistory(song, lastPlayedPosition, lastPlayedDuration)
        }
        val list = GlobalStorage.currentSongList
        val nextIndex = (GlobalStorage.currentSongIndex + 1).let { if (it >= list.size) 0 else it }
        GlobalStorage.currentSongIndex = nextIndex
        currentSong = list[nextIndex]
        startNewSong(currentSong!!.url)
    }

    private fun playPrevious() {
        Log.d("TestHistory", "➡️ playPrevious() được gọi")
        lastPlayedSong?.let { song ->
            HistoryUtils.saveListeningHistory(song, lastPlayedPosition, lastPlayedDuration)
        }
        val list = GlobalStorage.currentSongList
        val previousIndex = (GlobalStorage.currentSongIndex - 1).let { if (it < 0) list.size - 1 else it }
        GlobalStorage.currentSongIndex = previousIndex
        currentSong = list[previousIndex]
        startNewSong(currentSong!!.url)
    }
    private fun broadcastCurrentSongId() {
        val currentIndex = GlobalStorage.currentSongIndex
        val song = GlobalStorage.currentSongList.getOrNull(currentIndex) ?: return

        val intent = Intent("ACTION_UPDATE_S4").apply {
            putExtra("id", song.id)
        }
        sendBroadcast(intent)
        Log.d("MusicService", "📡 Broadcast sent from MusicService: id=${song.id}")
    }

}
