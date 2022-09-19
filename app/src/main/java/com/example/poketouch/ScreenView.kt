package com.example.poketouch

import WasmBoy
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import androidx.core.content.res.ResourcesCompat

class ScreenView: SurfaceView {
    object Dims {
        const val X = 160
        const val Y = 144
    }

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private var scaleFactor: Int = 1
    private var pixelBuffer = Array(Dims.Y) { IntArray(Dims.X) }
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.black, null)

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        println("####### Calling constructor!")
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(Dims.X, Dims.Y, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)

        scaleFactor = width / Dims.X
    }

    public fun drawScreen() {
        if (!holder.surface.isValid) return;

        for (x in 0 until Dims.X) {
            for (y in 0 until Dims.Y) {
                val px = pixelBuffer[y][x]
                extraBitmap.setPixel(
                    x,
                    y,
                    (0xFF000000 + px).toInt()
                )
            }
        }

        // frontBuffer = backBuffer.also { backBuffer = frontBuffer }

        // TODO Why does canvas become null?
        val canvas = holder.lockCanvas() ?: return
        canvas.save()

        val marginLeft = (width - (Dims.X * scaleFactor)) / 2
        val marginTop = (height - (Dims.Y * scaleFactor)) / 2
        val maxX = Dims.X * scaleFactor + marginLeft
        val maxY = Dims.Y * scaleFactor + marginTop

        canvas.drawBitmap(
            extraBitmap,
            null,
            Rect(marginLeft, marginTop, maxX, maxY),
            null
        )
        canvas.restore()
        holder.unlockCanvasAndPost(canvas)

    }


    public fun getPixelsFromEmulator(emulator: WasmBoy) {
        for (x in 0 until Dims.X) {
            for (y in 0 until Dims.Y) {
                val start = emulator.framE_LOCATION + (((y * 160) + x) * 3)
                val r = emulator.memory[start + 0].toInt()
                val g = emulator.memory[start + 1].toInt()
                val b = emulator.memory[start + 2].toInt()
                // backBuffer[y][x] = (r shl 16) + (g shl 8) + b
                pixelBuffer[y][x] = (r shl 16) + (g shl 8) + b
            }
        }

    }

    public fun redraw() {
        this.invalidate()
    }

}