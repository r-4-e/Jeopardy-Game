package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCobalt,
    onPrimary = MidnightBackground,
    primaryContainer = DarkCobaltContainer,
    onPrimaryContainer = LightCobaltInk,
    secondary = BrightGold,
    onSecondary = MidnightBackground,
    secondaryContainer = DarkGoldContainer,
    onSecondaryContainer = LightGoldInk,
    tertiary = BrightEmerald,
    onTertiary = MidnightBackground,
    tertiaryContainer = DarkEmeraldContainer,
    onTertiaryContainer = LightEmeraldInk,
    error = BrightCrimson,
    onError = MidnightBackground,
    errorContainer = DarkCrimsonContainer,
    onErrorContainer = LightCrimsonInk,
    background = MidnightBackground,
    onBackground = CrispOffWhite,
    surface = SlateSurface,
    onSurface = CrispOffWhite,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SilverMuted,
    outline = DarkHairline,
    outlineVariant = DarkHairline
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalCobalt,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = DeepCobaltInk,
    secondary = WarmGold,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = DeepGoldInk,
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = DeepEmeraldInk,
    error = CrimsonMissed,
    onError = Color.White,
    errorContainer = CrimsonContainer,
    onErrorContainer = DeepCrimsonInk,
    background = IvoryBackground,
    onBackground = ObsidianInk,
    surface = CrispSurface,
    onSurface = ObsidianInk,
    surfaceVariant = SoftStoneSurfaceVariant,
    onSurfaceVariant = GraphiteMuted,
    outline = HairlineOutline,
    outlineVariant = HairlineOutline
)

private val ClueGridShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ClueGridTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClueGridTypography,
        shapes = ClueGridShapes,
        content = content
    )
}
