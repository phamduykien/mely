package com.mely.sleep.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mely.sleep.model.Preset
import com.mely.sleep.model.SoundType
import com.mely.sleep.ui.theme.*

val SAMPLE_PRESETS = listOf(
    Preset(
        name = "Đêm Mưa Rào",
        iconEmoji = "🌧️",
        volumes = mapOf(
            SoundType.RAIN to 0.85f,
            SoundType.WIND to 0.40f,
            SoundType.CRICKETS to 0.00f,
            SoundType.NOISE to 0.15f
        )
    ),
    Preset(
        name = "Đồng Quê Yên Ả",
        iconEmoji = "🦗",
        volumes = mapOf(
            SoundType.CRICKETS to 0.75f,
            SoundType.WIND to 0.50f,
            SoundType.RAIN to 0.00f,
            SoundType.NOISE to 0.10f
        )
    ),
    Preset(
        name = "Giấc Ngủ Sâu",
        iconEmoji = "💤",
        volumes = mapOf(
            SoundType.NOISE to 0.65f,
            SoundType.RAIN to 0.45f,
            SoundType.WIND to 0.25f,
            SoundType.CRICKETS to 0.15f
        )
    ),
    Preset(
        name = "Gió Núi Vi Vu",
        iconEmoji = "🍃",
        volumes = mapOf(
            SoundType.WIND to 0.80f,
            SoundType.CRICKETS to 0.40f,
            SoundType.RAIN to 0.15f,
            SoundType.NOISE to 0.00f
        )
    ),
    Preset(
        name = "Tắt Tất Cả",
        iconEmoji = "🔇",
        volumes = mapOf(
            SoundType.RAIN to 0.00f,
            SoundType.WIND to 0.00f,
            SoundType.CRICKETS to 0.00f,
            SoundType.NOISE to 0.00f
        )
    )
)

@Composable
fun PresetChipsRow(
    onSelectPreset: (Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SAMPLE_PRESETS.forEach { preset ->
            SuggestionChip(
                onClick = { onSelectPreset(preset) },
                label = {
                    Text(
                        text = "${preset.iconEmoji} ${preset.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = CardBgHover
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    borderColor = CardBorder
                )
            )
        }
    }
}
