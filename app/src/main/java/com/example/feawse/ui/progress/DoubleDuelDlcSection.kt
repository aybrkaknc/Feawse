package com.example.feawse.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.data.MiscDb
import com.example.feawse.savefile.Chapter13
import com.example.feawse.theme.*
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@Composable
fun DoubleDuelDlcSection(
    chapter: Chapter13,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val du = chapter.blockDu26 ?: return
    val modCount = uiState.modificationCount

    var selectedTab by remember { mutableIntStateOf(0) } // 0: İkili Düello, 1: DLC Rekorları
    var confirmDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    val duelNames = remember { MiscDb.doubleDuelNames }
    val dlcNames = remember { MiscDb.chapterDlcNames }

    CollapsibleCard(
        title = "İkili Düello & DLC Rekorları",
        subtitle = "22 Düello Skoru • 25 DLC Tur Rekoru",
        icon = Icons.Default.EmojiEvents,
        initialExpanded = false,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Tab Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 0) AwakeningGoldContainer else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "İkili Düello (22)",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) AwakeningGoldBright else AwakeningTextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == 1) AwakeningGoldContainer else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DLC Tur Rekorları (25)",
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) AwakeningGoldBright else AwakeningTextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // TAB 0: Double Duel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            confirmDialog = "Tüm 22 ikili düello kazanıldı olarak işaretlensin mi?" to {
                                viewModel.setAllDuelsBeaten(true)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Tümünü Kazan", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = {
                            confirmDialog = "Tüm düello skorları maksimum (255) yapılsın mı?" to {
                                viewModel.setAllDuelScores(255)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningCardSurface, contentColor = AwakeningTextPrimary),
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Maks Skor (255)", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = "Tüm düello skorları ve kazanma durumları sıfırlansın mı?" to {
                                viewModel.setAllDuelsBeaten(false)
                                viewModel.setAllDuelScores(0)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Sıfırla", fontSize = 10.5.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                    }
                }

                HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                // List of 22 duels
                duelNames.forEachIndexed { index, name ->
                    val isBeaten = remember(du, index, modCount) { du.isDuelBeaten(index) }
                    val score = remember(du, index, modCount) { du.getDuelScore(index) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .clickable { viewModel.setDuelBeaten(index, !isBeaten) }
                        ) {
                            Icon(
                                imageVector = if (isBeaten) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isBeaten) AwakeningGold else AwakeningTextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isBeaten) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isBeaten) AwakeningTextPrimary else AwakeningTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isBeaten) "Kazanıldı • Skor: $score" else "Kazanılmadı • Skor: $score",
                                    fontSize = 10.sp,
                                    color = if (isBeaten) AwakeningGoldBright else AwakeningTextTertiary
                                )
                            }
                        }

                        NumberStepper(
                            value = score,
                            min = 0,
                            max = 255,
                            onValueChange = { viewModel.setDuelScore(index, it) }
                        )
                    }

                    if (index < duelNames.lastIndex) {
                        HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            } else {
                // TAB 1: DLC Turn Records
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            confirmDialog = "Tüm 25 DLC bölümü için tur rekoru 1 yapılsın mı?" to {
                                viewModel.setAllDlcTurns(1)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("1 Tur (Speedrun)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = {
                            confirmDialog = "Tüm DLC bölüm tur sayıları maksimum (255) yapılsın mı?" to {
                                viewModel.setAllDlcTurns(255)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningCardSurface, contentColor = AwakeningTextPrimary),
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Maks (255 Tur)", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = {
                            confirmDialog = "Tüm DLC tur rekorları sıfırlansın mı?" to {
                                viewModel.setAllDlcTurns(0)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Sıfırla", fontSize = 10.5.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                    }
                }

                HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                // List of 25 DLC maps
                dlcNames.forEachIndexed { index, name ->
                    val turns = remember(du, index, modCount) { du.dlcTurn(index) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AwakeningTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (turns > 0) "Rekor: $turns Tur" else "Oynanmadı / 0 Tur",
                                fontSize = 10.sp,
                                color = if (turns > 0) AwakeningGold else AwakeningTextTertiary
                            )
                        }

                        NumberStepper(
                            value = turns,
                            min = 0,
                            max = 255,
                            onValueChange = { viewModel.setDlcTurn(index, it) }
                        )
                    }

                    if (index < dlcNames.lastIndex) {
                        HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (confirmDialog != null) {
        val (message, action) = confirmDialog!!
        AlertDialog(
            onDismissRequest = { confirmDialog = null },
            title = { Text("İşlemi Onaylayın", color = AwakeningGoldBright, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
            text = { Text(message, color = AwakeningTextPrimary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        action()
                        confirmDialog = null
                    },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Evet, Uygula", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDialog = null },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text("İptal", color = AwakeningTextSecondary, fontSize = 11.5.sp, maxLines = 1, textAlign = TextAlign.Center)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
