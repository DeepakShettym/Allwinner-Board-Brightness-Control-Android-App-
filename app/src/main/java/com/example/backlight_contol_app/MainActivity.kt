package com.example.backlight_contol_app

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val brightnessPath = "/sys/class/remedibacklight/lm3530/brightness"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val seekBar = findViewById<SeekBar>(R.id.brightnessSeekBar)
        val brightnessValue = findViewById<TextView>(R.id.brightnessValue)

        // Set SeekBar properties (range 0-127)
        seekBar.max = 127
        seekBar.progress = getCurrentBrightness()

        // Display initial brightness
        brightnessValue.text = "Brightness: ${seekBar.progress}"

        // SeekBar listener to update brightness
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightnessValue.text = "Brightness: $progress"
                setBrightness(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getCurrentBrightness(): Int {
        return try {
            val file = File(brightnessPath)
            if (file.exists()) {
                file.readText().trim().toInt()
            } else {
                127 // Default brightness if file not found
            }
        } catch (e: Exception) {
            e.printStackTrace()
            127
        }
    }

    private fun setBrightness(value: Int) {
        try {
            val file = File(brightnessPath)
            if (file.exists()) {
                FileWriter(file).use { writer ->
                    writer.write(value.toString())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
