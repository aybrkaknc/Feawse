package com.example.feawse.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FEAWSEColorScheme = darkColorScheme(
    primary = AwakeningRoyalBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = AwakeningRoyalBlueContainer,
    onPrimaryContainer = AwakeningRoyalBlueBright,
    secondary = AwakeningGold,
    onSecondary = AwakeningDarkBg,
    secondaryContainer = AwakeningGoldContainer,
    onSecondaryContainer = AwakeningGoldBright,
    tertiary = AwakeningFalchionCyan,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = AwakeningFalchionBlueContainer,
    onTertiaryContainer = AwakeningTextPrimary,
    background = AwakeningDarkBg,
    onBackground = AwakeningTextPrimary,
    surface = AwakeningNavySurface,
    onSurface = AwakeningTextPrimary,
    surfaceVariant = AwakeningCardSurface,
    onSurfaceVariant = AwakeningTextSecondary,
    outline = AwakeningCardBorder,
    outlineVariant = AwakeningBorderSubtle
)

@Composable
fun FEAWSETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FEAWSEColorScheme,
        typography = Typography,
        content = content
    )
}
