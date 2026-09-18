package com.example.feawse.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.feawse.data.ChapterDb
import com.example.feawse.savefile.Chapter13
import com.example.feawse.savefile.map.RawMap
import com.example.feawse.theme.*
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.util.isCompact
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@Composable
fun OverworldEncountersSection(
    chapter: Chapter13,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val gmap = chapter.blockGmap ?: return
    val modCount = uiState.modificationCount
    val compact = isCompact()

    val maps = remember(gmap, modCount) { gmap.maps ?: emptyList() }
    val overWorldNames = remember { ChapterDb.getOverWorldNames() }

    var confirmDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var showOnlyUnlocked by remember { mutableStateOf(true) }

    val displayedMaps = remember(maps, showOnlyUnlocked, modCount) {
        maps.mapIndexed { idx, map ->
            val name = if (idx < overWorldNames.size) overWorldNames[idx] else "Bölüm #${idx + 1}"
            Triple(idx, map, name)
        }.filter { (!showOnlyUnlocked || it.second.lockState() != 0) }
    }

    CollapsibleCard(
        title = "Harita Karşılaşmaları (Overworld Encounters)",
        subtitle = "Risen Zombileri, Tüccar Anna & Karşılaşma Düzeni",
        icon = Icons.Default.Explore,
        initialExpanded = false,
        modifier = modifier
    ) {
        // Fast Action Buttons Row (Clear, Risen, Anna, Randomize)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Her Yere Risen
                Button(
                    onClick = {
                        confirmDialog = "Tüm açık bölümlere Risen zombi karşılaşmaları yerleştirilecek. Onaylıyor musunuz?" to {
                            viewModel.randomizeEncounters(1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A1818),
                        contentColor = Color(0xFFFF9E9E)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF8B2525)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "💀 Her Yere Risen",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                // Her Yere Anna
                Button(
                    onClick = {
                        confirmDialog = "Tüm açık bölümlere Gezgin Tüccar Anna yerleştirilecek. Onaylıyor musunuz?" to {
                            viewModel.randomizeEncounters(2)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF133626),
                        contentColor = Color(0xFF6EE7B7)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF1D5E3F)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "🛍️ Tüccar Anna",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Rastgele Doldur
                OutlinedButton(
                    onClick = {
                        confirmDialog = "Tüm açık bölümlere rastgele Risen veya Tüccar Anna dağıtılacak. Onaylıyor musunuz?" to {
                            viewModel.randomizeEncounters(3)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    border = BorderStroke(1.dp, AwakeningGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "🎲 Rastgele Doldur",
                        fontSize = 11.sp,
                        color = AwakeningGoldBright,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                // Tümünü Temizle
                OutlinedButton(
                    onClick = {
                        confirmDialog = "Tüm harita karşılaşmaları temizlenerek boşaltılacak. Onaylıyor musunuz?" to {
                            viewModel.randomizeEncounters(0)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Tümünü Temizle",
                        fontSize = 11.sp,
                        color = AwakeningTextSecondary,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Toggle: Show only unlocked vs all maps
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Bölüm Karşılaşma Yuvaları (${displayedMaps.size} Harita):",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = AwakeningTextSecondary
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AwakeningCardSurface)
                    .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (showOnlyUnlocked) AwakeningGoldContainer else Color.Transparent)
                        .clickable { showOnlyUnlocked = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Açık Olanlar",
                        fontSize = 10.sp,
                        fontWeight = if (showOnlyUnlocked) FontWeight.Bold else FontWeight.Normal,
                        color = if (showOnlyUnlocked) AwakeningGoldBright else AwakeningTextTertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!showOnlyUnlocked) AwakeningGoldContainer else Color.Transparent)
                        .clickable { showOnlyUnlocked = false }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Tüm Bölümler",
                        fontSize = 10.sp,
                        fontWeight = if (!showOnlyUnlocked) FontWeight.Bold else FontWeight.Normal,
                        color = if (!showOnlyUnlocked) AwakeningGoldBright else AwakeningTextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(6.dp))

        // Per-Map List
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            displayedMaps.forEach { (mapIdx, map, mapName) ->
                EncounterMapRow(
                    mapIndex = mapIdx,
                    map = map,
                    mapName = mapName,
                    onSetEncounter = { slot, newType ->
                        viewModel.setMapEncounter(mapIdx, slot, newType)
                    }
                )
            }
        }
    }

    // Confirmation Dialog
    if (confirmDialog != null) {
        val (message, action) = confirmDialog!!
        AlertDialog(
            onDismissRequest = { confirmDialog = null },
            title = { Text("İşlem Onayı", color = AwakeningGoldBright, fontWeight = FontWeight.Bold) },
            text = { Text(message, color = AwakeningTextPrimary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        action()
                        confirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Evet, Uygula", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialog = null }) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun EncounterMapRow(
    mapIndex: Int,
    map: RawMap,
    mapName: String,
    onSetEncounter: (slot: Int, type: Int) -> Unit
) {
    val slot0Type = when {
        map.isRisen(0) -> 1
        map.isMerchant(0) -> 2
        map.isWireless(0) -> 3
        else -> 0
    }

    val slot1Type = when {
        map.isRisen(1) -> 1
        map.isMerchant(1) -> 2
        map.isWireless(1) -> 3
        else -> 0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Map Name and Lock status
            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                Text(
                    text = mapName,
                    color = AwakeningTextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (map.lockState()) {
                        0 -> "Kilitli (Locked)"
                        1 -> "Tamamlandı (Beaten)"
                        else -> "Açık (Active)"
                    },
                    fontSize = 9.5.sp,
                    color = when (map.lockState()) {
                        0 -> Color(0xFFFF6B6B)
                        1 -> Color(0xFF4ADE80)
                        else -> AwakeningGoldBright
                    }
                )
            }

            // Encounter Slots 1 & 2
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                EncounterSlotPill(slotLabel = "Y1", encounterType = slot0Type) { nextType ->
                    onSetEncounter(0, nextType)
                }

                EncounterSlotPill(slotLabel = "Y2", encounterType = slot1Type) { nextType ->
                    onSetEncounter(1, nextType)
                }
            }
        }
    }
}

@Composable
private fun EncounterSlotPill(
    slotLabel: String,
    encounterType: Int,
    onSelectNext: (Int) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val (label, bgCol, textCol) = when (encounterType) {
        1 -> Triple("💀 Risen", Color(0xFF4A1818), Color(0xFFFF8E8E))
        2 -> Triple("🛍️ Anna", Color(0xFF133626), Color(0xFF6EE7B7))
        3 -> Triple("📶 Ekip", Color(0xFF192A4A), Color(0xFF93C5FD))
        else -> Triple("Boş", AwakeningNavySurface, AwakeningTextTertiary)
    }

    Box {
        Surface(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { expandedMenu = true },
            color = bgCol,
            border = BorderStroke(1.dp, if (encounterType > 0) textCol.copy(alpha = 0.5f) else AwakeningBorderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (encounterType > 0) FontWeight.Bold else FontWeight.Normal,
                    color = textCol
                )
            }
        }

        DropdownMenu(
            expanded = expandedMenu,
            onDismissRequest = { expandedMenu = false },
            modifier = Modifier.background(AwakeningNavySurface)
        ) {
            DropdownMenuItem(
                text = { Text("Boş / Yok (Empty)", fontSize = 11.5.sp, color = AwakeningTextSecondary) },
                onClick = {
                    onSelectNext(0)
                    expandedMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("💀 Risen (Zombi Ordusu)", fontSize = 11.5.sp, color = Color(0xFFFF8E8E), fontWeight = FontWeight.Bold) },
                onClick = {
                    onSelectNext(1)
                    expandedMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("🛍️ Gezgin Tüccar Anna", fontSize = 11.5.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold) },
                onClick = {
                    onSelectNext(2)
                    expandedMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("📶 Wireless / Sokak Ekibi", fontSize = 11.5.sp, color = Color(0xFF93C5FD)) },
                onClick = {
                    onSelectNext(3)
                    expandedMenu = false
                }
            )
        }
    }
}
