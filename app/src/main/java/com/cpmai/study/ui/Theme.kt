package com.cpmai.study.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF7C6AF7)
val Background = Color(0xFF0D0F1A)
val CardBg = Color(0xFF12152A)
val SurfaceAlt = Color(0xFF1A1D2E)
val Border = Color(0xFF1E2235)
val TextMain = Color(0xFFE0E0E0)
val TextMuted = Color(0xFF9999AA)
val Green = Color(0xFF4CAF50)
val Blue = Color(0xFF2196F3)
val Orange = Color(0xFFFF9800)
val Magenta = Color(0xFFE040FB)
val Cyan = Color(0xFF00BCD4)

fun categoryColor(category: String): Color = when {
    category.contains("Classif", true) || category.contains("Ensemble", true) || category.contains("Regression", true) -> Green
    category.contains("Cluster", true) || category.contains("Soft", true) -> Blue
    category.contains("Learning", true) -> Orange
    category.contains("Metric", true) || category.contains("Cheat", true) -> Cyan
    else -> Magenta
}

val CpmaiColors: ColorScheme = darkColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    background = Background,
    onBackground = TextMain,
    surface = CardBg,
    onSurface = TextMain,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextMuted,
    outline = Border,
    secondary = Cyan,
)
