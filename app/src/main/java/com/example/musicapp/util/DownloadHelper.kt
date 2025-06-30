package com.example.myapplication.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.myapplication.model.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object DownloadHelper {

    fun downloadAndSaveSong(context: Context, song: Song, onSuccess: (File) -> Unit, onError: (Exception) -> Unit) {
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(song.url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Tải thất bại")

                val fileName = song.title.replace(Regex("[^a-zA-Z0-9]"), "_") + ".mp3"
                val file = File(
                    context.getExternalFilesDir("DownloadedSongs"),
                    fileName
                )

                val sink = FileOutputStream(file)
                sink.use {
                    it.write(response.body?.bytes())
                }

                Log.d("DownloadDebug", "✅ File lưu tại: ${file.absolutePath}")
                saveToFirebase(song, file.absolutePath)

                onSuccess(file)

            } catch (e: Exception) {
                Log.e("DownloadDebug", "❌ Lỗi tải bài hát: ${e.message}")
                onError(e)
            }
        }.start()
    }

    private fun saveToFirebase(song: Song, localPath: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("users/$uid/downloads/${song.id}")

        val downloadInfo = mapOf(
            "title" to song.title,
            "artistNames" to song.artistNames,
            "image" to song.image,
            "duration" to song.duration,
            "localPath" to localPath
        )

        ref.setValue(downloadInfo)
        Log.d("DownloadDebug", "🔥 Đã lưu vào Firebase")
    }
}
