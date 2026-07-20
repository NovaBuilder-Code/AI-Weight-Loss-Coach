package com.novaai.calorietracker.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Whether the dark palette is active. Written only by NovaAITheme; every
 * color below reads it, so the whole UI repaints when it flips.
 */
internal object NovaPalette {
    var dark by mutableStateOf(true)
}

// — Brand palette (each colour resolves against the active light/dark palette) —
val NavyDeep: Color      get() = if (NovaPalette.dark) Color(0xFF090F1C) else Color(0xFFF3F5FA)   // page background
val NavySurface: Color   get() = if (NovaPalette.dark) Color(0xFF111827) else Color(0xFFFFFFFF)   // card / surface base
val NavyElevated: Color  get() = if (NovaPalette.dark) Color(0xFF1A2640) else Color(0xFFE9EEF6)   // elevated surfaces
val NavyBorder: Color    get() = if (NovaPalette.dark) Color(0xFF243252) else Color(0xFFD5DDEB)   // subtle borders

val GreenPrimary   = Color(0xFF00D48A)   // main accent
val GreenLight     = Color(0xFF33DDAA)   // hover / lighter tint
val GreenDim       = Color(0xFF00A86E)   // pressed state
val GreenContainer = Color(0xFF00D48A1A) // 10 % tint surface  (0xAARRGGBB)

// Green-tinted card background (Home AI insight card)
val GreenTintCard: Color get() = if (NovaPalette.dark) Color(0xFF0D2A1F) else Color(0xFFDCF3E9)

// "White" is the primary content colour: white on dark, deep navy on light.
val White: Color         get() = if (NovaPalette.dark) Color(0xFFFFFFFF) else Color(0xFF10192B)
val WhiteAlpha80: Color  get() = if (NovaPalette.dark) Color(0xCCFFFFFF) else Color(0xCC10192B)
val WhiteAlpha60: Color  get() = if (NovaPalette.dark) Color(0x99FFFFFF) else Color(0x9910192B)
val WhiteAlpha30: Color  get() = if (NovaPalette.dark) Color(0x4DFFFFFF) else Color(0x5910192B)
val WhiteAlpha10: Color  get() = if (NovaPalette.dark) Color(0x1AFFFFFF) else Color(0x1A10192B)

// — Semantic —
val ErrorRed       = Color(0xFFFF4D6A)
val WarningAmber   = Color(0xFFFFAB40)
val InfoBlue       = Color(0xFF40A9FF)
