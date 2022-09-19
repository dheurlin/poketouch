package com.example.poketouch

import WasmBoy
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class Emulator {
    private val wasmBoy: WasmBoy = WasmBoy(ByteBuffer.allocate(10_000_000), null)
    private val AUDIO_BUF_TARGET_SIZE = 2 * 4096
    private var audioBufLen = 0
    var running = false

    private lateinit var audio: AudioTrack
    private val screen: ScreenView
    private val controller: ControllerView

    constructor(rom: InputStream, screenView: ScreenView, controllerView: ControllerView) {
        screen = screenView
        controller = controllerView
        loadRom(rom)
        configure()
        initAudio()
    }

    private fun loadRom(rom: InputStream) {
        rom.readBytes().forEachIndexed { index, byte ->
            if (byte != 0.toByte()) {
                wasmBoy.memory.put(wasmBoy.cartridgE_ROM_LOCATION + index, byte)
            }
        }
    }

    private fun configure() {
        wasmBoy.config(
            0, // enableBootRom
            1, // preferGbc
            1, // audioBatchProcessing
            0, // graphicsBatchProcessing
            1, // timersBatchProcessing
            0, // graphicsDisableScanlineRendering
            1, // audioAccumulateSample
            1, // tileRendering
            1, // tileCaching
            0, // ?? (why 9 params???)
        )
    }

    private fun initAudio() {
        val bufsize = AudioTrack.getMinBufferSize(44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_8BIT)
        audio = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,  //sample rate
            AudioFormat.CHANNEL_OUT_STEREO,  //2 channel
            AudioFormat.ENCODING_PCM_8BIT,  // 16-bit
            bufsize,
            AudioTrack.MODE_STREAM
        )
        audio.play()
    }

    private fun playAudio() {
        audioBufLen = wasmBoy.numberOfSamplesInAudioBuffer * 2
        val audioArray = ByteArray(audioBufLen)
        wasmBoy.memory.position(wasmBoy.audiO_BUFFER_LOCATION)
        wasmBoy.memory.get(audioArray)
        audio.write(audioArray, 0, audioBufLen)
        wasmBoy.clearAudioBuffer()
    }

    private fun setJoypadInput() {
        wasmBoy.setJoypadState(
            if (controller.direction == ControllerView.DPadDirection.UP)    1 else 0,
            if (controller.direction == ControllerView.DPadDirection.RIGHT) 1 else 0,
            if (controller.direction == ControllerView.DPadDirection.DOWN)  1 else 0,
            if (controller.direction == ControllerView.DPadDirection.LEFT)  1 else 0,

            if (controller.aButton) 1 else 0,
            0, // B
            0, // SELECT
            if(controller.startButton) 1 else 0,
        )
    }

    fun start() {
        running = true

        thread {
            while (true) {
                if (!running) {
                    Thread.sleep(100)
                    continue
                }
                screen.drawScreen()
            }
        }

        thread {
            println("##### Starting emulation...")
            while (true) {
                if (wasmBoy.numberOfSamplesInAudioBuffer > 6000) continue;
                if (!running) {
                    Thread.sleep(100)
                    continue
                }
                val response = wasmBoy.executeFrame()
                if (response > -1) {
                    screen.getPixelsFromEmulator(wasmBoy)
                    playAudio()
                    setJoypadInput()
                } else {
                    println("##### Bruh moment... $response")
                }

                // Sleep a bit depending on how full the audio buffer is
                val audioBufFill =  audioBufLen.toFloat() / AUDIO_BUF_TARGET_SIZE.toFloat()
                Thread.sleep(((1000 / 60) * audioBufFill).toLong())
            }
        }
    }
}