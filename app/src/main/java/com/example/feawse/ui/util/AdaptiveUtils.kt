package com.example.feawse.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WindowWidthSize {
    Compact, // < 360dp (Düşük çözünürlüklü veya çok yüksek DPI ölçekli küçük ekranlar)
    Medium,  // 360dp .. 599dp (Standart telefon ekranları)
    Expanded // >= 600dp (Tabletler, geniş katlanabilirler veya yatay mod)
}

@Composable
fun rememberWindowWidthSize(): WindowWidthSize {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp
    return when {
        width < 360 -> WindowWidthSize.Compact
        width < 600 -> WindowWidthSize.Medium
        else -> WindowWidthSize.Expanded
    }
}

@Composable
fun isCompact(): Boolean = rememberWindowWidthSize() == WindowWidthSize.Compact

@Composable
fun isExpanded(): Boolean = rememberWindowWidthSize() == WindowWidthSize.Expanded

@Composable
fun adaptivePadding(): Dp {
    return when (rememberWindowWidthSize()) {
        WindowWidthSize.Compact -> 10.dp
        WindowWidthSize.Medium -> 16.dp
        WindowWidthSize.Expanded -> 24.dp
    }
}

@Composable
fun adaptiveSpacing(): Dp {
    return when (rememberWindowWidthSize()) {
        WindowWidthSize.Compact -> 8.dp
        WindowWidthSize.Medium -> 12.dp
        WindowWidthSize.Expanded -> 16.dp
    }
}

@Composable
fun adaptiveTitleSize(): TextUnit {
    return when (rememberWindowWidthSize()) {
        WindowWidthSize.Compact -> 16.sp
        WindowWidthSize.Medium -> 18.sp
        WindowWidthSize.Expanded -> 22.sp
    }
}
