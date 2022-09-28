package com.example.poketouch.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.poketouch.databinding.FragmentControllerBinding
import kotlin.math.*

class ControllerFragment : Fragment() {

    companion object {
        fun newInstance() = ControllerFragment()
    }

    private lateinit var binding: FragmentControllerBinding
    public lateinit var buttonAdapter: ControllerButtonsAdapter


    public enum class DPadDirection {
        UP, DOWN, LEFT, RIGHT
    }

    var direction: DPadDirection? = null
        private set
    var aButton = false
    var startButton = false
        private set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentControllerBinding.inflate(inflater, container, false)
        val root = binding.root

        root.setOnTouchListener { v, event ->
            onTouch(view, event)
        }

        buttonAdapter = ControllerButtonsAdapter(mutableListOf())
        binding.buttons.adapter = buttonAdapter
        binding.buttons.layoutManager = LinearLayoutManager(this.context)

        return root
    }

    fun hideDPad() {
        binding.pokeBall.visibility = View.INVISIBLE
    }

    fun showDPad() {
        binding.pokeBall.visibility = View.VISIBLE
    }

    private fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (view == null || event == null) return false
        if (buttonAdapter.itemCount > 0) {
            return view.onTouchEvent(event)
        }

        val xCentered = event.x - (view.width / 2)
        val yCentered = event.y - (view.height / 2)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (xCentered > 0 && yCentered > 400) {
                    startButton = true
                    return true
                }
                if (xCentered.absoluteValue > 100 || yCentered.absoluteValue > 100) {
                    setDPadFromCoordinates(xCentered, yCentered)
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
                if (direction != null) {
                    setDPadFromCoordinates(xCentered, yCentered)
                    return true
                }
            }
        }
        return view.onTouchEvent(event)
    }



    private fun setDPadFromCoordinates(x: Float, y: Float) {
        val θ = atan2(y, x)
        direction = when {
            θ <= PI / 4       && θ > -PI / 4       -> DPadDirection.RIGHT
            θ <= -PI / 4      && θ > (-3 * PI) / 4 -> DPadDirection.UP
            θ <= (3 * PI) / 4 && θ > PI / 4        -> DPadDirection.DOWN
            else                                   -> DPadDirection.LEFT
        }
    }


    fun releaseButtons() {
        direction = null
        aButton = false
        startButton = false
    }

}