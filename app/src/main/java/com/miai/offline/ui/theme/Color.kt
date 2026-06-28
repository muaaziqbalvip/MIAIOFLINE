package com.miai.offline.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Colors lifted straight from the MI AI logo gradient
val MiOrange = Color(0xFFFF8A3D)
val MiPink = Color(0xFFE0319B)
val MiPurple = Color(0xFF7B3FE4)
val MiBlue = Color(0xFF2FB8F5)
val MiCyan = Color(0xFF4FE3F2)

val BgDark = Color(0xFF0B0B12)
val SurfaceDark = Color(0xFF15151F)
val SurfaceElevated = Color(0xFF1E1E2C)
val TextPrimary = Color(0xFFF2F2F7)
val TextSecondary = Color(0xFF9A9AB0)
val BorderSubtle = Color(0xFF2A2A3A)

val SuccessGreen = Color(0xFF3DDC84)
val WarningAmber = Color(0xFFFFB23D)
val ErrorRed = Color(0xFFFF5C5C)

val MiBrandGradient = Brush.linearGradient(
    colors = listOf(MiOrange, MiPink, MiPurple, MiBlue)
)

val MiBrandGradientVertical = Brush.verticalGradient(
    colors = listOf(MiOrange, MiPink, MiPurple, MiBlue)
)

val MiCardGradient = Brush.linearGradient(
    colors = listOf(SurfaceElevated, SurfaceDark)
)
