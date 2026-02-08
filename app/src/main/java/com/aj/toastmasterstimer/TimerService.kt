package com.aj.toastmasterstimer

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aj.toastmasterstimer.R

class TimerService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var elapsedSeconds = 0
    private var isPaused = false
    private lateinit var timer: CountDownTimer
    private var speechType: SpeechType? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val typeName = intent.getStringExtra("speech_type")
                speechType = SpeechType.valueOf(typeName ?: "NORMAL_SPEECH")
                startForeground()
                startTimer()
            }
            "STOP" -> stopSelf()
            "PAUSE" -> isPaused = true
            "RESUME" -> isPaused = false
            "ADJUST_TIME" -> {
                elapsedSeconds = intent.getIntExtra("adjusted_time", 0)
            }
        }
        return START_STICKY
    }

    private fun startForeground() {
        val channelId = "timer_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Timer", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Toastmasters Timer")
            .setContentText("Timer is running")
            .setSmallIcon(R.drawable.ic_timer)
            .build()

        startForeground(1, notification)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "ToastmastersTimer::WakeLock"
        )
        wakeLock?.acquire(60 * 60 * 1000L)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    elapsedSeconds++
                    val intent = Intent("TIMER_UPDATE")
                    intent.putExtra("elapsed", elapsedSeconds.toLong())
                    LocalBroadcastManager.getInstance(this@TimerService).sendBroadcast(intent)
                }
            }
            override fun onFinish() {}
        }
        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timer.isInitialized) timer.cancel()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}