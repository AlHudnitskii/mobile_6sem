package com.example.timer.service

import android.app.*
import android.content.Intent
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.timer.MainActivity
import com.example.timer.R
import com.example.timer.data.model.Phase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimerService : Service() {

    companion object {
        const val ACTION_START = "START"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_RESUME = "RESUME"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREV = "PREV"
        const val ACTION_STOP = "STOP"
        const val EXTRA_PHASES = "phases_json"
        const val CHANNEL_ID = "timer_channel"
        const val NOTIF_ID = 1

        const val BROADCAST_TICK = "com.example.timer.TICK"
        const val BROADCAST_PHASE_CHANGE = "com.example.timer.PHASE_CHANGE"
        const val BROADCAST_FINISHED = "com.example.timer.FINISHED"
        const val EXTRA_REMAINING = "remaining"
        const val EXTRA_PHASE_INDEX = "phase_index"
        const val EXTRA_PHASE_LABEL = "phase_label"
    }

    private var phases: List<Phase> = emptyList()
    private var currentIndex = 0
    private var remainingSeconds = 0
    private var isPaused = false
    private var countDownTimer: CountDownTimer? = null
    private val toneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_ALARM, 100)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val json = intent.getStringExtra(EXTRA_PHASES) ?: return START_NOT_STICKY
                val type = object : TypeToken<List<Phase>>() {}.type
                phases = Gson().fromJson(json, type)
                currentIndex = 0
                isPaused = false
                startForeground(NOTIF_ID, buildNotification(getString(R.string.timer_started)))
                startPhase()
            }
            ACTION_PAUSE -> {
                isPaused = true
                countDownTimer?.cancel()
                updateNotification(getString(R.string.timer_paused))
            }
            ACTION_RESUME -> {
                isPaused = false
                startCountDown(remainingSeconds)
            }
            ACTION_NEXT -> {
                countDownTimer?.cancel()
                currentIndex++
                startPhase()
            }
            ACTION_PREV -> {
                countDownTimer?.cancel()
                if (currentIndex > 0) currentIndex--
                startPhase()
            }
            ACTION_STOP -> {
                countDownTimer?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startPhase() {
        if (currentIndex >= phases.size) {
            broadcastFinished()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val phase = phases[currentIndex]
        remainingSeconds = phase.durationSeconds
        broadcastPhaseChange()
        playTone()
        updateNotification("${phase.type.name}: $remainingSeconds сек")
        startCountDown(remainingSeconds)
    }

    private fun startCountDown(seconds: Int) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt() + 1
                broadcastTick()
                updateNotification("${phases.getOrNull(currentIndex)?.type?.name}: $remainingSeconds сек")
            }
            override fun onFinish() {
                currentIndex++
                startPhase()
            }
        }.start()
    }

    private fun playTone() {
        try { toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600) } catch (_: Exception) {}
    }

    private fun broadcastTick() {
        sendBroadcast(Intent(BROADCAST_TICK).putExtra(EXTRA_REMAINING, remainingSeconds))
    }

    private fun broadcastPhaseChange() {
        sendBroadcast(Intent(BROADCAST_PHASE_CHANGE).apply {
            putExtra(EXTRA_PHASE_INDEX, currentIndex)
            putExtra(EXTRA_PHASE_LABEL, phases.getOrNull(currentIndex)?.type?.name ?: "")
        })
    }

    private fun broadcastFinished() {
        try { toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1200) } catch (_: Exception) {}
        sendBroadcast(Intent(BROADCAST_FINISHED))
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(
            CHANNEL_ID, "Таймер",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Отображение состояния таймера" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        try { toneGenerator.release() } catch (_: Exception) {}
    }
}
