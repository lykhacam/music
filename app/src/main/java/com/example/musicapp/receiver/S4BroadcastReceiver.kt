package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.example.myapplication.View.S4Activity
import com.example.myapplication.databinding.ActivityScreen4Binding
import com.example.myapplication.service.MusicService
import com.example.myapplication.viewmodel.S4ViewModel

class S4BroadcastReceiver(
    private val context: Context,
    private val binding: ActivityScreen4Binding,
    private val viewModel: S4ViewModel,
    private val playNext: () -> Unit,
    private val playSong: (Int) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        when (intent?.action) {
            MusicService.BROADCAST_POSITION -> {
                val current = intent.getIntExtra("current_position", 0)
                val duration = intent.getIntExtra("duration", 0)
                binding.sbPlay.max = duration
                binding.sbPlay.progress = current
                binding.tvCurrentTime.text = formatTime(current)
                binding.tvTotalTime.text = formatTime(duration)
            }

            MusicService.BROADCAST_SESSION_ID -> {
                val sessionId = intent.getIntExtra("session_id", -1)
                if (sessionId != -1) {
                    (context as? S4Activity)?.setupVisualizer(sessionId)
                }
            }

            MusicService.BROADCAST_COMPLETE -> {
                when (viewModel.repeatMode.value ?: S4ViewModel.RepeatMode.NONE) {
                    S4ViewModel.RepeatMode.ONE -> playSong(viewModel.currentIndex.value ?: 0)
                    S4ViewModel.RepeatMode.ALL -> playNext()
                    S4ViewModel.RepeatMode.NONE -> {
                        if ((viewModel.currentIndex.value ?: 0) < (viewModel.songList.value?.lastIndex ?: 0)) {
                            playNext()
                        } else {
                            viewModel.setPlayingOnlyUI(false)
                            (context as? S4Activity)?.pauseMusicExternally()
                        }
                    }
                }
            }

            "ACTION_UPDATE_PLAY_STATE" -> {
                val isPlaying = intent.getBooleanExtra("is_playing", false)
                viewModel.setPlayingOnlyUI(isPlaying)
            }

            "ACTION_UPDATE_S4" -> {
                val title = intent.getStringExtra("title") ?: ""
                val artist = intent.getStringExtra("artist") ?: ""
                val image = intent.getStringExtra("image") ?: ""
                binding.songTitle.text = title
                binding.songArtist.text = artist
                Glide.with(context).load(image).circleCrop().into(binding.songImage)
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
