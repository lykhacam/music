package com.example.myapplication.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val musicIntent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        context?.startService(musicIntent)
    }
}
