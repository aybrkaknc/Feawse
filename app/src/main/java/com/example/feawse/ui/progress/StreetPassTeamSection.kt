package com.example.feawse.ui.progress

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feawse.data.ClassDb
import com.example.feawse.data.ItemDb
import com.example.feawse.data.SkillDb
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.Chapter13
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.savefile.wireless.DuTeam
import com.example.feawse.savefile.wireless.UnitDu
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@Composable
fun StreetPassTeamSection(
    chapter: Chapter13,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val du = chapter.blockDu26 ?: return
    val team = du.playerTeam ?: return
    val modCount = uiState.modificationCount
    val isWest = chapter.isWest

    val units = remember(team, modCount) { team.unitList ?: emptyList() }
    val foreignTeams = remember(du, modCount) { du.teamList ?: emptyList() }

    val maxNameChars = remember(isWest) { if (isWest) 21 else 11 }
    var teamNameInput by remember(team, modCount) { mutableStateOf(team.getTeamName()) }
    var showUnitPickerSlot by remember { mutableStateOf<Int?>(null) }
    var confirmDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    val mainRoster = remember(chapter.blockUnit, modCount) {
        chapter.blockUnit?.unitList?.getOrNull(3) ?: emptyList()
    }

    CollapsibleCard(
        title = "Sokak Geçişi Ekibi (StreetPass Team)",
        subtitle = "${units.size}/10 Üye • ${team.getTeamName().ifBlank { "Takım Adı Yok" }} • ${team.getRenown()} Şöhret",
        icon = Icons.Default.Wifi,
        initialExpanded = false,
        modifier = modifier
    ) {
        // Team Settings Card (Name & Renown)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningCardSurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Takım Bilgileri & Şöhret",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AwakeningTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Team Name Input
                OutlinedTextField(
                    value = teamNameInput,
                    onValueChange = {
                        val trimmed = it.take(maxNameChars)
                        teamNameInput = trimmed
                        viewModel.setPlayerTeamName(trimmed)
                    },
                    label = { Text("StreetPass Takım Adı (Maks $maxNameChars Karakter)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, color = AwakeningTextPrimary),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AwakeningGold,
                        unfocusedBorderColor = AwakeningBorderSubtle,
                        focusedContainerColor = AwakeningNavySurface,
                        unfocusedContainerColor = AwakeningNavySurface
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(16.dp))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Team Renown Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Takım Şöhreti (Renown):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        Text(
                            text = "${team.getRenown()} Puan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwakeningGoldBright
                        )
                    }

                    NumberStepper(
                        value = team.getRenown(),
                        onValueChange = { viewModel.setPlayerTeamRenown(it) },
                        min = 0,
                        max = 999999,
                        step = 100
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fast Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    confirmDialog = "StreetPass takımı ordunuzdaki en güçlü ilk 10 birimle otomatik doldurulacak. Onaylıyor musunuz?" to {
                        viewModel.autoFillPlayerTeamFromArmy()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ordudan Doldur (10)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }

            OutlinedButton(
                onClick = {
                    confirmDialog = "Haritada bulunan ${foreignTeams.size} adet yabancı kablosuz ekip temizlenecek. Onaylıyor musunuz?" to {
                        viewModel.clearForeignWirelessTeams()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AwakeningTextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Yabancıları Temizle (${foreignTeams.size})",
                    fontSize = 10.5.sp,
                    color = AwakeningTextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // 10 Unit Slots List
        Text(
            text = "Takım Kadrosu (10 Yuva):",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = AwakeningTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (slot in 0 until 10) {
                val unitDu = units.getOrNull(slot)
                if (unitDu != null) {
                    StreetPassUnitCard(
                        slot = slot,
                        unit = unitDu,
                        onReassign = { showUnitPickerSlot = slot },
                        onRemove = { viewModel.removePlayerTeamUnit(slot) }
                    )
                } else {
                    EmptyStreetPassSlotCard(
                        slot = slot,
                        onClick = { showUnitPickerSlot = slot }
                    )
                }
            }
        }
    }

    // Army Unit Picker Dialog
    if (showUnitPickerSlot != null) {
        val targetSlot = showUnitPickerSlot!!
        ArmyUnitPickerDialog(
            title = if (targetSlot == 0) "Takım Lideri Seç (Yuva #1)" else "Yuva #${targetSlot + 1} için Birim Seç",
            units = mainRoster,
            onDismiss = { showUnitPickerSlot = null },
            onSelectUnit = { selectedUnit ->
                viewModel.setPlayerTeamUnit(targetSlot, selectedUnit)
                showUnitPickerSlot = null
            }
        )
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
private fun StreetPassUnitCard(
    slot: Int,
    unit: UnitDu,
    onReassign: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val unitId = unit.getUnitId()
    val name = unit.getName()
    val className = ClassDb.getClassName(unit.getUnitClass())
    val level = unit.getLevel()
    val portrait = remember(unitId) { PortraitManager.getPortraitByUnitId(context, unitId) }

    val activeSkills = remember(unit) {
        unit.getActiveSkills().filter { it > 0 }
    }

    val equippedItems = remember(unit) {
        unit.itemList?.filter { it.itemId > 0 } ?: emptyList()
    }

    val isLeader = slot == 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isLeader) AwakeningGold else AwakeningBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unit Portrait
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AwakeningNavySurface)
                    .border(1.dp, if (isLeader) AwakeningGold else AwakeningBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (portrait != null) {
                    Image(
                        bitmap = portrait.asImageBitmap(),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLeader) {
                        Surface(
                            color = AwakeningGold,
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "LİDER",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwakeningDarkBg,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "#${slot + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AwakeningTextTertiary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = name,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLeader) AwakeningGoldBright else AwakeningTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "$className • Lv: $level",
                    fontSize = 10.sp,
                    color = AwakeningTextSecondary
                )

                // Equipped Items summary
                if (equippedItems.isNotEmpty()) {
                    val itemsText = equippedItems.take(3).joinToString(", ") { ItemDb.getItemName(it.itemId) }
                    Text(
                        text = "⚔️ $itemsText",
                        fontSize = 9.sp,
                        color = AwakeningFalchionBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons (Change & Delete)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onReassign),
                    color = AwakeningNavySurface,
                    border = BorderStroke(1.dp, AwakeningBorderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Değiştir", tint = AwakeningGold, modifier = Modifier.size(13.dp))
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onRemove),
                    color = AwakeningNavySurface,
                    border = BorderStroke(1.dp, AwakeningBorderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = AwakeningTextTertiary, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStreetPassSlotCard(
    slot: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = AwakeningCardSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = AwakeningTextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yuva #${slot + 1}: Boş (Birim Ata)",
                    fontSize = 11.sp,
                    color = AwakeningTextTertiary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AwakeningTextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ArmyUnitPickerDialog(
    title: String,
    units: List<SaveUnit>,
    onDismiss: () -> Unit,
    onSelectUnit: (SaveUnit) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(units, searchQuery) {
        if (searchQuery.isBlank()) units
        else units.filter { it.unitName().contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = AwakeningNavySurface,
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AwakeningGoldBright
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AwakeningSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Ordu birimi ara...",
                    containerColor = AwakeningCardSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList) { armyUnit ->
                        val uId = armyUnit.unitId
                        val uName = armyUnit.unitName()
                        val uClass = armyUnit.rawBlock1?.unitClass() ?: 0
                        val uClassName = ClassDb.getClassName(uClass)
                        val uLevel = armyUnit.rawBlock1?.level() ?: 1
                        val portrait = remember(uId) { PortraitManager.getPortrait(context, armyUnit) }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectUnit(armyUnit) },
                            color = AwakeningCardSurface,
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AwakeningNavySurface)
                                        .border(1.dp, AwakeningGold.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (portrait != null) {
                                        Image(
                                            bitmap = portrait.asImageBitmap(),
                                            contentDescription = uName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uName,
                                        color = AwakeningTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$uClassName • Lv: $uLevel",
                                        color = AwakeningTextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
