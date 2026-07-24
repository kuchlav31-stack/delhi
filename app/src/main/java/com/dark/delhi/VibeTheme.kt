package com.dark.delhi

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object VibeTheme {
    // --- Core Brand Colors ---
    val DarkBg = Color(0xFF000000)      // Pure Black for OLED
    val CardBg = Color(0xFF151517)      // Slightly lighter for depth
    val NeonPink = Color(0xFFFF2D55)    // Main Brand Color
    val NeonPurple = Color(0xFFBC00FF)  // Accent/Gradient Color
    val VerifiedBlue = Color(0xFF00B2FF) // Verification Tick Color

    // --- Text & Utility Colors ---
    val TextMain = Color(0xFFFFFFFF)    // Primary Text
    val TextSub = Color(0xFF8E8E93)     // Secondary/Hint Text
    val SuccessGreen = Color(0xFF4CD964) // For "LIKE" stamps
    val ErrorRed = Color(0xFFFF3B30)     // For "NOPE" stamps

    // --- Premium Gradients ---
    // Use this for Main Buttons
    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(NeonPink, NeonPurple)
    )

    // Use this for Bottom Overlays on Cards
    val GlassOverlay = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
    )

    // Use this for background glows
    fun glowGradient(color: Color) = Brush.radialGradient(
        colors = listOf(color.copy(alpha = 0.15f), Color.Transparent)
    )
}

// --- Global Theme Wrapper ---
@Composable
fun DilliDateTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = VibeTheme.NeonPink,
        secondary = VibeTheme.NeonPurple,
        background = VibeTheme.DarkBg,
        surface = VibeTheme.CardBg,
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}

fun getMetroColor(line: String): Color {
    return when {
        line.contains("Yellow", ignoreCase = true) -> Color(0xFFFFD700)
        line.contains("Blue", ignoreCase = true) -> Color(0xFF0072BB)
        line.contains("Pink", ignoreCase = true) -> Color(0xFFFF91AF)
        line.contains("Violet", ignoreCase = true) -> Color(0xFF8A2BE2)
        line.contains("Red", ignoreCase = true) -> Color(0xFFFF0000)
        line.contains("Green", ignoreCase = true) -> Color(0xFF00B25B)
        line.contains("Magenta", ignoreCase = true) -> Color(0xFF8B008B)
        line.contains("Orange", ignoreCase = true) -> Color(0xFFFF8C00)
        else -> Color(0xFFBC00FF) // Default Neon Purple if no match
    }
}