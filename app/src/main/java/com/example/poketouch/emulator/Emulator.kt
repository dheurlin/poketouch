package com.example.poketouch.emulator

import WasmBoy
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.poketouch.ControllerView
import com.example.poketouch.ScreenView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class Emulator(rom: InputStream, screenView: ScreenView, controllerView: ControllerView, context: Context) {
    private val wasmBoy: WasmBoy = WasmBoy(ByteBuffer.allocate(20_000_000), null)
    private val AUDIO_BUF_TARGET_SIZE = 2 * 4096
    private var audioBufLen = 0
    private val context: Context = context

    var running = false
    var shouldLoadState = false
    var shouldSaveState = false

    private lateinit var audio: AudioTrack
    private val screen: ScreenView = screenView
    private val controller: ControllerView = controllerView

    private val breakMan: BreakpointManager = BreakpointManager(wasmBoy)

    init {
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


    public fun loadState() {
        shouldLoadState = true
    }

    public fun saveState() {
        shouldSaveState = true
    }

    private fun _saveState() {
        wasmBoy.saveState()

        val wasmState = ByteArray(wasmBoy.wasmboY_STATE_SIZE)
        wasmBoy.memory.position(wasmBoy.wasmboY_STATE_LOCATION)
        wasmBoy.memory.get(wasmState)

        val gbInternalMemory = ByteArray(wasmBoy.gameboY_INTERNAL_MEMORY_SIZE)
        wasmBoy.memory.position(wasmBoy.gameboY_INTERNAL_MEMORY_LOCATION)
        wasmBoy.memory.get(gbInternalMemory)

        val cartridgeRam = ByteArray(wasmBoy.cartridgE_RAM_SIZE)
        wasmBoy.memory.position(wasmBoy.cartridgE_RAM_LOCATION)
        wasmBoy.memory.get(cartridgeRam)

        val gbcPalette = ByteArray(wasmBoy.gbC_PALETTE_SIZE)
        wasmBoy.memory.position(wasmBoy.gbC_PALETTE_LOCATION)
        wasmBoy.memory.get(gbcPalette)

        val file = File(context.filesDir, "state")
        if (file.isFile) {
            // TODO Support inf states?
            println("Deleting existing saveState...")
            file.delete()
        }
        FileOutputStream(file).use {
            it.write(wasmState)
            it.write(gbInternalMemory)
            it.write(cartridgeRam)
            it.write(gbcPalette)
        }

        shouldSaveState = false
    }


    private fun _loadState() {
        val file = File(context.filesDir, "state")
        if (!file.isFile) {
            println("Save state not created...")
            return
        }
        val wasmState = ByteArray(wasmBoy.wasmboY_STATE_SIZE)
        val gbInternalMemory = ByteArray(wasmBoy.gameboY_INTERNAL_MEMORY_SIZE)
        val cartridgeRam = ByteArray(wasmBoy.cartridgE_RAM_SIZE)
        val gbcPalette = ByteArray(wasmBoy.gbC_PALETTE_SIZE)

        FileInputStream(file).use {
            it.read(wasmState)
            it.read(gbInternalMemory)
            it.read(cartridgeRam)
            it.read(gbcPalette)
        }

        wasmBoy.memory.position(wasmBoy.wasmboY_STATE_LOCATION)
        wasmBoy.memory.put(wasmState)
        wasmBoy.memory.position(wasmBoy.gameboY_INTERNAL_MEMORY_LOCATION)
        wasmBoy.memory.put(gbInternalMemory)
        wasmBoy.memory.position(wasmBoy.cartridgE_RAM_LOCATION)
        wasmBoy.memory.put(cartridgeRam)
        wasmBoy.memory.position(wasmBoy.gbC_PALETTE_LOCATION)
        wasmBoy.memory.put(gbcPalette)

        wasmBoy.loadState()

        shouldLoadState = false
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

        breakMan.setPCBreakPoint(Offsets.StartBattle)
        breakMan.setPCBreakPoint(Offsets.ExitBattle)

        controller.text = "Not in battle"

        thread {
            println("##### Starting emulation...")
            while (true) {
                if (wasmBoy.numberOfSamplesInAudioBuffer > 6000) continue;
                if (!running) {
                    Thread.sleep(100)
                    continue
                }

                if (shouldLoadState) _loadState()
                if (shouldSaveState) _saveState()

                val pc = wasmBoy.programCounter
                if (pc == 0x74c1) {
                    controller.text = "in battle"
                }
                if (pc == 0x769e) {
                    controller.text = "left battle"
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
//                Thread.sleep(((1000 / 60) * audioBufFill).toLong())
            }
        }
    }
}