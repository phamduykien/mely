package com.mely.sleep.model

import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mely.sleep.R

enum class SoundType(
    val title: String,
    val subtitle: String,
    @RawRes val rawResId: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val defaultVolume: Float
) {
    RAIN(
        title = "Tiếng Mưa",
        subtitle = "Mưa rào êm dịu, vỗ về giấc ngủ",
        rawResId = R.raw.rain,
        icon = Icons.Default.WaterDrop,
        accentColor = Color(0xFF38BDF8), // Soft sky blue
        defaultVolume = 0.6f
    ),
    WIND(
        title = "Tiếng Gió",
        subtitle = "Gió đêm vi vu nhẹ nhàng qua tán cây",
        rawResId = R.raw.wind,
        icon = Icons.Default.Air,
        accentColor = Color(0xFFA78BFA), // Soft purple
        defaultVolume = 0.4f
    ),
    CRICKETS(
        title = "Dế Đồng Quê",
        subtitle = "Tiếng côn trùng đồng nội đêm hè rả rích",
        rawResId = R.raw.crickets,
        icon = Icons.Default.BugReport,
        accentColor = Color(0xFF4ADE80), // Soft meadow green
        defaultVolume = 0.5f
    ),
    NOISE(
        title = "Âm Thanh Noise",
        subtitle = "Tiếng ồn trắng / Pink noise cách ly tạp âm",
        rawResId = R.raw.noise,
        icon = Icons.Default.GraphicEq,
        accentColor = Color(0xFFFB923C), // Warm orange
        defaultVolume = 0.3f
    )
}

data class SoundState(
    val type: SoundType,
    val volume: Float, // 0.0f to 1.0f
    val isMuted: Boolean = false
) {
    val effectiveVolume: Float
        get() = if (isMuted) 0f else volume
}

data class Preset(
    val name: String,
    val iconEmoji: String,
    val volumes: Map<SoundType, Float>
)
