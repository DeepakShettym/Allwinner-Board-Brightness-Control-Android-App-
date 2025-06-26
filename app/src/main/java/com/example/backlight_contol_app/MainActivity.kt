package com.example.backlight_contol_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val brightnessPath = "/sys/class/remedibacklight/lm3530/brightness"
    private lateinit var seekBar: SeekBar
    private lateinit var brightnessValue: TextView
    private lateinit var intensityValue: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L // 5 seconds

    private var currentBrightness = 80 // Start mid-range

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        seekBar = findViewById(R.id.brightnessSeekBar)
        brightnessValue = findViewById(R.id.brightnessValue)
        intensityValue = findViewById(R.id.intensityTextView) // Add this TextView in layout

        seekBar.max = 127
        seekBar.progress = currentBrightness
        brightnessValue.text = "Brightness: $currentBrightness"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentBrightness = progress
                    brightnessValue.text = "Brightness: $progress"
                    setBrightness(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startAutoUpdate()
    }

    private fun setBrightness(value: Int) {
        try {
            val file = File(brightnessPath)
            if (file.exists()) {
                FileWriter(file).use { it.write(value.toString()) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startAutoUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                val intensity = Random.nextInt(0, 1024)

                // Display intensity
                intensityValue.text = "Light Intensity: $intensity"

                // Update brightness based on intensity
                when {
                    intensity < 400 -> {
                        currentBrightness = (currentBrightness + 10).coerceAtMost(127)
                    }
                    intensity > 700 -> {
                        currentBrightness = (currentBrightness - 10).coerceAtLeast(0)
                    }
                    // Else keep brightness unchanged
                }

                // Apply updated brightness
                seekBar.progress = currentBrightness
                brightnessValue.text = "Brightness: $currentBrightness"
                setBrightness(currentBrightness)

                handler.postDelayed(this, updateInterval)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
