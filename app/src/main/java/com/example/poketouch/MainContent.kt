package com.example.poketouch

import WasmBoy
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.poketouch.databinding.ContentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class MainContent : Fragment() {
    private var _binding: ContentMainBinding? = null
    private var running = true
    private lateinit var emulator: Emulator

    private val binding get() = _binding!!

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ContentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emulator = Emulator(resources.openRawResource(R.raw.crystal), binding.screen)
        emulator.start()
    }

    override fun onPause() {
        super.onPause()
        emulator.running = false
    }

    override fun onStart() {
        super.onStart()
        emulator.running = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}