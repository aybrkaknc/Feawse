package com.example.feawse.ui.units

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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feawse.data.MiscDb
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState
import androidx.compose.ui.graphics.Color as ComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildEditorSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val modCount = uiState.modificationCount

    val hasChildBlock = unit.rawChild != null
    val unitId = remember(unit) { unit.rawBlock1.unitId() }

    // Sibling detection across army
    val siblingId = remember(unit, modCount, hasChildBlock) {
        if (hasChildBlock) {
            uiState.chapterFile?.blockUnit?.findSibling(unit) ?: -1
        } else -1
    }

    var showConfirmRemoveBlock by remember { mutableStateOf(false) }
    var pickerSlotToOpen by remember { mutableStateOf<Int?>(null) } // 0..5
    var expandedGrandparents by remember { mutableStateOf(false) }

    // Summary subtitle
    val subtitleText = remember(unit, modCount, hasChildBlock, siblingId) {
        if (!hasChildBlock) {
            "Çocuk bloğu yok (Ebeveyn veya saç rengi eklemek için dokunun)"
        } else {
            val fatherId = unit.rawChild.parentId(0)
            val motherId = unit.rawChild.parentId(1)
            val fName = if (fatherId == 65535 || fatherId == -1) "Yok" else UnitDb.getUnitName(fatherId)
            val mName = if (motherId == 65535 || motherId == -1) "Yok" else UnitDb.getUnitName(motherId)
            val sibText = if (siblingId != -1) " • Kardeş: ${UnitDb.getUnitName(siblingId)}" else ""
            "Baba: $fName • Anne: $mName$sibText"
        }
    }

    CollapsibleCard(
        title = "Çocuk & Ebeveyn Yönetimi (Child Block)",
        subtitle = subtitleText,
        icon = Icons.Default.ChildCare,
        initialExpanded = false,
        modifier = modifier
    ) {
        if (!hasChildBlock) {
            // No Child Block State
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = AwakeningGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bu birimde çocuk bloğu (ebeveyn verisi) bulunmuyor.",
                            color = AwakeningTextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "FE Awakening'de çocuk birimler (Lucina, Morgan, Owain vb.) ebeveynlerinden stat bonusları (Asset/Flaw), sınıf yetenekleri ve saç rengi devralır. Bu birime ebeveyn ve aile bağları tanımlamak için çocuk bloğu ekleyebilirsiniz.",
                        color = AwakeningTextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.addUnitChildBlock(unit) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningGold,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Çocuk Veri Bloğu Ekle (+ ChildBlock)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Even without child block, hair color can be edited on rawBlockEnd
            HairColorEditorCard(
                unit = unit,
                viewModel = viewModel,
                modCount = modCount
            )
            return@CollapsibleCard
        }

        // Active Child Block State
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Status & Sibling Info Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AwakeningGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "ÇOCUK BİRİM",
                                color = AwakeningGoldBright,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (siblingId != -1) {
                            val sibBmp = remember(siblingId) {
                                PortraitManager.getPortraitByUnitId(context, siblingId)
                            }
                            if (sibBmp != null) {
                                Image(
                                    bitmap = sibBmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AwakeningDarkBg)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Kardeş: ${UnitDb.getUnitName(siblingId)}",
                                color = AwakeningTextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Kardeş Tespit Edilmedi",
                                color = AwakeningTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Remove Child Block Button
                    OutlinedButton(
                        onClick = { showConfirmRemoveBlock = true },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningCrimson),
                        border = BorderStroke(1.dp, AwakeningCrimson.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bloğu Kaldır",
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 2. Primary Parents: Father (Slot 0) & Mother (Slot 1)
            ParentSlotCard(
                title = "Baba (Father)",
                slot = 0,
                unit = unit,
                viewModel = viewModel,
                modCount = modCount,
                onOpenPicker = { pickerSlotToOpen = 0 }
            )

            ParentSlotCard(
                title = "Anne (Mother)",
                slot = 1,
                unit = unit,
                viewModel = viewModel,
                modCount = modCount,
                onOpenPicker = { pickerSlotToOpen = 1 }
            )

            // 3. Family Support Levels (Father, Mother, Sibling)
            FamilySupportControlCard(
                unit = unit,
                viewModel = viewModel,
                modCount = modCount,
                siblingId = siblingId
            )

            // 4. Grandparents (Slots 2..5) - Collapsible Sub-card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedGrandparents = !expandedGrandparents },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Elderly,
                                contentDescription = null,
                                tint = AwakeningGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Büyükanne & Büyükbaba Verileri (Grandparents)",
                                color = AwakeningTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (expandedGrandparents) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AwakeningTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (expandedGrandparents) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val grandSlots = listOf(
                            Triple(2, "Baba Tarafı Dede", "Paternal Grandfather"),
                            Triple(3, "Baba Tarafı Büyükanne", "Paternal Grandmother"),
                            Triple(4, "Anne Tarafı Dede", "Maternal Grandfather"),
                            Triple(5, "Anne Tarafı Büyükanne", "Maternal Grandmother")
                        )

                        grandSlots.forEach { (slot, trTitle, enTitle) ->
                            GrandparentSlotRow(
                                title = trTitle,
                                enTitle = enTitle,
                                slot = slot,
                                unit = unit,
                                viewModel = viewModel,
                                modCount = modCount,
                                onOpenPicker = { pickerSlotToOpen = slot }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // 5. Offspring Hair Color Management
            HairColorEditorCard(
                unit = unit,
                viewModel = viewModel,
                modCount = modCount
            )
        }
    }

    // Parent Picker Dialog
    pickerSlotToOpen?.let { slot ->
        val slotTitle = when (slot) {
            0 -> "Baba (Father)"
            1 -> "Anne (Mother)"
            2 -> "Baba Tarafı Dede"
            3 -> "Baba Tarafı Büyükanne"
            4 -> "Anne Tarafı Dede"
            5 -> "Anne Tarafı Büyükanne"
            else -> "Slot $slot"
        }

        ParentPickerDialog(
            title = slotTitle,
            slot = slot,
            currentParentId = unit.rawChild?.parentId(slot) ?: 65535,
            onDismiss = { pickerSlotToOpen = null },
            onSelectParent = { selectedId ->
                viewModel.setUnitParent(unit, slot, selectedId)
                pickerSlotToOpen = null
            }
        )
    }

    // Confirm Remove Block Dialog
    if (showConfirmRemoveBlock) {
        AlertDialog(
            onDismissRequest = { showConfirmRemoveBlock = false },
            title = {
                Text(
                    text = "Çocuk Bloğunu Kaldır",
                    color = AwakeningCrimson,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "${unit.unitName()} biriminin çocuk veri bloğu ve ebeveyn kayıtları silinecek. Birim normal bir birime dönüştürülecek. Devam etmek istiyor musunuz?",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeUnitChildBlock(unit)
                        showConfirmRemoveBlock = false
                    },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCrimson),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Kaldır", color = AwakeningTextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmRemoveBlock = false },
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
private fun ParentSlotCard(
    title: String,
    slot: Int,
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long,
    onOpenPicker: () -> Unit
) {
    val context = LocalContext.current
    val child = unit.rawChild ?: return

    val parentId = remember(unit, slot, modCount) { child.parentId(slot) }
    val asset = remember(unit, slot, modCount) { child.asset(slot) }
    val flaw = remember(unit, slot, modCount) { child.flaw(slot) }

    val parentName = remember(parentId) {
        if (parentId == 65535 || parentId == -1) "Yok (Tanımsız)" else UnitDb.getUnitName(parentId)
    }
    val portraitBmp = remember(parentId) {
        if (parentId != 65535 && parentId != -1) {
            PortraitManager.getPortraitByUnitId(context, parentId)
        } else null
    }

    var assetMenuExpanded by remember { mutableStateOf(false) }
    var flawMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Avatar + Name + Change Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (portraitBmp != null) {
                    Image(
                        bitmap = portraitBmp.asImageBitmap(),
                        contentDescription = parentName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AwakeningNavySurface)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AwakeningNavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = AwakeningTextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = AwakeningGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = parentName,
                        color = AwakeningTextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenPicker,
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningNavySurface,
                        contentColor = AwakeningGoldBright
                    ),
                    border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Değiştir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Modifiers: Asset (Boon) & Flaw (Bane)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Asset Picker Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val assetLabel = if (asset in 0 until MiscDb.modifNames.size) MiscDb.modifNames[asset] else "None"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { assetMenuExpanded = true },
                        color = AwakeningNavySurface,
                        border = BorderStroke(1.dp, AwakeningSuccess.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "+Asset: $assetLabel",
                                color = AwakeningSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AwakeningSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = assetMenuExpanded,
                        onDismissRequest = { assetMenuExpanded = false },
                        modifier = Modifier.background(AwakeningNavySurface)
                    ) {
                        MiscDb.modifNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (idx == 0) "Yok (None)" else "+$name",
                                        color = if (idx == asset) AwakeningGoldBright else AwakeningTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (idx == asset) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setUnitParentAsset(unit, slot, idx)
                                    assetMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Flaw Picker Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    val flawLabel = if (flaw in 0 until MiscDb.modifNames.size) MiscDb.modifNames[flaw] else "None"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { flawMenuExpanded = true },
                        color = AwakeningNavySurface,
                        border = BorderStroke(1.dp, AwakeningCrimson.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "-Flaw: $flawLabel",
                                color = AwakeningCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AwakeningCrimson,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = flawMenuExpanded,
                        onDismissRequest = { flawMenuExpanded = false },
                        modifier = Modifier.background(AwakeningNavySurface)
                    ) {
                        MiscDb.modifNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (idx == 0) "Yok (None)" else "-$name",
                                        color = if (idx == flaw) AwakeningGoldBright else AwakeningTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (idx == flaw) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.setUnitParentFlaw(unit, slot, idx)
                                    flawMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrandparentSlotRow(
    title: String,
    enTitle: String,
    slot: Int,
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long,
    onOpenPicker: () -> Unit
) {
    val child = unit.rawChild ?: return
    val parentId = remember(unit, slot, modCount) { child.parentId(slot) }
    val asset = remember(unit, slot, modCount) { child.asset(slot) }
    val flaw = remember(unit, slot, modCount) { child.flaw(slot) }

    val parentName = remember(parentId) {
        if (parentId == 65535 || parentId == -1) "Yok" else UnitDb.getUnitName(parentId)
    }

    var assetMenuExpanded by remember { mutableStateOf(false) }
    var flawMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningNavySurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = AwakeningTextSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = parentName,
                        color = if (parentId != 65535 && parentId != -1) AwakeningTextPrimary else AwakeningTextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenPicker,
                    modifier = Modifier.height(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningCardSurface,
                        contentColor = AwakeningGold
                    ),
                    border = BorderStroke(0.5.dp, AwakeningGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Seç", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compact Asset & Flaw row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Asset
                Box(modifier = Modifier.weight(1f)) {
                    val assetName = if (asset in 0 until MiscDb.modifNames.size) MiscDb.modifNames[asset] else "None"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { assetMenuExpanded = true },
                        color = AwakeningCardSurface,
                        border = BorderStroke(0.5.dp, AwakeningSuccess.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+$assetName",
                                color = AwakeningSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = assetMenuExpanded,
                        onDismissRequest = { assetMenuExpanded = false },
                        modifier = Modifier.background(AwakeningNavySurface)
                    ) {
                        MiscDb.modifNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text(if (idx == 0) "Yok" else "+$name", fontSize = 11.sp) },
                                onClick = {
                                    viewModel.setUnitParentAsset(unit, slot, idx)
                                    assetMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Flaw
                Box(modifier = Modifier.weight(1f)) {
                    val flawName = if (flaw in 0 until MiscDb.modifNames.size) MiscDb.modifNames[flaw] else "None"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { flawMenuExpanded = true },
                        color = AwakeningCardSurface,
                        border = BorderStroke(0.5.dp, AwakeningCrimson.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "-$flawName",
                                color = AwakeningCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = flawMenuExpanded,
                        onDismissRequest = { flawMenuExpanded = false },
                        modifier = Modifier.background(AwakeningNavySurface)
                    ) {
                        MiscDb.modifNames.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text(if (idx == 0) "Yok" else "-$name", fontSize = 11.sp) },
                                onClick = {
                                    viewModel.setUnitParentFlaw(unit, slot, idx)
                                    flawMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilySupportControlCard(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long,
    siblingId: Int
) {
    val child = unit.rawChild ?: return

    val fatherVal = remember(unit, modCount) { child.supportParentValue(true) }
    val motherVal = remember(unit, modCount) { child.supportParentValue(false) }
    val siblingVal = remember(unit, modCount) { child.supportSiblingValue() }

    val fatherRank = remember(fatherVal) { UnitDb.getChildSupportLevelName(fatherVal) }
    val motherRank = remember(motherVal) { UnitDb.getChildSupportLevelName(motherVal) }
    val siblingRank = remember(siblingVal) { UnitDb.getChildSupportLevelName(siblingVal) }

    var expandedDetail by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AwakeningGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aile Destek Bağları (Family Supports)",
                        color = AwakeningGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { expandedDetail = !expandedDetail },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (expandedDetail) Icons.Default.ExpandLess else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (expandedDetail) AwakeningGold else AwakeningTextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Father Support Row
            SupportLevelPillRow(
                label = "Baba Desteği (Father)",
                levelName = fatherRank,
                onRankSelected = { lvl -> viewModel.setChildSupportParent(unit, true, lvl) }
            )

            if (expandedDetail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baba Puanı (0-22):", fontSize = 11.sp, color = AwakeningTextSecondary)
                    NumberStepper(
                        value = fatherVal,
                        min = 0,
                        max = 22,
                        onValueChange = { pts -> viewModel.setChildSupportParentPoints(unit, true, pts) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mother Support Row
            SupportLevelPillRow(
                label = "Anne Desteği (Mother)",
                levelName = motherRank,
                onRankSelected = { lvl -> viewModel.setChildSupportParent(unit, false, lvl) }
            )

            if (expandedDetail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Anne Puanı (0-22):", fontSize = 11.sp, color = AwakeningTextSecondary)
                    NumberStepper(
                        value = motherVal,
                        min = 0,
                        max = 22,
                        onValueChange = { pts -> viewModel.setChildSupportParentPoints(unit, false, pts) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sibling Support Row
            SupportLevelPillRow(
                label = if (siblingId != -1) "Kardeş Desteği (${UnitDb.getUnitName(siblingId)})" else "Kardeş Desteği (Sibling)",
                levelName = siblingRank,
                onRankSelected = { lvl -> viewModel.setChildSupportSibling(unit, lvl) }
            )

            if (expandedDetail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kardeş Puanı (0-22):", fontSize = 11.sp, color = AwakeningTextSecondary)
                    NumberStepper(
                        value = siblingVal,
                        min = 0,
                        max = 22,
                        onValueChange = { pts -> viewModel.setChildSupportSiblingPoints(unit, pts) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportLevelPillRow(
    label: String,
    levelName: String,
    onRankSelected: (Int) -> Unit
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
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                        .clickable { onRankSelected(rLevel) },
                    color = if (isActive) AwakeningGold else AwakeningNavySurface,
                    border = BorderStroke(1.dp, if (isActive) AwakeningGoldBright else AwakeningBorderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = rLabel,
                            color = if (isActive) AwakeningDarkBg else AwakeningTextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HairColorEditorCard(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long
) {
    val context = LocalContext.current
    val currentHex = remember(unit, modCount) {
        unit.rawBlockEnd?.hairColor ?: "FFFFFF"
    }

    var customHexInput by remember(currentHex) { mutableStateOf(currentHex) }

    val portraitBmp = remember(unit, modCount, currentHex) {
        PortraitManager.getPortrait(context, unit)
    }

    // Color swatch parsing
    val parsedColor = remember(currentHex) {
        try {
            val colorInt = android.graphics.Color.parseColor("#$currentHex")
            ComposeColor(colorInt)
        } catch (e: Exception) {
            ComposeColor.White
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = AwakeningGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Saç Rengi (Offspring Hair Color)",
                    color = AwakeningGoldBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Color Card with Portrait & Color Swatch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live preview portrait
                if (portraitBmp != null) {
                    Image(
                        bitmap = portraitBmp.asImageBitmap(),
                        contentDescription = "Saç Önizleme",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AwakeningNavySurface)
                            .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(6.dp))
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Color Swatch Box + Hex Code
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AwakeningNavySurface)
                        .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(1.5.dp, AwakeningDarkBg, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aktif Renk Kodu",
                            color = AwakeningTextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "#$currentHex",
                            color = AwakeningTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parent Quick Inherit Buttons
            val fatherId = unit.rawChild?.parentId(0) ?: 65535
            val motherId = unit.rawChild?.parentId(1) ?: 65535

            val hasFather = fatherId != 65535 && fatherId != -1
            val hasMother = motherId != 65535 && motherId != -1

            if (hasFather || hasMother) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hasFather) {
                        val fHair = remember(fatherId) {
                            UnitDb.getUnitHairColor(fatherId).removePrefix("#").take(6)
                        }
                        val fName = UnitDb.getUnitName(fatherId)
                        Button(
                            onClick = { viewModel.setUnitHairColor(unit, fHair) },
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningGold
                            ),
                            border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Babadan ($fName)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (hasMother) {
                        val mHair = remember(motherId) {
                            UnitDb.getUnitHairColor(motherId).removePrefix("#").take(6)
                        }
                        val mName = UnitDb.getUnitName(motherId)
                        Button(
                            onClick = { viewModel.setUnitHairColor(unit, mHair) },
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningGold
                            ),
                            border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Anneden ($mName)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Quick Iconic Awakening Palettes
            Text(
                text = "Hızlı Palet Seçimi:",
                color = AwakeningTextSecondary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            val palettes = listOf(
                "505c81" to "Lucina/Chrom",
                "dad3bd" to "Owain/Lissa",
                "af5454" to "Cordelia/Severa",
                "2e2e2e" to "Robin Siyah",
                "ecebea" to "Robin Beyaz",
                "c2d6ae" to "Nowi/Nah",
                "f99eaf" to "Olivia/Morgan",
                "a3726b" to "Gregor Kahve",
                "6c5b7b" to "Tharja Mor",
                "8fa4b3" to "Sumia/Cynthia",
                "e6c875" to "Maribelle Sarı",
                "cbc2a8" to "Libra/Brady"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                palettes.take(6).forEach { (hex, _) ->
                    PaletteColorCircle(
                        hex = hex,
                        isSelected = currentHex.equals(hex, ignoreCase = true),
                        onSelect = { viewModel.setUnitHairColor(unit, hex) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                palettes.drop(6).take(6).forEach { (hex, _) ->
                    PaletteColorCircle(
                        hex = hex,
                        isSelected = currentHex.equals(hex, ignoreCase = true),
                        onSelect = { viewModel.setUnitHairColor(unit, hex) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Hex Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customHexInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                        customHexInput = filtered
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AwakeningTextPrimary),
                    singleLine = true,
                    placeholder = { Text("RRGGBB", fontSize = 11.sp, color = AwakeningTextTertiary) },
                    prefix = { Text("#", color = AwakeningGold, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AwakeningGold,
                        unfocusedBorderColor = AwakeningBorderSubtle,
                        focusedContainerColor = AwakeningNavySurface,
                        unfocusedContainerColor = AwakeningNavySurface
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Button(
                    onClick = {
                        if (customHexInput.length == 6) {
                            viewModel.setUnitHairColor(unit, customHexInput)
                        }
                    },
                    enabled = customHexInput.length == 6,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningGold,
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        disabledContainerColor = AwakeningBorderSubtle,
                        disabledContentColor = AwakeningTextTertiary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Text("Uygula", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PaletteColorCircle(
    hex: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val color = remember(hex) {
        try {
            val colorInt = android.graphics.Color.parseColor("#$hex")
            ComposeColor(colorInt)
        } catch (e: Exception) {
            ComposeColor.White
        }
    }

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) AwakeningGoldBright else AwakeningBorderSubtle,
                shape = CircleShape
            )
            .clickable { onSelect() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPickerDialog(
    title: String,
    slot: Int,
    currentParentId: Int,
    onDismiss: () -> Unit,
    onSelectParent: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Pre-build unit list
    val allUnits = remember {
        val total = UnitDb.getUnitCount()
        (0 until total).map { id ->
            Triple(id, UnitDb.getUnitName(id), UnitDb.isUnitFemale(id))
        }
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allUnits
        else allUnits.filter { (_, name, _) ->
            name.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = AwakeningGoldBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                AwakeningSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Ebeveyn ara...",
                    containerColor = AwakeningDarkBg
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option: "None / Yok (Sıfırla)"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelectParent(65535) },
                    color = if (currentParentId == 65535 || currentParentId == -1) AwakeningGold.copy(alpha = 0.2f) else AwakeningCardSurface,
                    border = BorderStroke(1.dp, if (currentParentId == 65535 || currentParentId == -1) AwakeningGold else AwakeningBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AwakeningNavySurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = AwakeningCrimson, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Yok / Boş (None - 0xFFFF)",
                            color = AwakeningTextSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // Unit List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.first }) { (id, name, isFemale) ->
                        val isSelected = id == currentParentId
                        val portraitBmp = remember(id) {
                            PortraitManager.getPortraitByUnitId(context, id)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelectParent(id) },
                            color = if (isSelected) AwakeningGold.copy(alpha = 0.2f) else AwakeningCardSurface,
                            border = BorderStroke(1.dp, if (isSelected) AwakeningGold else AwakeningBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (portraitBmp != null) {
                                    Image(
                                        bitmap = portraitBmp.asImageBitmap(),
                                        contentDescription = name,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AwakeningDarkBg)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AwakeningDarkBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) AwakeningGoldBright else AwakeningTextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = if (isFemale) "Kadın (Female)" else "Erkek (Male)",
                                        color = AwakeningTextTertiary,
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = "#$id",
                                    color = AwakeningTextTertiary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
