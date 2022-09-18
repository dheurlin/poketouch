package com.example.poketouch

import WasmBoy
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import java.nio.ByteBuffer

class ScreenView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    object Dims {
        const val X = 160
        const val Y = 144
    }

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var scaleFactor: Int = 1
    var pixels = Array(Dims.Y) { IntArray(Dims.X) }
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.black, null)

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        scaleFactor = width / Dims.X
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the pixel array to the bitmap
        val marginLeft = (extraBitmap.width - (Dims.X * scaleFactor)) / 2
        val marginTop = (extraBitmap.height - (Dims.Y * scaleFactor)) / 2
        for (x in 0 until Dims.X * scaleFactor) {
            for (y in 0 until Dims.Y * scaleFactor) {
                val px = pixels[y / scaleFactor][x / scaleFactor]
                extraBitmap.setPixel(
                    x,
                    y,
                    (0xFF000000 + px).toInt()
                )
            }
        }
        canvas.drawBitmap(extraBitmap, marginLeft.toFloat(), marginTop.toFloat(), null)
    }

    public fun getPixelsFromEmulator(emulator: WasmBoy) {
        val getRgbPixelStart = { x: Int, y: Int ->
            emulator.framE_LOCATION + (((y * 160) + x) * 3)
        }
        for (x in 0 until Dims.X) {
            for (y in 0 until Dims.Y) {
                val start = getRgbPixelStart(x,y)
                val r = emulator.memory[start + 0].toInt()
                val g = emulator.memory[start + 1].toInt()
                val b = emulator.memory[start + 2].toInt()
                pixels[y][x] = (r shl 16) + (g shl 8) + b
            }
        }

    }

    public fun redraw() {
        this.invalidate()
    }

}