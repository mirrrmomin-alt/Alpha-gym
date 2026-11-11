package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlphaGymSilverRedColorScheme = lightColorScheme(
    primary = ElectricLime,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEE2E2),
    onPrimaryContainer = Color(0xFF7F1D1D),
    secondary = CyberCyan,
    onSecondary = Color.White,
    secondaryContainer = ElevatedSlate,
    onSecondaryContainer = CrispWhite,
    tertiary = BlazeAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    error = CrimsonPulse,
    onError = Color.White,
    background = ObsidianSlate,
    onBackground = CrispWhite,
    surface = DeepSlate,
    onSurface = CrispWhite,
    surfaceVariant = ElevatedSlate,
    onSurfaceVariant = MutedSteel,
    outline = BorderSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlphaGymSilverRedColorScheme,
        typography = Typography,
        content = content
    )
}
