package com.mely.sleep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mely.sleep.ui.theme.*

@Composable
fun SleepTimerSection(
    timerSecondsLeft: Long?,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerOptions = listOf(15, 30, 45, 60, 90, 120)
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Hẹn giờ",
                    tint = IndigoLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hẹn Giờ Tắt",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                if (timerSecondsLeft != null) {
                    val minutes = timerSecondsLeft / 60
                    val seconds = timerSecondsLeft % 60
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(IndigoPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndigoLight,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onCancelTimer,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Huỷ hẹn giờ",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timerOptions.forEach { mins ->
                    val isSelected = timerSecondsLeft != null && (timerSecondsLeft / 60) <= mins && (timerSecondsLeft / 60) > mins - 15
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetTimer(mins) },
                        label = {
                            Text(
                                text = "${mins}p",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CardBgHover,
                            labelColor = TextSecondary,
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = CardBorder,
                            selectedBorderColor = IndigoPrimary
                        )
                    )
                }
            }
        }
    }
}
