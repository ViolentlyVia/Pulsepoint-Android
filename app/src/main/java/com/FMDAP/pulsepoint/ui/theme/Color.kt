package com.FMDAP.pulsepoint.ui.theme

import androidx.compose.ui.graphics.Color

val Cyan80   = Color(0xFF80D8FF)
val Cyan40   = Color(0xFF0097A7)
val Teal80   = Color(0xFF80CBC4)
val Teal40   = Color(0xFF00796B)

val OnlineGreen  = Color(0xFF4CAF50)
val OfflineRed   = Color(0xFFF44336)
val WarnOrange   = Color(0xFFFF9800)
val NeutralGray  = Color(0xFF9E9E9E)

fun statColor(value: Double?) = when {
    value == null    -> NeutralGray
    value < 50.0     -> OnlineGreen
    value < 80.0     -> WarnOrange
    else             -> OfflineRed
}
