package com.example.myapplication.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Log.d("NotificationReceiver", "🔥 Nhận được action: $action")

        val serviceIntent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        context?.startService(serviceIntent)
    }

}
