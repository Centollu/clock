package com.example.clock

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var textClock: TextView
    private lateinit var textDate: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val fonts = arrayOf(
        R.font.dseg7_classic,
        R.font.dseg7_modern,
        R.font.dseg14_classic,
        R.font.dseg14_modern
    )
    private var currentFontIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake lock - keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enter full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContentView(R.layout.activity_main)

        textClock = findViewById(R.id.textClock)
        textDate = findViewById(R.id.textDate)

        val rootView = findViewById<android.view.View>(R.id.rootView)
        rootView.setOnClickListener {
            currentFontIndex = (currentFontIndex + 1) % fonts.size
            val typeface = androidx.core.content.res.ResourcesCompat.getFont(this, fonts[currentFontIndex])
            textClock.typeface = typeface
            
            // Optionally change date font too if we want a uniform look
            // textDate.typeface = typeface 
        }

        setRandomColor()
        updateTime()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateTime() {
        val date = Date()
        
        // Time format: HH:mm
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(date)
        
        if (textClock.text != timeString) {
            textClock.text = timeString
        }

        // Date format: DayW Day Month
        val dayWFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayW = dayWFormat.format(date).take(3).replaceFirstChar { it.uppercase() }
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val day = dayFormat.format(date)
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val month = monthFormat.format(date).replaceFirstChar { it.uppercase() }
        
        val dateString = "$dayW $day $month"
        if (textDate.text != dateString) {
            textDate.text = dateString
        }
    }

    private fun setRandomColor() {
        val brightness = 2
        val rgb = intArrayOf(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
        val mix = intArrayOf(brightness * 51, brightness * 51, brightness * 51)
        val mixedRgb = intArrayOf(
            (rgb[0] + mix[0]) / 2,
            (rgb[1] + mix[1]) / 2,
            (rgb[2] + mix[2]) / 2
        )
        val color = Color.rgb(mixedRgb[0], mixedRgb[1], mixedRgb[2])
        
        textClock.setTextColor(color)
        textDate.setTextColor(color)
    }
}
