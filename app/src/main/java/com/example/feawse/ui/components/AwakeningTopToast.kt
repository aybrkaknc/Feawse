package com.example.feawse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.theme.*

data class ToastData(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AwakeningToast(
    toastData: ToastData?,
    modifier: Modifier = Modifier,
    isBottom: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = toastData != null,
        enter = slideInVertically(
            initialOffsetY = { if (isBottom) it + 40 else -it - 40 },
            animationSpec = tween(durationMillis = 350)
        ) + fadeIn(animationSpec = tween(durationMillis = 250)),
        exit = slideOutVertically(
            targetOffsetY = { if (isBottom) it + 40 else -it - 40 },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (toastData != null) {
            val isError = toastData.isError
            val borderColor = if (isError) AwakeningCrimson else AwakeningGold
            val iconTint = if (isError) AwakeningCrimson else AwakeningGoldBright
            val titleColor = if (isError) AwakeningCrimson else AwakeningGoldBright

            val titleText = when {
                isError -> "Hata Oluştu"
                toastData.message.contains("yüklendi", ignoreCase = true) -> "Kayıt Başarıyla Yüklendi"
                toastData.message.contains("kaydedildi", ignoreCase = true) -> "Kayıt Başarıyla Kaydedildi"
                toastData.message.contains("yedek", ignoreCase = true) -> "Güvenlik Yedeği Bildirimi"
                else -> "Bildirim"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() },
                color = AwakeningNavySurface,
                border = BorderStroke(1.5.dp, borderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left circular icon with thematic container
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = if (isError) AwakeningCrimsonContainer else AwakeningGoldContainer,
                        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isError) {
                                    Icons.Default.Error
                                } else if (toastData.message.contains("yüklendi", ignoreCase = true)) {
                                    Icons.Default.Shield
                                } else {
                                    Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = toastData.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = AwakeningTextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = AwakeningTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AwakeningTopToast(
    toastData: ToastData?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) = AwakeningToast(toastData = toastData, modifier = modifier, isBottom = false, onDismiss = onDismiss)
