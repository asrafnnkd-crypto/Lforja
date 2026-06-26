package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFFFD700),      // Premium Gold Accent
    secondary = Color(0xFF9F70FF),    // Vivid Purple Accent
    tertiary = Color(0xFFFFC107),     // Rich Amber Gold
    background = Color(0xFF0C071A),   // Glossy cinematic deep purple-black background
    surface = Color(0xFF170F2E),      // Glossy dark purple card surface
    onPrimary = Color(0xFF0C071A),    // High-contrast deep purple text on gold buttons
    onSecondary = Color.White,
    onTertiary = Color(0xFF0C071A),
    onBackground = Color(0xFFF8FAFC), // Ultra soft white
    onSurface = Color(0xFFF8FAFC),    // Ultra soft white
    surfaceVariant = Color(0xFF2B1C4B) // Luxurious slate purple highlight border
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF7E36FF),      // Lfraja Purple
    secondary = Color(0xFF6B21A8),    // Deep Purple Accent
    tertiary = Color(0xFF0284C7),     // Ocean Blue
    background = Color(0xFFF8FAFC),   // Clean off-white background
    surface = Color.White,            // Pure white cards
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), // Dark slate text
    onSurface = Color(0xFF0F172A),    // Dark slate text on cards
    surfaceVariant = Color(0xFFF1F5F9) // Light gray boundary shading
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colors = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
