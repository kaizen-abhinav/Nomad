package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NomadDarkColorScheme = darkColorScheme(
    primary = ImmersiveAccent,
    onPrimary = ImmersiveSurface,
    primaryContainer = ImmersiveSurfaceElevated,
    onPrimaryContainer = ImmersiveAccent,
    secondary = ImmersiveOrange,
    onSecondary = ImmersiveBg,
    secondaryContainer = ImmersiveSurfaceVariant,
    onSecondaryContainer = ImmersiveOrangeBright,
    tertiary = ImmersiveRed,
    onTertiary = Color.White,
    background = ImmersiveBg,
    onBackground = TextPrimary,
    surface = ImmersiveSurface,
    onSurface = TextPrimary,
    surfaceVariant = ImmersiveSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ImmersiveBorder,
    outlineVariant = ImmersiveBorderWarm
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Nomad is strictly an offline tactical dark node for battery saving in grid-down scenarios
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NomadDarkColorScheme,
        typography = Typography,
        content = content
    )
}
