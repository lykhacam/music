package com.example.myapplication

import Screen4ViewModel
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.myapplication.databinding.ActivityScreen4Binding

class Screen4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityScreen4Binding
    private val viewModel: Screen4ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScreen4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalTime = viewModel.getTotalTime()
        binding.tvTotalTime.text = formatTime(totalTime)
        binding.sbPlay.max = totalTime // SeekBar có giá trị tối đa là tổng thời gian

        // Cập nhật SeekBar & thời gian hiện tại
        viewModel.currentProgress.observe(this) { progress ->
            binding.sbPlay.progress = progress
            binding.tvCurrentTime.text = formatTime(progress)
        }

        // Cập nhật trạng thái Play/Pause
        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.btnPlay.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play)
        }

        // Cập nhật trạng thái Like
        viewModel.isLiked.observe(this) { isLiked ->
            binding.cbLike.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, if (isLiked) R.drawable.ic_heart_full else R.drawable.ic_heart_image, 0
            )
        }

        // Xử lý nút Play/Pause
        binding.btnPlay.setOnClickListener {
            viewModel.setPlaying(!(viewModel.isPlaying.value ?: false))
        }

        // Xử lý SeekBar
        binding.sbPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.updateProgress(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Xử lý nút Like
        binding.cbLike.setOnClickListener {
            viewModel.toggleLike()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setPlaying(false) // Dừng nhạc khi thoát app
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isPlaying.value == true) viewModel.setPlaying(true) // Tiếp tục phát nếu trước đó đang phát
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}
