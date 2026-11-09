package com.mely.sleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mely.sleep.MainActivity
import com.mely.sleep.R
import com.mely.sleep.audio.AudioMixer
import com.mely.sleep.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SleepAudioService : Service() {

    private val binder = LocalBinder()
    private lateinit var audioMixer: AudioMixer
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volumes = MutableStateFlow<Map<SoundType, Float>>(
        SoundType.values().associateWith { it.defaultVolume }
    )
    val volumes: StateFlow<Map<SoundType, Float>> = _volumes.asStateFlow()

    private val _muted = MutableStateFlow<Map<SoundType, Boolean>>(
        SoundType.values().associateWith { false }
    )
    val muted: StateFlow<Map<SoundType, Boolean>> = _muted.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow<Long?>(null)
    val timerSecondsLeft: StateFlow<Long?> = _timerSecondsLeft.asStateFlow()

    private var timerJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): SleepAudioService = this@SleepAudioService
    }

    override fun onCreate() {
        super.onCreate()
        audioMixer = AudioMixer(this)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(playing: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SleepAudioService::class.java).apply {
                action = if (playing) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, SleepAudioService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activeSounds = _volumes.value.filter { entry ->
            val isMute = _muted.value[entry.key] == true
            !isMute && entry.value > 0.05f
        }.keys.joinToString(", ") { it.title }

        val contentText = if (activeSounds.isNotEmpty()) {
            "Đang phát: $activeSounds"
        } else {
            "Âm thanh thư giãn giúp ngủ ngon"
        }

        val timerText = _timerSecondsLeft.value?.let {
            val m = it / 60
            val s = it % 60
            " • Tự tắt: %02d:%02d".format(m, s)
        } ?: ""

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mêly Sleep" + timerText)
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setOngoing(playing)
            .setOnlyAlertOnce(true)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "Tạm dừng" else "Phát",
                playPauseIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dừng",
                stopIntent
            )

        return builder.build()
    }

    fun play() {
        _isPlaying.value = true
        val currentStates = _volumes.value.mapValues { (type, vol) ->
            if (_muted.value[type] == true) 0f else vol
        }
        audioMixer.startPlayback(currentStates)

        val notification = buildNotification(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    fun pause() {
        _isPlaying.value = false
        audioMixer.pauseAll()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(false))
    }

    fun stopPlayback() {
        _isPlaying.value = false
        cancelTimer()
        audioMixer.pauseAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun setSoundVolume(type: SoundType, volume: Float) {
        val updated = _volumes.value.toMutableMap()
        updated[type] = volume.coerceIn(0f, 1f)
        _volumes.value = updated

        val effective = if (_muted.value[type] == true) 0f else updated[type] ?: 0f
        audioMixer.updateVolume(type, effective, _isPlaying.value)

        if (_isPlaying.value) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(true))
        }
    }

    fun toggleMute(type: SoundType) {
        val updated = _muted.value.toMutableMap()
        val newState = !(updated[type] ?: false)
        updated[type] = newState
        _muted.value = updated

        val vol = if (newState) 0f else (_volumes.value[type] ?: 0f)
        audioMixer.updateVolume(type, vol, _isPlaying.value)

        if (_isPlaying.value) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(true))
        }
    }

    fun applyPreset(presetMap: Map<SoundType, Float>) {
        val current = _volumes.value.toMutableMap()
        for ((type, vol) in presetMap) {
            current[type] = vol
            val effective = if (_muted.value[type] == true) 0f else vol
            audioMixer.updateVolume(type, effective, _isPlaying.value)
        }
        _volumes.value = current

        if (!_isPlaying.value) {
            play()
        } else {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(true))
        }
    }

    fun setTimer(minutes: Int) {
        timerJob?.cancel()
        val totalSeconds = minutes * 60L
        _timerSecondsLeft.value = totalSeconds

        timerJob = serviceScope.launch {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining--
                _timerSecondsLeft.value = remaining

                // Fade out smoothly in the last 15 seconds
                if (remaining <= 15) {
                    val fadeFraction = remaining / 15.0f
                    audioMixer.setMasterFadeScale(fadeFraction)
                }

                if (remaining % 30 == 0L && _isPlaying.value) {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, buildNotification(true))
                }
            }

            if (remaining <= 0) {
                _timerSecondsLeft.value = null
                stopPlayback()
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerSecondsLeft.value = null
        audioMixer.setMasterFadeScale(1.0f)
        if (_isPlaying.value) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(true))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        audioMixer.release()
    }

    companion object {
        const val CHANNEL_ID = "mely_sleep_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.mely.sleep.action.PLAY"
        const val ACTION_PAUSE = "com.mely.sleep.action.PAUSE"
        const val ACTION_STOP = "com.mely.sleep.action.STOP"
    }
}
