package com.example.poketouch

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poketouch.databinding.FragmentControllerBinding
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan

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

    private fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (view == null || event == null) return false
        if (buttonAdapter.itemCount > 0) {
            return view.onTouchEvent(event)
        }

        val xCentered = event.x.minus((view.width / 2))
        val yCentered = event.y.minus((view.height / 2))

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
                // TODO Only if we have moved a certain distance?
                setDPadFromCoordinates(xCentered!!, yCentered!!)
                return true
            }
        }
        return view.onTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        buttonAdapter = ControllerButtonsAdapter(mutableListOf())
//        binding.buttons.adapter = buttonAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    private fun setDPadFromCoordinates(x: Float, y: Float) {
        val θ = atan(y / x)
        direction = if (y > 0 && θ.absoluteValue > PI / 4) {
            ControllerFragment.DPadDirection.DOWN
        } else if (y <= 0 && θ.absoluteValue >= PI / 4) {
            ControllerFragment.DPadDirection.UP
        } else if (x > 0 && θ.absoluteValue < PI / 4) {
            ControllerFragment.DPadDirection.RIGHT
        } else {
            ControllerFragment.DPadDirection.LEFT
        }
    }


    fun releaseButtons() {
        direction = null
        aButton = false
        startButton = false
    }

}