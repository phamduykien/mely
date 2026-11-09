package com.mely.sleep.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mely.sleep.R
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
    onCancelTimer: () -> Unit
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
            // Thanh điều khiển đáy: Chỉ duy nhất 1 nút thay đổi trạng thái Bật / Tắt
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(26.dp),
                color = CardBg,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPlaying) "Đang phát $activeTrackCount âm thanh" else "Đang tắt âm thanh",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPlaying) IndigoLight else TextSecondary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = if (isPlaying) "Vỗ về giấc ngủ êm đềm" else "Chạm nút để bắt đầu phát",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Nút duy nhất điều khiển Bật / Tắt (Play / Pause)
                    Button(
                        onClick = onTogglePlay,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.scale(if (isPlaying) pulseScale else 1.0f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) IndigoPrimary else CardBgHover,
                            contentColor = if (isPlaying) Color.White else IndigoLight
                        ),
                        border = if (!isPlaying)
                            androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
                        else null,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Tạm dừng" else "Bật phát",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "Tạm dừng" else "Bật phát",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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

            // Serene Sleeping Woman Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "Người phụ nữ ngủ ngon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(115.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Giấc Ngủ An Nhiên",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hòa quyện âm thanh mưa, gió, côn trùng & noise để ru êm tâm trí.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
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
