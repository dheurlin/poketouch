package com.example.poketouch

import WasmBoy
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.view.get
import androidx.fragment.app.findFragment
import com.example.poketouch.databinding.ActivityMainBinding
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Array
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainContent: MainContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // test.executeFrame()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        fun mc(): MainContent? {
            val frag = supportFragmentManager.findFragmentById(R.id.main_content);
            if (frag is MainContent) {
                return frag
            }
            return null
        }

        mc()?.emulator?.backPressed = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        fun mc(): MainContent? {
            val frag = supportFragmentManager.findFragmentById(R.id.main_content);
            if (frag is MainContent) {
                return frag
            }
            return null
        }
        return when (item.itemId) {
            R.id.action_save_state -> { mc()?.emulator?.saveState(); true }
            R.id.action_load_state -> { mc()?.emulator?.loadState(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}