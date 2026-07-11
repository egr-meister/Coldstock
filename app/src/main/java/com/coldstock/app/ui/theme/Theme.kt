package com.coldstock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DeepFreezerBlue,
    onPrimary = FrozenWhite,
    primaryContainer = IceMist,
    onPrimaryContainer = DeepText,
    secondary = FrostBlue,
    onSecondary = FrozenWhite,
    secondaryContainer = DrawerGlass,
    onSecondaryContainer = DeepText,
    tertiary = UseFirstViolet,
    onTertiary = FrozenWhite,
    background = AppBackground,
    onBackground = DeepText,
    surface = SurfaceWhite,
    onSurface = DeepText,
    surfaceVariant = IceMist,
    onSurfaceVariant = SecondaryText,
    outline = DrawerEdge,
    outlineVariant = DividerColor,
    error = ReviewPassedRed,
    onError = FrozenWhite,
)

private val DarkColors = darkColorScheme(
    primary = FrostBlue,
    onPrimary = DeepText,
    primaryContainer = DeepFreezerBlue,
    onPrimaryContainer = FrozenWhite,
    secondary = DrawerEdge,
    onSecondary = DeepText,
    tertiary = UseFirstViolet,
    onTertiary = FrozenWhite,
    background = Color0F1C22,
    onBackground = IceMist,
    surface = Color15252C,
    onSurface = IceMist,
    surfaceVariant = Color1E323A,
    onSurfaceVariant = DrawerGlass,
    outline = DrawerShadow,
    outlineVariant = DrawerShadow,
    error = ReviewPassedRed,
    onError = FrozenWhite,
)

@Composable
fun ColdstockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = ColdstockTypography,
        content = content
    )
}
