package com.mely.sleep.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mely.sleep.model.Preset
import com.mely.sleep.model.SoundType
import com.mely.sleep.ui.components.PresetChipsRow
import com.mely.sleep.ui.components.SleepTimerSection
import com.mely.sleep.ui.components.SoundSliderCard
import com.mely.sleep.ui.theme.*

@Composable
fun SleepScreen(
    isPlaying: Boolean,
    volumes: Map<SoundType, Float>,
    muted: Map<SoundType, Boolean>,
    timerSecondsLeft: Long?,
    onTogglePlay: () -> Unit,
    onVolumeChange: (SoundType, Float) -> Unit,
    onToggleMute: (SoundType) -> Unit,
    onSelectPreset: (Preset) -> Unit,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onStopAll: () -> Unit
) {
    val activeTrackCount = volumes.count { (type, vol) ->
        val isMute = muted[type] == true
        !isMute && vol > 0.01f
    }

    // Gentle pulsing animation for Master Play button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        containerColor = NightBg,
        bottomBar = {
            // Master Play / Pause Bottom Floating Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = CardBg,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isPlaying) "Đang phát $activeTrackCount âm thanh" else "Đang tạm dừng",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPlaying) IndigoLight else TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = if (isPlaying) "Vỗ về giấc ngủ êm đềm" else "Nhấn để bắt đầu thư giãn",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Quick Stop / Reset
                        IconButton(
                            onClick = onStopAll,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CardBgHover)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Dừng",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Large Play/Pause button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.scale(if (isPlaying) pulseScale else 1.0f)
                        ) {
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary.copy(alpha = 0.25f))
                                )
                            }
                            FloatingActionButton(
                                onClick = onTogglePlay,
                                shape = CircleShape,
                                containerColor = IndigoPrimary,
                                contentColor = Color.White,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // App Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Trăng đêm",
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mêly Sleep",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 24.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bộ âm thanh tự nhiên ru êm giấc ngủ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }

                    // Night Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardBg)
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🌙 Thư giãn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = IndigoLight
                        )
                    }
                }
            }

            // Presets
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Bộ Phối Âm Gợi Ý",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PresetChipsRow(
                        onSelectPreset = onSelectPreset
                    )
                }
            }

            // Core 4 Sliders
            item {
                Text(
                    text = "Điều Chỉnh Âm Lượng Từng Kênh",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            items(SoundType.values().size) { index ->
                val type = SoundType.values()[index]
                SoundSliderCard(
                    soundType = type,
                    volume = volumes[type] ?: type.defaultVolume,
                    isMuted = muted[type] ?: false,
                    isMasterPlaying = isPlaying,
                    onVolumeChange = { newVol ->
                        onVolumeChange(type, newVol)
                    },
                    onToggleMute = {
                        onToggleMute(type)
                    }
                )
            }

            // Sleep Timer Section
            item {
                SleepTimerSection(
                    timerSecondsLeft = timerSecondsLeft,
                    onSetTimer = onSetTimer,
                    onCancelTimer = onCancelTimer
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
