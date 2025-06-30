package com.example.myapplication.helper

import android.media.audiofx.Visualizer
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.myapplication.visualizer.CircularBarVisualizerView

class AudioVisualizerHelper(
    private val visualizerView: CircularBarVisualizerView
) {
    private var visualizer: Visualizer? = null

    fun setup(audioSessionId: Int) {
        release()

        // Kiểm tra session ID hợp lệ
        if (audioSessionId <= 0) {
            Log.e("AudioVisualizerHelper", "SessionId không hợp lệ: $audioSessionId")
            return
        }

        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]

                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {}

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let { visualizerView.updateFFT(it) }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)

                enabled = true

                if (!enabled) {
                    Log.w("AudioVisualizerHelper", "Visualizer không được bật – thiếu quyền RECORD_AUDIO?")
                }
            }

        } catch (e: Exception) {
            Log.e("AudioVisualizerHelper", "Lỗi khởi tạo Visualizer: ${e.message}")
            e.printStackTrace()
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }
}
