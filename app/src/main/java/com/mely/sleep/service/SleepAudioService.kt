package com.mely.sleep.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.mely.sleep.MainActivity
import com.mely.sleep.R
import com.mely.sleep.audio.AudioMixer
import com.mely.sleep.model.SoundType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SleepAudioService : Service() {

    private val binder = LocalBinder()
    private lateinit var audioMixer: AudioMixer
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaSession: MediaSessionCompat? = null

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
    private var notifyDebounceJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): SleepAudioService = this@SleepAudioService
    }

    override fun onCreate() {
        super.onCreate()
        audioMixer = AudioMixer(this)
        createNotificationChannel()
        initMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stopPlayback()
            ACTION_TOGGLE -> {
                if (_isPlaying.value) pause() else play()
            }
        }
        return START_NOT_STICKY
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "MelySleepSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    play()
                }

                override fun onPause() {
                    pause()
                }

                override fun onStop() {
                    stopPlayback()
                }
            })
            isActive = true
        }
    }

    private fun updateMediaSessionState(playing: Boolean) {
        val state = if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP

        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        )

        val activeNames = getActiveSoundsDescription()
        val artBitmap = try {
            BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        } catch (e: Exception) {
            null
        }

        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Mêly Sleep")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeNames)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artBitmap)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, artBitmap)
                .build()
        )
    }

    private fun getActiveSoundsDescription(): String {
        val active = _volumes.value.filter { (type, vol) ->
            _muted.value[type] != true && vol > 0.01f
        }.keys.map { it.title }

        return if (active.isNotEmpty()) {
            "Đang phát: " + active.joinToString(", ")
        } else {
            "Âm thanh thư giãn ru ngủ"
        }
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
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(playing: Boolean): Notification {
        updateMediaSessionState(playing)

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

        val timerText = _timerSecondsLeft.value?.let {
            val m = it / 60
            val s = it % 60
            " • Tắt sau: %02d:%02d".format(m, s)
        } ?: ""

        val artBitmap = try {
            BitmapFactory.decodeResource(resources, R.drawable.app_icon)
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(artBitmap)
            .setContentTitle("Mêly Sleep" + timerText)
            .setContentText(getActiveSoundsDescription())
            .setContentIntent(contentIntent)
            .setOngoing(playing)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (playing) "Tạm dừng" else "Bật phát",
                playPauseIntent
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)
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
        val clamped = volume.coerceIn(0f, 1f)
        updated[type] = clamped
        _volumes.value = updated

        // If user drags slider up, automatically unmute
        if (clamped > 0.01f && _muted.value[type] == true) {
            val mutedCopy = _muted.value.toMutableMap()
            mutedCopy[type] = false
            _muted.value = mutedCopy
        } else if (clamped < 0.01f) {
            val mutedCopy = _muted.value.toMutableMap()
            mutedCopy[type] = true
            _muted.value = mutedCopy
        }

        val effective = if (_muted.value[type] == true) 0f else clamped
        audioMixer.updateVolume(type, effective, _isPlaying.value)

        // Debounce notification updates
        if (_isPlaying.value) {
            notifyDebounceJob?.cancel()
            notifyDebounceJob = serviceScope.launch {
                delay(500L)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, buildNotification(true))
            }
        }
    }

    fun toggleMute(type: SoundType) {
        val updatedMuted = _muted.value.toMutableMap()
        val isCurrentlyOff = (updatedMuted[type] == true) || ((_volumes.value[type] ?: 0f) < 0.01f)
        val newMuted = !isCurrentlyOff
        updatedMuted[type] = newMuted
        _muted.value = updatedMuted

        val volMap = _volumes.value.toMutableMap()
        // If turning on and volume was 0%, restore default volume so sound is heard immediately
        if (!newMuted && (volMap[type] ?: 0f) < 0.05f) {
            volMap[type] = type.defaultVolume
            _volumes.value = volMap
        }

        val effective = if (newMuted) 0f else (volMap[type] ?: 0f)
        audioMixer.updateVolume(type, effective, _isPlaying.value)

        if (_isPlaying.value) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(true))
        }
    }

    fun applyPreset(presetMap: Map<SoundType, Float>) {
        val currentVols = _volumes.value.toMutableMap()
        val currentMuted = _muted.value.toMutableMap()

        for ((type, vol) in presetMap) {
            currentVols[type] = vol
            if (vol > 0.01f) {
                currentMuted[type] = false
            } else {
                currentMuted[type] = true
            }
            val effective = if (currentMuted[type] == true) 0f else vol
            audioMixer.updateVolume(type, effective, _isPlaying.value)
        }
        _volumes.value = currentVols
        _muted.value = currentMuted

        if (_isPlaying.value) {
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
        notifyDebounceJob?.cancel()
        mediaSession?.release()
        audioMixer.release()
    }

    companion object {
        const val CHANNEL_ID = "mely_sleep_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.mely.sleep.action.PLAY"
        const val ACTION_PAUSE = "com.mely.sleep.action.PAUSE"
        const val ACTION_STOP = "com.mely.sleep.action.STOP"
        const val ACTION_TOGGLE = "com.mely.sleep.action.TOGGLE"
    }
}
