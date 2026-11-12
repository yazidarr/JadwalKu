package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = OnNeonGreen,
    primaryContainer = NeonGreenContainer,
    onPrimaryContainer = NeonGreenLight,

    secondary = ElectricBlue,
    onSecondary = OnElectricBlue,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlueLight,

    tertiary = CatPersonal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF262045),
    onTertiaryContainer = Color(0xFFC7D2FE),

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    outline = DarkBorder,
    outlineVariant = Color(0xFF333C4D),

    error = Color(0xFFFF5252),
    onError = Color.White
)

@Composable
fun JadwalKuTheme(
    darkTheme: Boolean = true, // Defaults to the requested elegant dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography,
        content = content
    )
}
