package com.miko.reader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val M3ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

enum class AppTheme(val label: String) {
    Default("Lavender"),
    Forest("Forest"),
    Midnight("Midnight"),
    Rose("Rose"),
    Monochrome("Monochrome"),
    Dynamic("Dynamic (M3)")
}

@Composable
fun MikoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    theme: AppTheme = AppTheme.Dynamic,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (theme) {
        AppTheme.Dynamic -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        AppTheme.Monochrome -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = androidx.compose.ui.graphics.Color.White,
                    onPrimary = androidx.compose.ui.graphics.Color.Black,
                    primaryContainer = androidx.compose.ui.graphics.Color.White,
                    onPrimaryContainer = androidx.compose.ui.graphics.Color.Black,
                    secondary = androidx.compose.ui.graphics.Color.White,
                    onSecondary = androidx.compose.ui.graphics.Color.Black,
                    background = androidx.compose.ui.graphics.Color.Black,
                    onBackground = androidx.compose.ui.graphics.Color.White,
                    surface = androidx.compose.ui.graphics.Color.Black,
                    onSurface = androidx.compose.ui.graphics.Color.White
                )
            } else {
                lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color.Black,
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    primaryContainer = androidx.compose.ui.graphics.Color.Black,
                    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
                    secondary = androidx.compose.ui.graphics.Color.Black,
                    onSecondary = androidx.compose.ui.graphics.Color.White,
                    background = androidx.compose.ui.graphics.Color.White,
                    onBackground = androidx.compose.ui.graphics.Color.Black,
                    surface = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.Black
                )
            }
        }
        AppTheme.Forest -> {
            if (darkTheme) {
                darkColorScheme(primary = Color(0xFF81C784), secondary = Color(0xFFA5D6A7))
            } else {
                lightColorScheme(primary = Color(0xFF2E7D32), secondary = Color(0xFF4CAF50))
            }
        }
        AppTheme.Midnight -> {
            if (darkTheme) {
                darkColorScheme(primary = Color(0xFF90CAF9), secondary = Color(0xFF64B5F6))
            } else {
                lightColorScheme(primary = Color(0xFF1565C0), secondary = Color(0xFF1E88E5))
            }
        }
        AppTheme.Rose -> {
            if (darkTheme) {
                darkColorScheme(primary = Color(0xFFF48FB1), secondary = Color(0xFFF06292))
            } else {
                lightColorScheme(primary = Color(0xFFC2185B), secondary = Color(0xFFE91E63))
            }
        }
        else -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = M3ExpressiveShapes,
        content = content
    )
}
