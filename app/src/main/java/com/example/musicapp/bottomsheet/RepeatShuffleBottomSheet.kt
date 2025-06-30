package com.example.myapplication.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.SongAdapter
import com.example.myapplication.databinding.BottomsheetRepeatShuffleBinding
import com.example.myapplication.model.Song
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RepeatShuffleBottomSheet(
    private val context: Context,
    private val mode: Mode,
    private val songList: List<Song>,
    private val onModeSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    enum class Mode { REPEAT, SHUFFLE }

    private lateinit var binding: BottomsheetRepeatShuffleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetRepeatShuffleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SongAdapter(songList.toMutableList()) { }
        binding.rvSongs.layoutManager = LinearLayoutManager(context)
        binding.rvSongs.adapter = adapter

        if (mode == Mode.REPEAT) {
            binding.groupRepeat.visibility = View.VISIBLE
            binding.groupShuffle.visibility = View.GONE

            setupRepeatOptions()

        } else {
            binding.groupRepeat.visibility = View.GONE
            binding.groupShuffle.visibility = View.VISIBLE

            setupShuffleOptions()
        }
    }

    private fun setupRepeatOptions() {
        binding.optionRepeatNone.setOnClickListener {
            onModeSelected("none")
            dismiss()
        }
        binding.optionRepeatOne.setOnClickListener {
            onModeSelected("one")
            dismiss()
        }
        binding.optionRepeatAll.setOnClickListener {
            onModeSelected("all")
            dismiss()
        }
    }

    private fun setupShuffleOptions() {
        binding.optionShuffleOff.setOnClickListener {
            onModeSelected("off")
            dismiss()
        }
        binding.optionShuffleOn.setOnClickListener {
            onModeSelected("on")
            dismiss()
        }
    }
}
