package com.jarvis.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    secondary = JarvisAmber,
    background = JarvisBackground,
    surface = JarvisPanel,
    error = JarvisError,
    onPrimary = JarvisBackground,
    onBackground = JarvisText,
    onSurface = JarvisText,
)

val JarvisMonoFamily = FontFamily.Monospace

val JarvisTypography = androidx.compose.material3.Typography(
    headlineMedium = TextStyle(fontFamily = JarvisMonoFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = JarvisMonoFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = JarvisMonoFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = JarvisMonoFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.5.sp),
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = JarvisTypography,
        content = content,
    )
}
