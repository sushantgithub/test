package com.sushant.ringcompanion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFD4AF77)
val GoldSoft = Color(0xFFE8D4A8)
val Ink = Color(0xFF0B0C10)
val Panel = Color(0xFF16181F)
val PanelEdge = Color(0xFF2A2E3A)
val Mist = Color(0xFFB7BCC8)

private val scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    secondary = GoldSoft,
    background = Ink,
    surface = Panel,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = PanelEdge
)

@Composable
fun RingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
