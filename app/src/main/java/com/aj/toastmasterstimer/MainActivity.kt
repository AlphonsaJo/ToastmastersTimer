package com.aj.toastmasterstimer

import android.content.*
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputEditText
import com.aj.toastmasterstimer.R

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: SeekBar
    private lateinit var timerDisplay: TextView
    private lateinit var speakerNameInput: TextInputEditText
    private lateinit var speechNameInput: TextInputEditText
    private lateinit var startStopBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var restartBtn: ImageButton
    private lateinit var speechTypeSpinner: Spinner
    private lateinit var soundToggle: ImageView
    private lateinit var mainLayout: LinearLayout

    private var isSoundEnabled = true
    private var isTimerRunning = false
    private var isPaused = false
    private var selectedSpeech: SpeechType? = null
    private var mediaPlayer: MediaPlayer? = null

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val elapsed = intent?.getLongExtra("elapsed", 0)?.toInt() ?:0
            updateUI(elapsed)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinner()
        setupListeners()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(timerReceiver, IntentFilter("TIMER_UPDATE"))
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        timerDisplay = findViewById(R.id.timerDisplay)
        speakerNameInput = findViewById(R.id.speakerNameInput)
        speechNameInput = findViewById(R.id.speechNameInput)
        startStopBtn = findViewById(R.id.startStopBtn)
        pauseBtn = findViewById(R.id.pauseBtn)
        restartBtn = findViewById(R.id.restartBtn)
        speechTypeSpinner = findViewById(R.id.speechTypeSpinner)
        soundToggle = findViewById(R.id.soundToggle)
        mainLayout = findViewById(R.id.mainLayout)
    }

    private fun setupSpinner() {
        val speeches = SpeechType.values().map { it.displayName }
        val adapter = ArrayAdapter(this,  R.layout.spinner_item, speeches)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        speechTypeSpinner.adapter = adapter

        speechTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedSpeech = SpeechType.values()[pos]
                selectedSpeech?.let {
                    progressBar.max = it.redTime
                    if (!isTimerRunning) {
                        progressBar.progress = 0
                        timerDisplay.text = formatTime(0)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        soundToggle.setOnClickListener {
            isSoundEnabled = !isSoundEnabled
            soundToggle.setImageResource(
                if (isSoundEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off
            )
        }

        findViewById<ImageView>(R.id.menuIcon).setOnClickListener {
            showMenuDropdown()
        }

        findViewById<ImageView>(R.id.logIcon).setOnClickListener {
            startActivity(Intent(this, TimerLogActivity::class.java))
        }

        startStopBtn.setOnClickListener {
            if (selectedSpeech == null) {
                Toast.makeText(this, "Select a speech type first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isTimerRunning) {
                startTimer()
            } else {
                stopTimer()
            }
        }

        pauseBtn.setOnClickListener {
            if (isTimerRunning) {
                togglePause()
            }
        }

        restartBtn.setOnClickListener {
            restartTimer()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val intent = Intent(this@MainActivity, TimerService::class.java)
                    intent.action = "ADJUST_TIME"
                    intent.putExtra("adjusted_time", progress)
                    startService(intent)
                    timerDisplay.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun startTimer() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = "START"
        intent.putExtra("speech_type", selectedSpeech?.name)
        startService(intent)

        isTimerRunning = true
        isPaused = false
        startStopBtn.text = "Stop"
        pauseBtn.isEnabled = true
    }

    private fun stopTimer() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = "STOP"
        startService(intent)

        selectedSpeech?.let { speech ->
            val elapsed = progressBar.progress
            val speakerName = speakerNameInput.text.toString()
            val speechName = speechNameInput.text.toString()

            TimerLog.addEntry(speech.displayName, speakerName, speechName, elapsed, speech)
        }

        isTimerRunning = false
        isPaused = false
        startStopBtn.text = "Start"
        pauseBtn.isEnabled = false
        mainLayout.setBackgroundColor(getColor(android.R.color.white))
    }

    private fun togglePause() {
        val intent = Intent(this, TimerService::class.java)
        intent.action = if (isPaused) "RESUME" else "PAUSE"
        startService(intent)

        isPaused = !isPaused
        pauseBtn.text = if (isPaused) "Resume" else "Pause"
    }

    private fun restartTimer() {
        stopTimer()
        progressBar.progress = 0
        timerDisplay.text = formatTime(0)
    }

    private fun updateUI(elapsedSeconds: Int) {
        progressBar.progress = elapsedSeconds
        timerDisplay.text = formatTime(elapsedSeconds)

        selectedSpeech?.let { speech ->
            val color = when {
                elapsedSeconds < speech.greenTime -> getColor(android.R.color.white)
                elapsedSeconds < speech.yellowTime -> getColor(android.R.color.holo_green_light)
                elapsedSeconds < speech.redTime -> getColor(android.R.color.holo_orange_light)
                else -> getColor(android.R.color.holo_red_light)
            }
            mainLayout.setBackgroundColor(color)

            if (isSoundEnabled) {
                when (elapsedSeconds) {
                    speech.greenTime, speech.yellowTime, speech.redTime -> playSound()
                }
            }
        }
    }

    private fun playSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.beep)
        mediaPlayer?.start()
    }

    private fun showMenuDropdown() {
        val popup = PopupMenu(this, findViewById(R.id.menuIcon))
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_timer -> true
                R.id.menu_script -> {
                    startActivity(Intent(this, ScriptActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver)
        mediaPlayer?.release()
    }
}