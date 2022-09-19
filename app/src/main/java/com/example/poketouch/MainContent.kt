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


        val emulator = WasmBoy(ByteBuffer.allocate(10_000_000), null)

        // Load the rom
        val rom = resources.openRawResource(R.raw.crystal)
        rom.readBytes().forEachIndexed { index, byte ->
            if (byte != 0.toByte()) {
                emulator.memory.put(emulator.cartridgE_ROM_LOCATION + index, byte)
            }
        }
        emulator.config(
            0, // enableBootRom
            1, // preferGbc
            1, // audioBatchProcessing
            1, // graphicsBatchProcessing
            1, // timersBatchProcessing
            0, // graphicsDisableScanlineRendering
            1, // audioAccumulateSample
            1, // tileRendering
            1, // tileCaching
            0, // ?? (why 9 params???)
        )

        // set up audio
        val bufsize = AudioTrack.getMinBufferSize(44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_8BIT)
        val audio = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,  //sample rate
            AudioFormat.CHANNEL_OUT_STEREO,  //2 channel
            AudioFormat.ENCODING_PCM_8BIT,  // 16-bit
            bufsize,
            AudioTrack.MODE_STREAM
        )
        audio.play()
        val AUDIO_BUF_TARGET_SIZE = 2 * 4096
        var audioBufLen = 0

        thread {
            while (true) {
                if (!running) {
                    Thread.sleep(100)
                    continue
                }
                binding.screen.drawScreen()
            }
        }

        thread {
            println("##### Starting emulation...")
            while (true) {
                if (emulator.numberOfSamplesInAudioBuffer > 6000) continue;
                if (!running) {
                    Thread.sleep(100)
                    continue
                }
                val response = emulator.executeFrame()
                if (response > -1) {
                    binding.screen.getPixelsFromEmulator(emulator)
                    // thread { binding.screen.drawScreen() }

                    audioBufLen = emulator.numberOfSamplesInAudioBuffer * 2
                    val audioArray = ByteArray(audioBufLen)
                    emulator.memory.position(emulator.audiO_BUFFER_LOCATION)
                    emulator.memory.get(audioArray)
                    audio.write(audioArray, 0, audioBufLen)
                    emulator.clearAudioBuffer()
                    // println("ye, respoinse = $response")
                } else {
                    println("##### Bruh the response weren't above 0... $response")
                }
                // TODD more accurate timing
                val audioBufFill =  audioBufLen.toFloat() / AUDIO_BUF_TARGET_SIZE.toFloat()
                Thread.sleep(((1000 / 60) * audioBufFill).toLong())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
    }

    override fun onStart() {
        super.onStart()
        running = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}