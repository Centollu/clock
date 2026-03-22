package com.example.clock

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var previewClock: TextView
    private lateinit var previewDate: TextView
    
    // Controls
    private lateinit var spinnerClockFont: Spinner
    private lateinit var checkClockBold: CheckBox
    private lateinit var seekClockWeight: SeekBar
    private lateinit var labelClockWeight: TextView
    
    private lateinit var spinnerDateFont: Spinner
    private lateinit var checkDateBold: CheckBox
    private lateinit var seekDateWeight: SeekBar
    private lateinit var labelDateWeight: TextView
    
    private lateinit var radioGroupColor: RadioGroup
    private lateinit var radioStatic: RadioButton
    private lateinit var layoutStaticColor: LinearLayout
    private lateinit var layoutRandomColor: LinearLayout
    
    private lateinit var seekRed: SeekBar
    private lateinit var seekGreen: SeekBar
    private lateinit var seekBlue: SeekBar
    
    private lateinit var seekInterval: SeekBar
    private lateinit var labelInterval: TextView

    private val fonts = arrayOf(
        R.font.dseg7_classic,
        R.font.dseg7_modern,
        R.font.dseg14_classic,
        R.font.dseg14_modern,
        0 // Sistema
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Bind Views
        previewClock = findViewById(R.id.previewClock)
        previewDate = findViewById(R.id.previewDate)
        
        spinnerClockFont = findViewById(R.id.spinnerClockFont)
        checkClockBold = findViewById(R.id.checkClockBold)
        seekClockWeight = findViewById(R.id.seekClockWeight)
        labelClockWeight = findViewById(R.id.labelClockWeight)
        
        spinnerDateFont = findViewById(R.id.spinnerDateFont)
        checkDateBold = findViewById(R.id.checkDateBold)
        seekDateWeight = findViewById(R.id.seekDateWeight)
        labelDateWeight = findViewById(R.id.labelDateWeight)
        
        radioGroupColor = findViewById(R.id.radioGroupColor)
        radioStatic = findViewById(R.id.radioStatic)
        layoutStaticColor = findViewById(R.id.layoutStaticColor)
        layoutRandomColor = findViewById(R.id.layoutRandomColor)
        
        seekRed = findViewById(R.id.seekRed)
        seekGreen = findViewById(R.id.seekGreen)
        seekBlue = findViewById(R.id.seekBlue)
        
        seekInterval = findViewById(R.id.seekInterval)
        labelInterval = findViewById(R.id.labelInterval)

        val btnSave = findViewById<Button>(R.id.btnSave)

        // Load Preferences
        val prefs = getSharedPreferences("ClockPrefs", Context.MODE_PRIVATE)
        spinnerClockFont.setSelection(prefs.getInt("clock_font_index", 0))
        checkClockBold.isChecked = prefs.getBoolean("clock_bold", false)
        seekClockWeight.progress = (prefs.getInt("clock_weight", 400) / 100) - 1
        
        spinnerDateFont.setSelection(prefs.getInt("date_font_index", 0))
        checkDateBold.isChecked = prefs.getBoolean("date_bold", true)
        seekDateWeight.progress = (prefs.getInt("date_weight", 700) / 100) - 1

        val colorMode = prefs.getInt("color_mode", 0) // 0 Random, 1 Static
        if (colorMode == 1) {
            radioStatic.isChecked = true
            layoutStaticColor.visibility = View.VISIBLE
            layoutRandomColor.visibility = View.GONE
        } else {
            findViewById<RadioButton>(R.id.radioRandom).isChecked = true
            layoutStaticColor.visibility = View.GONE
            layoutRandomColor.visibility = View.VISIBLE
        }

        val staticColor = prefs.getInt("static_color", Color.parseColor("#687C78"))
        seekRed.progress = Color.red(staticColor)
        seekGreen.progress = Color.green(staticColor)
        seekBlue.progress = Color.blue(staticColor)

        seekInterval.progress = prefs.getInt("random_interval", 1) - 1

        // Initialize Preview Texts
        val date = Date()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        previewClock.text = timeFormat.format(date)

        val dayWFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayW = dayWFormat.format(date).take(3).replaceFirstChar { it.uppercase() }
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val day = dayFormat.format(date)
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val month = monthFormat.format(date).replaceFirstChar { it.uppercase() }
        previewDate.text = "$dayW $day $month"

        setupListeners()
        updatePreview()

        btnSave.setOnClickListener {
            val editor = prefs.edit()
            editor.putInt("clock_font_index", spinnerClockFont.selectedItemPosition)
            editor.putBoolean("clock_bold", checkClockBold.isChecked)
            editor.putInt("clock_weight", (seekClockWeight.progress + 1) * 100)
            
            editor.putInt("date_font_index", spinnerDateFont.selectedItemPosition)
            editor.putBoolean("date_bold", checkDateBold.isChecked)
            editor.putInt("date_weight", (seekDateWeight.progress + 1) * 100)
            
            editor.putInt("color_mode", if (radioStatic.isChecked) 1 else 0)
            
            val newColor = Color.rgb(seekRed.progress, seekGreen.progress, seekBlue.progress)
            editor.putInt("static_color", newColor)
            
            editor.putInt("random_interval", seekInterval.progress + 1)
            
            editor.apply()
            finish()
        }
    }

    private fun setupListeners() {
        val updateAction = { updatePreview() }

        val fontSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerClockFont.onItemSelectedListener = fontSelectedListener
        spinnerDateFont.onItemSelectedListener = fontSelectedListener

        checkClockBold.setOnCheckedChangeListener { _, _ -> updatePreview() }
        checkDateBold.setOnCheckedChangeListener { _, _ -> updatePreview() }

        val simpleSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar) {
                    seekClockWeight -> labelClockWeight.text = getString(R.string.weight, (progress + 1) * 100)
                    seekDateWeight -> labelDateWeight.text = getString(R.string.weight, (progress + 1) * 100)
                    seekInterval -> labelInterval.text = getString(R.string.interval_minutes, progress + 1)
                }
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        
        seekClockWeight.setOnSeekBarChangeListener(simpleSeekBarChangeListener)
        seekDateWeight.setOnSeekBarChangeListener(simpleSeekBarChangeListener)
        seekInterval.setOnSeekBarChangeListener(simpleSeekBarChangeListener)
        seekRed.setOnSeekBarChangeListener(simpleSeekBarChangeListener)
        seekGreen.setOnSeekBarChangeListener(simpleSeekBarChangeListener)
        seekBlue.setOnSeekBarChangeListener(simpleSeekBarChangeListener)

        radioGroupColor.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioStatic) {
                layoutStaticColor.visibility = View.VISIBLE
                layoutRandomColor.visibility = View.GONE
            } else {
                layoutStaticColor.visibility = View.GONE
                layoutRandomColor.visibility = View.VISIBLE
            }
            updatePreview()
        }
    }

    private fun updatePreview() {
        // Appy Custom Font Logic
        applyTextStyle(previewClock, spinnerClockFont.selectedItemPosition, checkClockBold.isChecked, (seekClockWeight.progress + 1) * 100)
        applyTextStyle(previewDate, spinnerDateFont.selectedItemPosition, checkDateBold.isChecked, (seekDateWeight.progress + 1) * 100)

        // Color
        val color = if (radioStatic.isChecked) {
            Color.rgb(seekRed.progress, seekGreen.progress, seekBlue.progress)
        } else {
            // For random preview just show a color based on current sliders anyway or a random one 
            // since it's just previewing
            Color.CYAN
        }
        previewClock.setTextColor(color)
        previewDate.setTextColor(color)
    }

    private fun applyTextStyle(textView: TextView, fontIndex: Int, isBold: Boolean, weight: Int) {
        var baseTypeface: Typeface? = null
        val fontRes = fonts.getOrNull(fontIndex) ?: 0
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
            textView.paint.isFakeBoldText = isBold // Can add fake bold on top 
        } else {
            textView.setTypeface(baseTypeface, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textView.paint.isFakeBoldText = isBold
        }
    }
}
