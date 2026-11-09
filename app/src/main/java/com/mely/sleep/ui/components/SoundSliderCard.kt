package com.mely.sleep.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    val isActivelyEmitting = isMasterPlaying && !isMuted && volume > 0.01f
    val percent = (volume * 100).toInt()

    val cardBorderColor by animateColorAsState(
        targetValue = if (isActivelyEmitting) soundType.accentColor.copy(alpha = 0.45f) else CardBorder,
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
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Header: Icon + Titles + Mute Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Icon inside glowing circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActivelyEmitting)
                                soundType.accentColor.copy(alpha = 0.2f)
                            else
                                CardBorder.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = soundType.icon,
                        contentDescription = soundType.title,
                        tint = if (isActivelyEmitting) soundType.accentColor else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title + Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = soundType.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 17.sp
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

                // Volume percentage pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isMuted) Color(0x33EF4444)
                            else soundType.accentColor.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isMuted) "Tắt tiếng" else "$percent%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isMuted) Color(0xFFEF4444) else soundType.accentColor,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mute toggle button
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Bật/Tắt tiếng",
                        tint = if (isMuted) Color(0xFFEF4444) else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slider
            Slider(
                value = volume,
                onValueChange = { newVol ->
                    onVolumeChange(newVol)
                },
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = SliderDefaults.colors(
                    thumbColor = if (isMuted) TextMuted else soundType.accentColor,
                    activeTrackColor = if (isMuted) TextMuted.copy(alpha = 0.5f) else soundType.accentColor,
                    inactiveTrackColor = CardBorder
                )
            )
        }
    }
}
