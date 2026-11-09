package com.mely.sleep.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val isActivelyAudible = isMasterPlaying && !isMuted && volume > 0.01f
    val percent = if (isMuted) 0 else (volume * 100).toInt()

    val cardBorderColor by animateColorAsState(
        targetValue = if (isActivelyAudible) soundType.accentColor.copy(alpha = 0.5f) else CardBorder,
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
            // Header Row: Sound Category Icon + Titles + Speaker Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound category icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActivelyAudible)
                                soundType.accentColor.copy(alpha = 0.2f)
                            else
                                CardBorder.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = soundType.icon,
                        contentDescription = soundType.title,
                        tint = if (isActivelyAudible) soundType.accentColor else TextMuted,
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
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = soundType.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                // Volume % badge (clickable to toggle mute as well)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isMuted) Color(0x33EF4444)
                            else soundType.accentColor.copy(alpha = 0.15f)
                        )
                        .clickable { onToggleMute() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isMuted) "TẮT" else "$percent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) Color(0xFFEF4444) else soundType.accentColor,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prominent Speaker Icon Button: Bật/Tắt âm thanh này ngay lập tức
                FilledTonalIconButton(
                    onClick = onToggleMute,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isMuted)
                            Color(0x22EF4444)
                        else
                            soundType.accentColor.copy(alpha = 0.18f),
                        contentColor = if (isMuted)
                            Color(0xFFEF4444)
                        else
                            soundType.accentColor
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Bật âm thanh" else "Tắt âm thanh",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider: Kéo là thay đổi âm thanh real-time tức thì
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini speaker at slider start
                Icon(
                    imageVector = Icons.Default.VolumeMute,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onVolumeChange(0f) }
                )

                Slider(
                    value = if (isMuted) 0f else volume,
                    onValueChange = { newVol ->
                        onVolumeChange(newVol)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(36.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isMuted) TextMuted else soundType.accentColor,
                        activeTrackColor = if (isMuted) TextMuted.copy(alpha = 0.3f) else soundType.accentColor,
                        inactiveTrackColor = CardBorder
                    )
                )

                // High speaker at slider end
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isMuted) TextMuted else soundType.accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onVolumeChange(1f) }
                )
            }
        }
    }
}
