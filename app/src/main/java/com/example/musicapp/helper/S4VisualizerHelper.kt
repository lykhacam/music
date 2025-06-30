package com.example.myapplication.helper

import android.media.audiofx.Visualizer
import android.util.Log
import com.example.myapplication.databinding.ActivityScreen4Binding

object S4VisualizerHelper {

    private var visualizer: Visualizer? = null

    fun setup(binding: ActivityScreen4Binding, audioSessionId: Int) {
        release()

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
                        fft?.let {
                            binding.visualizerView.updateFFT(it)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)

                enabled = true
            }

            Log.d("Visualizer", "Visualizer initialized with session $audioSessionId")
        } catch (e: Exception) {
            Log.e("Visualizer", "Failed to init visualizer: ${e.message}")
        }
    }

    fun release() {
        visualizer?.release()
        visualizer = null
    }
}
