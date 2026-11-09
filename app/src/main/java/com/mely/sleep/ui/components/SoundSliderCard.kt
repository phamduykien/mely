package com.mely.sleep.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mely.sleep.model.SoundType
import com.mely.sleep.ui.theme.*

@Composable
fun SoundSliderCard(
    soundType: SoundType,
    volume: Float,
    isMuted: Boolean,
    isMasterPlaying: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A channel is considered "turned off" if explicitly muted OR volume is 0
    val isTurnedOff = isMuted || volume < 0.01f
    val isAudibleNow = isMasterPlaying && !isTurnedOff
    val displayPercent = if (isTurnedOff) 0 else (volume * 100).toInt()

    // Local slider position tracking user touch directly for ultra-smooth 120 FPS dragging
    var localPosition by remember(volume, isMuted) {
        mutableFloatStateOf(if (isMuted) 0f else volume)
    }

    val cardBorderColor by animateColorAsState(
        targetValue = if (isAudibleNow) soundType.accentColor.copy(alpha = 0.5f) else CardBorder,
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header: Category Icon + Title + Mute Badge + Speaker Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAudibleNow)
                                soundType.accentColor.copy(alpha = 0.2f)
                            else
                                CardBorder.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = soundType.icon,
                        contentDescription = soundType.title,
                        tint = if (isAudibleNow) soundType.accentColor else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = soundType.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTurnedOff) TextSecondary else TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = soundType.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                // Volume % / TẮT badge (click to toggle mute)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isTurnedOff) Color(0x33EF4444)
                            else soundType.accentColor.copy(alpha = 0.15f)
                        )
                        .clickable { onToggleMute() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTurnedOff) "TẮT" else "$displayPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isTurnedOff) Color(0xFFEF4444) else soundType.accentColor,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // BIỂU TƯỢNG CÁI LOA TRÊN TỪNG SLIDER:
                // - Khi TẮT: Hiển thị loa có dấu gạch chéo (Icons.Default.VolumeOff)
                // - Khi BẬT: Hiển thị loa phát sóng (Icons.Default.VolumeUp)
                FilledTonalIconButton(
                    onClick = onToggleMute,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isTurnedOff)
                            Color(0x22EF4444)
                        else
                            soundType.accentColor.copy(alpha = 0.18f),
                        contentColor = if (isTurnedOff)
                            Color(0xFFEF4444)
                        else
                            soundType.accentColor
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isTurnedOff) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isTurnedOff) "Đang tắt - Nhấn để bật loa" else "Đang bật - Nhấn để tắt loa",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider Row: Kéo là thay đổi giá trị và nghe âm thanh lập tức
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biểu tượng loa nhỏ ở đầu thanh slider (hiện dấu gạch khi tắt)
                Icon(
                    imageVector = if (isTurnedOff) Icons.Default.VolumeOff else Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = if (isTurnedOff) Color(0xFFEF4444) else TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            localPosition = 0f
                            onVolumeChange(0f)
                        }
                )

                Slider(
                    value = localPosition,
                    onValueChange = { newVol ->
                        localPosition = newVol
                        onVolumeChange(newVol)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(36.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isTurnedOff) TextMuted else soundType.accentColor,
                        activeTrackColor = if (isTurnedOff) TextMuted.copy(alpha = 0.3f) else soundType.accentColor,
                        inactiveTrackColor = CardBorder
                    )
                )

                // Biểu tượng loa to ở cuối thanh slider
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isTurnedOff) TextMuted else soundType.accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            localPosition = 1f
                            onVolumeChange(1f)
                        }
                )
            }
        }
    }
}
