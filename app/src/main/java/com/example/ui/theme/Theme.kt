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
    primary = Color(0xFF9F70FF),      // Lfraja Vivid Purple
    secondary = Color(0xFFC084FC),    // Light Purple Accent
    tertiary = Color(0xFF38BDF8),     // Teal/Ice Blue for secondary actions
    background = Color(0xFF0B0715),   // Premium cinematic deep purple-black background
    surface = Color(0xFF150F26),      // Premium card dark surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF1F5F9), // Soft light-gray text
    onSurface = Color(0xFFF1F5F9),    // Soft light-gray text on cards
    surfaceVariant = Color(0xFF241B3B) // Border/divider shade in dark mode
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
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
