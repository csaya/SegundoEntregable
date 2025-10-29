package com.example.segundoentregable.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    error = md_theme_light_error,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun SegundoEntregableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
