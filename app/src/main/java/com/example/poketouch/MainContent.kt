package com.example.poketouch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.poketouch.controller.ControllerFragment
import com.example.poketouch.databinding.ContentMainBinding
import com.example.poketouch.emulator.Emulator

class MainContent : Fragment() {
    private var _binding: ContentMainBinding? = null
    private var running = true
    public lateinit var emulator: Emulator

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

        val act = activity
        if (act != null)  {
            val controller = childFragmentManager.findFragmentById(R.id.controller) as ControllerFragment
            emulator = Emulator(resources.openRawResource(R.raw.pokecrystal), binding.screen, controller, act)
            emulator.start()
            emulator.loadState()
        }
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