package com.example.kinetiqprotocalintegration.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KinetiqDarkColorScheme = darkColorScheme(
    primary = KinetiqAccentGreen,
    onPrimary = KinetiqTextPrimary,
    secondary = KinetiqAccentPink,
    onSecondary = KinetiqTextPrimary,
    tertiary = KinetiqAccentBlue,
    onTertiary = KinetiqTextPrimary,
    background = KinetiqBackground,
    onBackground = KinetiqTextPrimary,
    surface = KinetiqCardBackground,
    onSurface = KinetiqTextPrimary,
    error = Color(0xFFCF6679), // Default error color
    onError = Color.Black // Default on error color
)

@Composable
fun KinetiqProtocalIntegrationTheme(
    darkTheme: Boolean = true, // Force dark theme
    content: @Composable () -> Unit
) {
    // Always use the Kinetiq dark color scheme
    val colorScheme = KinetiqDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography is defined elsewhere
        content = content
    )
}