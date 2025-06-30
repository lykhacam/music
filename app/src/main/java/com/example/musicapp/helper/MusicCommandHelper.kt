package com.example.myapplication.helper

import android.content.Context
import android.content.Intent
import com.example.myapplication.model.Song
import com.example.myapplication.service.MusicService

object MusicCommandHelper {

    fun startNewSong(context: Context, song: Song) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_START_NEW
            putExtra(MusicService.EXTRA_TITLE, song.title)
            putExtra(MusicService.EXTRA_ARTIST, song.artistNames.joinToString(", "))
            putExtra(MusicService.EXTRA_IMAGE, song.image)
            putExtra(MusicService.EXTRA_URL, song.url)
        }
        context.startService(intent)
    }

    fun sendPlay(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY
        })
    }

    fun sendPause(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PAUSE
        })
    }

    fun sendSeek(context: Context, position: Int) {
        context.startService(Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK_TO
            putExtra(MusicService.EXTRA_SEEK_POSITION, position)
        })
    }

    fun requestSessionId(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_SESSION_ID
        })
    }

    fun requestUpdateMiniPlayer(context: Context) {
        context.startService(Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_REQUEST_UPDATE_MINI_PLAYER
        })
    }
}
