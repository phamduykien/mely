package com.mely.sleep

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.mely.sleep.model.Preset
import com.mely.sleep.model.SoundType
import com.mely.sleep.service.SleepAudioService
import com.mely.sleep.ui.SleepScreen
import com.mely.sleep.ui.theme.MelySleepTheme

class MainActivity : ComponentActivity() {

    private var sleepService: SleepAudioService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SleepAudioService.LocalBinder
            sleepService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sleepService = null
            isBound = false
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start and bind service
        val serviceIntent = Intent(this, SleepAudioService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        setContent {
            MelySleepTheme {
                val service = sleepService

                // Observe service states or default states
                val isPlaying by (service?.isPlaying?.collectAsState()
                    ?: remember { mutableStateOf(false) })
                val volumes by (service?.volumes?.collectAsState()
                    ?: remember { mutableStateOf(SoundType.values().associateWith { it.defaultVolume }) })
                val muted by (service?.muted?.collectAsState()
                    ?: remember { mutableStateOf(SoundType.values().associateWith { false }) })
                val timerSecondsLeft by (service?.timerSecondsLeft?.collectAsState()
                    ?: remember { mutableStateOf<Long?>(null) })

                SleepScreen(
                    isPlaying = isPlaying,
                    volumes = volumes,
                    muted = muted,
                    timerSecondsLeft = timerSecondsLeft,
                    onTogglePlay = {
                        val s = sleepService ?: return@SleepScreen
                        if (isPlaying) s.pause() else s.play()
                    },
                    onVolumeChange = { soundType, volume ->
                        sleepService?.setSoundVolume(soundType, volume)
                    },
                    onToggleMute = { soundType ->
                        sleepService?.toggleMute(soundType)
                    },
                    onSelectPreset = { preset: Preset ->
                        sleepService?.applyPreset(preset.volumes)
                    },
                    onSetTimer = { minutes ->
                        sleepService?.setTimer(minutes)
                    },
                    onCancelTimer = {
                        sleepService?.cancelTimer()
                    },
                    onStopAll = {
                        sleepService?.stopPlayback()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
