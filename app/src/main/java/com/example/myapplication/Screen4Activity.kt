package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
class Screen4Activity : AppCompatActivity(){

    private var isPlaying = false
    private var  isLike = false
    private var currentProgress = 0
    private val totalTime = 194 // Ví dụ: 180 giây (3 phút)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var sbPlay: SeekBar
    private lateinit var btnPlay: ImageButton
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var imgLike: ImageButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sbPlay = findViewById(R.id.sbPlay)
        btnPlay = findViewById(R.id.btnPlay)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        imgLike = findViewById(R.id.imgLike)

        sbPlay.max = totalTime
        tvCurrentTime.text = formatTime(0)
        tvTotalTime.text = formatTime(totalTime)

        imgLike.setOnClickListener{
            if(isLike){
                unLike()
            }
            else{
                enterLike()
            }
        }
        // Xử lý khi nhấn nút Play/Pause
        btnPlay.setOnClickListener {
            if (isPlaying) {
                clickPause()
            } else {
                clickPlay()
            }
        }
    }
    private val updateRun = object : Runnable {
        override fun run() {
            if (isPlaying && currentProgress < totalTime) {
                currentProgress++
                sbPlay.progress = currentProgress
                tvCurrentTime.text = formatTime(currentProgress)
                handler.postDelayed(this, 1000)
            } else {
                clickPause()
            }
        }
    }

    private fun enterLike(){
        isLike = true
        imgLike.setImageResource(R.drawable.ic_heart_full)
    }
    private fun unLike(){
        isLike = false
        imgLike.setImageResource(R.drawable.ic_heart_image)
    }
    private fun clickPlay() {
        isPlaying = true
        btnPlay.setImageResource(R.drawable.pause)
        handler.post(updateRun)
    }

    private fun clickPause() {
        isPlaying = false
        btnPlay.setImageResource(R.drawable.play)
        handler.removeCallbacks(updateRun)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", minutes, sec)
    }
}