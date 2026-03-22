package com.example.clock

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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

    private var lastColorChangeTime: Long = 0
    private var currentRandomIntervalMinutes: Int = 1
    private var colorMode: Int = 0 // 0 Random, 1 Static

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
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

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
        loadSettings()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
        
        // Font
        applyTextStyle(textClock, prefs.getInt("clock_font_index", 0), prefs.getBoolean("clock_bold", false), prefs.getInt("clock_weight", 400))
        applyTextStyle(textDate, prefs.getInt("date_font_index", 0), prefs.getBoolean("date_bold", true), prefs.getInt("date_weight", 700))
        
        // Color
        colorMode = prefs.getInt("color_mode", 0)
        currentRandomIntervalMinutes = prefs.getInt("random_interval", 1)
        
        if (colorMode == 1) {
            val staticColor = prefs.getInt("static_color", Color.parseColor("#687C78"))
            textClock.setTextColor(staticColor)
            textDate.setTextColor(staticColor)
        } else {
            // Force random color update on resume if random mode
            setRandomColor()
            lastColorChangeTime = System.currentTimeMillis()
        }
    }

    private fun applyTextStyle(textView: TextView, fontIndex: Int, isBold: Boolean, weight: Int) {
        val fontsList = arrayOf(
            R.font.dseg7_classic,
            R.font.dseg7_modern,
            R.font.dseg14_classic,
            R.font.dseg14_modern,
            0 // System
        )
        
        var baseTypeface: Typeface? = null
        val fontRes = fontsList.getOrNull(fontIndex) ?: 0
        if (fontRes != 0) {
            try {
                baseTypeface = ResourcesCompat.getFont(this, fontRes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            baseTypeface = Typeface.DEFAULT
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && baseTypeface != null) {
            val finalTypeface = Typeface.create(baseTypeface, weight, false)
            textView.typeface = finalTypeface
            textView.paint.isFakeBoldText = isBold
        } else {
            textView.setTypeface(baseTypeface, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textView.paint.isFakeBoldText = isBold
        }
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

        if (colorMode == 0) {
            val now = System.currentTimeMillis()
            if (now - lastColorChangeTime >= currentRandomIntervalMinutes * 60 * 1000L) {
                setRandomColor()
                lastColorChangeTime = now
            }
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
