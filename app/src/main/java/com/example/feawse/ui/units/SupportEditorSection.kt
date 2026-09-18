package com.example.feawse.ui.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportEditorSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val modCount = uiState.modificationCount

    val unitId = remember(unit) { unit.rawBlock1.unitId() }
    val validUnitIds = remember(unitId) { UnitDb.getUnitSupportUnits(unitId) }
    val supportTypes = remember(unitId) { UnitDb.getUnitSupportTypes(unitId) }
    val hasSupports = validUnitIds.isNotEmpty()

    // Ensure support block is expanded for safe indexing
    LaunchedEffect(unit, modCount) {
        unit.rawSupport?.expandBlock()
    }

    var syncEnabled by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var batchLevelSelect by remember { mutableStateOf(8) } // Default S-Rank (8)
    var showConfirmBatchDialog by remember { mutableStateOf(false) }
    var expandedDetailSlot by remember { mutableStateOf<Int?>(null) }

    // Count active supports (supportValue > 0)
    val activeCount = remember(unit, modCount) {
        if (!hasSupports || unit.rawSupport == null) 0
        else {
            (0 until validUnitIds.size).count { slot ->
                unit.rawSupport.supportValue(slot) > 0
            }
        }
    }

    CollapsibleCard(
        title = "Destek İlişkileri (Supports)",
        subtitle = if (hasSupports) "$activeCount aktif bağ • ${validUnitIds.size} destek adayı" else "Bu birim için destek bulunmuyor",
        icon = Icons.Default.Diversity3,
        initialExpanded = false,
        modifier = modifier
    ) {
        if (!hasSupports) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Text(
                    text = "Bu birim (Jenerik / Einherjar / Canavar) destek konuşmalarına veya ilişkilerine sahip değildir.",
                    color = AwakeningTextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
            return@CollapsibleCard
        }

        // 1. Quick Batch & Global Sync Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningCardSurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Karşılıklı Eşitle (Sync)",
                            color = AwakeningTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Partner birimin desteği de aynı seviyeye getirilir",
                            color = AwakeningTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = { syncEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AwakeningDarkBg,
                            checkedTrackColor = AwakeningGold,
                            uncheckedThumbColor = AwakeningTextTertiary,
                            uncheckedTrackColor = AwakeningDarkBg
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                // Chrom-specific Village Maiden flag
                if (unitId == 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val isMaiden = remember(unit, modCount) { unit.rawFlags.hasBattleFlag(22) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Köy Kızı Evliliği (Village Maiden)",
                                color = AwakeningTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Chrom hiçbir adayla evlenmezse köy kızıyla evlenir",
                                color = AwakeningTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isMaiden,
                            onCheckedChange = { viewModel.setChromVillageMaiden(unit, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningCrimson,
                                uncheckedThumbColor = AwakeningTextTertiary,
                                uncheckedTrackColor = AwakeningDarkBg
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Batch set all supports for this unit
                Text(
                    text = "Bu Birimin Tüm Desteklerini Ayarla:",
                    color = AwakeningTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val batchRanks = listOf(
                        "Yok" to 0,
                        "C" to 2,
                        "B" to 4,
                        "A" to 6,
                        "S" to 8
                    )
                    batchRanks.forEach { (label, levelVal) ->
                        val isSelected = batchLevelSelect == levelVal
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { batchLevelSelect = levelVal },
                            color = if (isSelected) AwakeningGold else AwakeningNavySurface,
                            border = BorderStroke(1.dp, if (isSelected) AwakeningGoldBright else AwakeningBorderSubtle)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (isSelected) AwakeningDarkBg else AwakeningTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = { showConfirmBatchDialog = true },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningFalchionBlue,
                            contentColor = AwakeningTextPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Uygula",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Child unit parent / sibling supports
        if (unit.rawChild != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ChildSupportsCard(
                unit = unit,
                viewModel = viewModel,
                modCount = modCount
            )
        }

        // 2. Search Box if > 6 partners
        if (validUnitIds.size > 6) {
            Spacer(modifier = Modifier.height(8.dp))
            AwakeningSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Karakter ara...",
                containerColor = AwakeningCardSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Partner List
        val filteredPartners = remember(validUnitIds, searchQuery) {
            validUnitIds.indices.mapNotNull { slot ->
                val pId = validUnitIds[slot]
                val name = UnitDb.getUnitName(pId)
                if (searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true)) {
                    Triple(slot, pId, name)
                } else null
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredPartners.forEach { (slot, partnerId, partnerName) ->
                val supportType = if (slot < supportTypes.size) supportTypes[slot] else 0
                val supportsS = supportType != 0 // Type 0 is Non-Romantic, max is A-Rank
                val rawVal = remember(unit, slot, modCount) { unit.rawSupport.supportValue(slot) }
                val levelName = remember(unitId, rawVal, slot) {
                    UnitDb.getUnitSupportLevelName(unitId, rawVal, slot)
                }
                val partnerBmp = remember(partnerId) {
                    PortraitManager.getPortraitByUnitId(context, partnerId)
                }
                val isExpanded = expandedDetailSlot == slot

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (rawVal > 0) AwakeningGold.copy(alpha = 0.4f) else AwakeningBorderSubtle
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            if (partnerBmp != null) {
                                Image(
                                    bitmap = partnerBmp.asImageBitmap(),
                                    contentDescription = partnerName,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AwakeningNavySurface)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AwakeningNavySurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AwakeningTextTertiary)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & Rank Badge
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = partnerName,
                                    color = AwakeningTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SupportRankBadge(levelName = levelName)
                                    if (!supportsS) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Platonik (Maks A)",
                                            color = AwakeningTextTertiary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // Quick Toggle Stepper button
                            IconButton(
                                onClick = {
                                    expandedDetailSlot = if (isExpanded) null else slot
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.Tune else Icons.Default.Tune,
                                    contentDescription = "Detay",
                                    tint = if (isExpanded) AwakeningGold else AwakeningTextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rank Selector Pills [Yok] [C] [B] [A] [S]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val ranks = listOf(
                                "Yok" to 0,
                                "C" to 2,
                                "B" to 4,
                                "A" to 6
                            ) + if (supportsS) listOf("S" to 8) else emptyList()

                            ranks.forEach { (rLabel, rLevel) ->
                                val isActive = when (rLevel) {
                                    0 -> levelName.contains("D-Rank", ignoreCase = true) || rawVal == 0
                                    2 -> levelName.contains("C", ignoreCase = true)
                                    4 -> levelName.contains("B", ignoreCase = true)
                                    6 -> levelName.contains("A", ignoreCase = true)
                                    8 -> levelName.contains("S", ignoreCase = true)
                                    else -> false
                                }

                                val pillColor = when {
                                    !isActive -> AwakeningNavySurface
                                    rLevel == 8 -> AwakeningCrimson
                                    rLevel == 6 -> AwakeningFalchionBlue
                                    rLevel == 4 -> AwakeningSuccess
                                    rLevel == 2 -> AwakeningGold
                                    else -> AwakeningBorderSubtle
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.setUnitSupportLevel(unit, slot, rLevel, sync = syncEnabled)
                                        },
                                    color = pillColor,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) pillColor else AwakeningBorderSubtle
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = rLabel,
                                            color = if (isActive) AwakeningTextPrimary else AwakeningTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Fine-tune exact support points stepper (expandable)
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Detaylı Destek Puanı (0 - 22):",
                                    color = AwakeningTextSecondary,
                                    fontSize = 11.5.sp
                                )
                                NumberStepper(
                                    value = rawVal,
                                    min = 0,
                                    max = 22,
                                    onValueChange = { newPoints ->
                                        viewModel.setUnitSupportPoints(unit, slot, newPoints, sync = syncEnabled)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for Batch Setting all supports
    if (showConfirmBatchDialog) {
        val levelLabel = when (batchLevelSelect) {
            0 -> "Yok (Sıfırlama)"
            2 -> "C Seviyesi"
            4 -> "B Seviyesi"
            6 -> "A Seviyesi"
            8 -> "S Seviyesi (Evlilik)"
            else -> "$batchLevelSelect"
        }

        AlertDialog(
            onDismissRequest = { showConfirmBatchDialog = false },
            title = {
                Text(
                    text = "Tüm Destekleri Ayarla",
                    color = AwakeningGoldBright,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "${unit.unitName()} biriminin tüm destek adaylarıyla olan bağı $levelLabel olarak ayarlanacak." +
                            if (syncEnabled) "\n\nKarşılıklı Eşitleme (Sync) aktif: Partner birimlerin de destekleri aynı anda güncellenecek." else "",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAllUnitSupports(unit, batchLevelSelect, sync = syncEnabled)
                        showConfirmBatchDialog = false
                    },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Text("Uygula", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmBatchDialog = false },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("İptal", color = AwakeningTextSecondary, fontSize = 11.5.sp)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun ChildSupportsCard(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long
) {
    val child = unit.rawChild ?: return

    val fatherVal = remember(unit, modCount) { child.supportParentValue(true) }
    val motherVal = remember(unit, modCount) { child.supportParentValue(false) }
    val siblingVal = remember(unit, modCount) { child.supportSiblingValue() }

    val fatherRank = remember(fatherVal) { UnitDb.getChildSupportLevelName(fatherVal) }
    val motherRank = remember(motherVal) { UnitDb.getChildSupportLevelName(motherVal) }
    val siblingRank = remember(siblingVal) { UnitDb.getChildSupportLevelName(siblingVal) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningNavySurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = AwakeningGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aile Bağları (Çocuk Birim)",
                    color = AwakeningGoldBright,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Baba Desteği
            FamilySupportRow(
                label = "Baba Desteği (Father)",
                levelName = fatherRank,
                onLevelSelected = { lvl -> viewModel.setChildSupportParent(unit, true, lvl) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Anne Desteği
            FamilySupportRow(
                label = "Anne Desteği (Mother)",
                levelName = motherRank,
                onLevelSelected = { lvl -> viewModel.setChildSupportParent(unit, false, lvl) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Kardeş Desteği
            FamilySupportRow(
                label = "Kardeş Desteği (Sibling)",
                levelName = siblingRank,
                onLevelSelected = { lvl -> viewModel.setChildSupportSibling(unit, lvl) }
            )
        }
    }
}

@Composable
private fun FamilySupportRow(
    label: String,
    levelName: String,
    onLevelSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = AwakeningTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            SupportRankBadge(levelName = levelName)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            val ranks = listOf("Yok" to 0, "C" to 2, "B" to 4, "A" to 6)
            ranks.forEach { (rLabel, rLevel) ->
                val isActive = when (rLevel) {
                    0 -> levelName.contains("D-Rank", ignoreCase = true)
                    2 -> levelName.contains("C", ignoreCase = true)
                    4 -> levelName.contains("B", ignoreCase = true)
                    6 -> levelName.contains("A", ignoreCase = true)
                    else -> false
                }

                Surface(
                    modifier = Modifier
                        .width(36.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onLevelSelected(rLevel) },
                    color = if (isActive) AwakeningGold else AwakeningCardSurface,
                    border = BorderStroke(1.dp, if (isActive) AwakeningGoldBright else AwakeningBorderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = rLabel,
                            color = if (isActive) AwakeningDarkBg else AwakeningTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportRankBadge(levelName: String) {
    val bgColor: androidx.compose.ui.graphics.Color
    val textColor: androidx.compose.ui.graphics.Color

    when {
        levelName.contains("S-Rank", ignoreCase = true) -> {
            bgColor = AwakeningCrimson
            textColor = AwakeningTextPrimary
        }
        levelName.contains("S-Pending", ignoreCase = true) -> {
            bgColor = AwakeningCrimson.copy(alpha = 0.6f)
            textColor = AwakeningTextPrimary
        }
        levelName.contains("A-Rank", ignoreCase = true) -> {
            bgColor = AwakeningFalchionBlue
            textColor = AwakeningTextPrimary
        }
        levelName.contains("A-Pending", ignoreCase = true) -> {
            bgColor = AwakeningFalchionBlue.copy(alpha = 0.6f)
            textColor = AwakeningTextPrimary
        }
        levelName.contains("B-Rank", ignoreCase = true) -> {
            bgColor = AwakeningSuccess
            textColor = AwakeningTextPrimary
        }
        levelName.contains("B-Pending", ignoreCase = true) -> {
            bgColor = AwakeningSuccess.copy(alpha = 0.6f)
            textColor = AwakeningTextPrimary
        }
        levelName.contains("C-Rank", ignoreCase = true) -> {
            bgColor = AwakeningGold
            textColor = AwakeningDarkBg
        }
        levelName.contains("C-Pending", ignoreCase = true) -> {
            bgColor = AwakeningGold.copy(alpha = 0.6f)
            textColor = AwakeningDarkBg
        }
        else -> {
            bgColor = AwakeningNavySurface
            textColor = AwakeningTextTertiary
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
    ) {
        Text(
            text = levelName,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
