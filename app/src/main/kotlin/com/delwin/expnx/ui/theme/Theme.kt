package com.delwin.expnx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OliveAccent,
    secondary = TanAccent,
    tertiary = BurntOrangeAccent,
    background = NearBlack,
    surface = SurfaceDark,
    onPrimary = NearBlack,
    onSecondary = NearBlack,
    onTertiary = NearBlack,
    onBackground = CreamText,
    onSurface = CreamText,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = TanAccent,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = MutedCream,
    outline = OliveDim
)

// Unified dark/premium aesthetics, ignoring light scheme
private val LightColorScheme = DarkColorScheme

@Composable
fun EXPNXTheme(
    darkTheme: Boolean = true, // Force dark theme for the new aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NearBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

