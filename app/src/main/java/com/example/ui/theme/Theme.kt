package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FireStockDarkColorScheme = darkColorScheme(
    primary = FireRedBright,
    onPrimary = Color.White,
    primaryContainer = FireRedContainer,
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = AmberCautionBright,
    onSecondary = Color(0xFF261800),
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFFFFECB3),
    tertiary = WaterBlueBright,
    onTertiary = Color(0xFF002238),
    tertiaryContainer = WaterBlueContainer,
    onTertiaryContainer = Color(0xFFB3E5FC),
    background = TacticalDark,
    onBackground = TextPrimary,
    surface = TacticalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TacticalOutline,
    error = StockRed,
    onError = Color.White,
    errorContainer = StockRedBg,
    onErrorContainer = Color(0xFFFFB4AB)
)

private val FireStockLightColorScheme = lightColorScheme(
    primary = FireRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = AmberCaution,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = WaterBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF002008),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFFCBD2DC),
    error = FireRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to tactical dark / emergency high contrast mode for firefighting
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FireStockDarkColorScheme else FireStockLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
