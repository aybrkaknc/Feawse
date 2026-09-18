package com.example.feawse.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.theme.*
import com.example.feawse.ui.util.isCompact
import com.example.feawse.util.EmulatorHelper

@Composable
fun BackupWarningBanner(
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    onOpenGuide: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val compact = isCompact()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AwakeningNavySurface,
        border = BorderStroke(1.dp, AwakeningBorderSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Uyarı",
                        tint = AwakeningWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Yedek Almayı Unutma!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = AwakeningTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Azahar Plus, Lime3DS veya Citra kayıt dosyalarınızı düzenlemeden önce mutlaka orijinal bir kopyasını yedekleyin! Uygulama otomatik .bak yedeği almaktadır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AwakeningTextSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenGuide,
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningGold),
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (compact) "3DS Rehberi" else "Azahar Plus & 3DS Rehberi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollapsibleCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    initialExpanded: Boolean = false,
    headerTrailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(initialExpanded) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AwakeningNavySurface,
        border = BorderStroke(1.dp, AwakeningBorderSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AwakeningGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AwakeningTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = AwakeningTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (headerTrailing != null) {
                        headerTrailing()
                    }
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                            tint = AwakeningTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

/**
 * Standard Awakening Search Bar used across all screens and dialogs
 * for consistent, high-polish UI continuity.
 */
@Composable
fun AwakeningSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    containerColor: Color = AwakeningNavySurface,
    trailingContent: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 12.sp,
                color = AwakeningTextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedBorderColor = AwakeningGold,
            unfocusedBorderColor = AwakeningBorderSubtle,
            focusedTextColor = AwakeningTextPrimary,
            unfocusedTextColor = AwakeningTextPrimary,
            cursorColor = AwakeningGold
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = if (query.isNotEmpty()) AwakeningGoldBright else AwakeningTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Temizle",
                            tint = AwakeningTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                trailingContent?.invoke()
            }
        }
    )
}

@Composable
fun ExpandableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = { Text(placeholder, fontSize = 12.sp, color = AwakeningTextTertiary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = AwakeningTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = onToggleExpand, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningGold, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AwakeningNavySurface,
                unfocusedContainerColor = AwakeningNavySurface,
                focusedBorderColor = AwakeningGold,
                unfocusedBorderColor = AwakeningBorderSubtle,
                focusedTextColor = AwakeningTextPrimary,
                unfocusedTextColor = AwakeningTextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun PortraitAvatar(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    size: Int = 52
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AwakeningCardSurface)
            .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Portre",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = AwakeningTextTertiary,
                modifier = Modifier.size((size / 2).dp)
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color = AwakeningCardSurface,
    textColor: Color = AwakeningGoldBright
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.35f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = 999,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = { if (value - step >= min) onValueChange(value - step) },
                enabled = value > min,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Azalt",
                    tint = if (value > min) AwakeningTextPrimary else AwakeningTextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AwakeningGoldBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 34.dp)
            )
            IconButton(
                onClick = { if (value + step <= max) onValueChange(value + step) },
                enabled = value < max,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Artır",
                    tint = if (value < max) AwakeningTextPrimary else AwakeningTextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun EmulatorGuideDialog(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AwakeningGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emülatör Kayıt Rehberi", color = AwakeningGoldBright, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = EmulatorHelper.EMULATOR_GUIDE_TEXT,
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextPrimary,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anladım", color = AwakeningGold)
            }
        },
        containerColor = AwakeningNavySurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A111C)
@Composable
fun BackupWarningBannerPreview() {
    FEAWSETheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BackupWarningBanner(onOpenGuide = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A111C)
@Composable
fun StatusBadgePreview() {
    FEAWSETheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(text = "USA / EUR", textColor = AwakeningGold)
            StatusBadge(text = "Sıkıştırılmış", textColor = AwakeningFalchionBlue)
        }
    }
}

