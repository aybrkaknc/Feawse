package com.example.feawse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.theme.*
import com.example.feawse.ui.util.WindowWidthSize
import com.example.feawse.ui.util.rememberWindowWidthSize

data class GlowNavItem(
    val tabId: Int,
    val title: String,
    val icon: ImageVector,
    val isProminentCenter: Boolean = false
)

/**
 * Modern Floating Morphing Bubble Navigation Bar.
 * Inspired by top mobile design patterns (BubbleNavigationBar, SmoothBottomBar).
 * Features persistent tab buttons with direction-aware horizontal slide and fade transitions
 * for titles, spring-bounce icon scale, and glowing golden pill expansion/contraction.
 */
@Composable
fun AwakeningGlowNavigationBar(
    items: List<GlowNavItem>,
    activeTabId: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSize = rememberWindowWidthSize()
    val isCompact = windowSize == WindowWidthSize.Compact
    val labelSize = if (isCompact) 11.sp else 12.sp

    // Track previous tab to provide direction-aware slide animations between buttons
    var previousTabId by remember { mutableIntStateOf(activeTabId) }
    val isMovingRight = activeTabId >= previousTabId

    LaunchedEffect(activeTabId) {
        previousTabId = activeTabId
    }

    // Outer wrapper respecting system navigation bars
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Island Dock
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = AwakeningNavySurface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, AwakeningBorderSubtle),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.tabId == activeTabId
                    val interactionSource = remember { MutableInteractionSource() }

                    val animDuration = 130

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                        label = "tabScale_${item.tabId}"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) AwakeningGoldBright else AwakeningTextSecondary,
                        animationSpec = tween(durationMillis = animDuration),
                        label = "iconTint_${item.tabId}"
                    )

                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                        label = "pillAlpha_${item.tabId}"
                    )

                    val pillScale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.92f,
                        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                        label = "pillScale_${item.tabId}"
                    )

                    // Single persistent button container that animates its content & size smoothly
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onTabSelected(item.tabId)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Morphing Glowing Background Pill for Active Tab (always in composition, smooth alpha & scale)
                        if (pillAlpha > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer {
                                        alpha = pillAlpha
                                        scaleX = pillScale
                                        scaleY = pillScale
                                    }
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                AwakeningGold.copy(alpha = 0.28f),
                                                AwakeningGold.copy(alpha = 0.06f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.55f * pillAlpha)),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .background(
                                        color = AwakeningGold.copy(alpha = 0.16f * pillAlpha),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            )
                        }

                        // Tab Button Content (Icon + Directional Slide Animated Title)
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .animateContentSize(
                                    animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing)
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(19.dp)
                                    .scale(iconScale)
                            )

                            // Title with Direction-aware Slide + Fade + Expand (Faster, smoother, no lateral jump)
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(animationSpec = tween(durationMillis = 110)) +
                                        slideInHorizontally(
                                            animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                                        ) { width -> width / 4 } +
                                        expandHorizontally(
                                            expandFrom = Alignment.Start,
                                            animationSpec = tween(animDuration, easing = FastOutSlowInEasing)
                                        ),
                                exit = fadeOut(animationSpec = tween(durationMillis = 80)) +
                                        shrinkHorizontally(
                                            shrinkTowards = Alignment.Start,
                                            animationSpec = tween(100, easing = FastOutSlowInEasing)
                                        )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        color = AwakeningGoldBright,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = labelSize,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
