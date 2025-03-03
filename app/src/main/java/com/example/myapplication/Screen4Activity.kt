package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask

class Screen4Activity : AppCompatActivity(){

    private var isPlaying = false
    private var  isLike = false
    private var currentProgress = 0
    private val TOTAL_TIME = 194 // Ví dụ: 180 giây (3 phút)
    private var timer: Timer? = null
//    private val handler = Handler(Looper.getMainLooper())


//    private lateinit val sbPlay by lazy { finishActivity(R.id.sbPlay) } //chỉ dùng với var?
//    private lateinit var btnPlay: ImageButton
//    private lateinit var tvCurrentTime: TextView
//    private lateinit var tvTotalTime: TextView
//    private lateinit var imgLike: ImageButton

//    private lateinit var binding: ActivityMainBinding

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        sbPlay = findViewById(R.id.sbPlay)
//        btnPlay = findViewById(R.id.btnPlay)
//        tvCurrentTime = findViewById(R.id.tvCurrentTime)
//        tvTotalTime = findViewById(R.id.tvTotalTime)
//        imgLike = findViewById(R.id.imgLike)

        binding.sbPlay.max = TOTAL_TIME
        binding.tvCurrentTime.text = formatTime(0)
        binding.tvTotalTime.text = formatTime(TOTAL_TIME)

//        binding.imgLike.setOnClickListener{
//            if(isLike){
//                unLike()
//            }
//            else{
//                enterLike()
//            }
//        }

        binding.cbLike.setOnCheckedChangeListener{_, isChecked->
            if(isChecked){
                binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_full,0,0,0)
            }
            else{
                binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_image,0,0,0)
            }

        }

        // Xử lý khi nhấn nút Play/Pause
        binding.btnPlay.setOnClickListener {
            if (isPlaying) {
                clickPause()
            } else {
                clickPlay()
            }
        }
    }
//    private val updateRun = object : Runnable {
//        override fun run() {
//            if (isPlaying && currentProgress < TOTAL_TIME) {
//                currentProgress++
//                binding.sbPlay.progress = currentProgress
//                binding.tvCurrentTime.text = formatTime(currentProgress)
//                handler.postDelayed(this, 1000)
//            } else {
//                clickPause()
//            }
//        }
//    }


//    phục hồi dữ liệu
    companion object{
        const val KEY_PROGRESS = "KEY_PROGRESS"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_PROGRESS,currentProgress)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentProgress = savedInstanceState.getInt(KEY_PROGRESS)
        binding.tvCurrentTime.text = formatTime(currentProgress)

    }

    override fun onPause() {
        super.onPause()
        clickPause()
    }

    override fun onResume() {
        super.onResume()
        if(isLike){
            clickPlay()
        }
    }

//    private fun enterLike(){
//        isLike = true
//        binding.cbLike.setImageResource(R.drawable.ic_heart_full)
//    }
//    private fun unLike(){
//        isLike = false
//        binding.cbLike.setImageResource(R.drawable.ic_heart_image)
//    }

    private fun clickPlay() {
        isPlaying = true
        binding.btnPlay.setImageResource(R.drawable.pause)

        if(timer==null){
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        if (isPlaying && currentProgress < TOTAL_TIME) {
                            currentProgress++
                            binding.sbPlay.progress = currentProgress
                            binding.tvCurrentTime.text = formatTime(currentProgress)
                        } else {
                            clickPause()
                        }
                    }
                }
            }, 0, 1000)
        }

    }

    private fun clickPause() {
        isPlaying = false
        binding.btnPlay.setImageResource(R.drawable.play)
        timer?.cancel()
        timer = null
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val sec = seconds % 60
        return String.format("%02d:%02d", minutes, sec)
    }
}