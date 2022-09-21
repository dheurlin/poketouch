package com.example.poketouch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan

class ControllerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = 0
): View(context, attributeSet, defStyleAttr) {

    public enum class DPadDirection {
        UP, DOWN, LEFT, RIGHT
    }

    var direction: DPadDirection? = null
        private set
    var aButton = false
        private set
    var startButton = false
        private set

    public var text: String? = null
        set(value) {
            field = value
            invalidate()
        }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // return super.onTouchEvent(event)

        val xCentered = event?.x?.minus((width / 2))
        val yCentered = event?.y?.minus((height / 2))

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (xCentered!! > 0 && yCentered!! > 400) {
                    startButton = true
                    return true
                }
                if (xCentered.absoluteValue > 100 || yCentered!!.absoluteValue > 100) {
                    setDPadFromCoordinates(xCentered, yCentered!!)
                    return true
                }
                // Touching middle
                aButton = true
                return true
            }
            MotionEvent.ACTION_UP -> {
                releaseButtons()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // TODO Only if we have moved a certain distance?
                setDPadFromCoordinates(xCentered!!, yCentered!!)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun setDPadFromCoordinates(x: Float, y: Float) {
        val θ = atan(y / x)
        direction = if (y > 0 && θ.absoluteValue > PI / 4) {
            DPadDirection.DOWN
        } else if (y <= 0 && θ.absoluteValue >= PI / 4) {
            DPadDirection.UP
        } else if (x > 0 && θ.absoluteValue < PI / 4) {
            DPadDirection.RIGHT
        } else {
            DPadDirection.LEFT
        }
    }

    private fun releaseButtons() {
        direction = null
        aButton = false
        startButton = false
    }

    public override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (this.text == null) return

        var paint = Paint()
        paint.setColor(Color.WHITE)
        paint.textSize = 150f
        canvas?.drawText(text ?: "", 50f, 150f, paint)


    }
}
