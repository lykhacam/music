package com.example.myapplication

import Screen4ViewModel
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: Screen4ViewModel by viewModels() // Khởi tạo ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        viewModel.currentProgress.observe(this, Observer { progress ->
            binding.sbPlay.progress = progress
            binding.tvCurrentTime.text = formatTime(progress)
        })

        // Quan sát trạng thái phát nhạc
        viewModel.isPlaying.observe(this, Observer { isPlaying ->
            binding.btnPlay.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
        })

        // Quan sát trạng thái Like
        viewModel.isLiked.observe(this, Observer { isLiked ->
            binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(
                0,0, if (isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_image,0
            )
        })

        // Xử lý khi nhấn nút Play/Pause
        binding.btnPlay.setOnClickListener {
            val isPlaying = viewModel.isPlaying.value ?: false
            viewModel.setPlaying(!isPlaying)
        }

        // Xử lý khi SeekBar thay đổi
        binding.sbPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.updateProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Xử lý nút Like
        binding.cbLike.setOnClickListener {
            viewModel.toggleLike()
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)

    }
}
