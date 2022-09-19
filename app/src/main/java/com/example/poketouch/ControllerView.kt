package com.example.poketouch

import android.content.Context
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
                if (xCentered!!.absoluteValue > 100 || yCentered!!.absoluteValue > 100) {
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
        if (y > 0 && θ.absoluteValue > PI / 4) {
            direction = DPadDirection.DOWN
        } else if (y <= 0 && θ.absoluteValue >= PI / 4) {
            direction = DPadDirection.UP
        } else if (x > 0 && θ.absoluteValue < PI / 4) {
            direction = DPadDirection.RIGHT
        } else {
            direction = DPadDirection.LEFT
        }
    }

    private fun releaseButtons() {
        direction = null
        aButton = false
        startButton = false
    }
}
