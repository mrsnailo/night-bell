package com.nightbell.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAmber,
    onPrimary = DarkBackground,
    primaryContainer = AmberMutedBg,
    onPrimaryContainer = PrimaryAmber,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceNav,
    onSurfaceVariant = TextMuted,
    outline = DarkBorder,
    outlineVariant = DashedBorder
)

@Composable
fun NightBellTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkSurfaceNav.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
