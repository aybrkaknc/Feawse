package com.example.feawse.ui.progress

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.Chapter13
import com.example.feawse.savefile.barrack.RawEvent
import com.example.feawse.theme.*
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

private val EVENT_TYPES = listOf(
    0 to ("Kapalı / Boş" to "Etkinlik yok"),
    1 to ("İstatistik Artışı (+Stat)" to "Birim rastgele stat kazanır"),
    2 to ("Deneyim (+EXP)" to "Birim doğrudan EXP puanı alır"),
    3 to ("Silah Deneyimi (+Wpn EXP)" to "Silah kullanım seviyesi artar"),
    4 to ("Nadir Eşya / Hediye" to "Konvoya veya birime özel eşya verilir"),
    5 to ("Karakter Sohbeti" to "İki birim sohbet eder ve destek puanı kazanır"),
    6 to ("Doğum Günü Kutlaması" to "Özel kışla doğum günü kutlaması")
)

@Composable
fun BarracksSection(
    chapter: Chapter13,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val evst = chapter.blockEvst ?: return
    val modCount = uiState.modificationCount

    val events = remember(evst, modCount) { evst.eventList ?: emptyList() }

    var unitPickerTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) } // eventIndex to slot (1 or 2)
    var confirmDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    CollapsibleCard(
        title = "Kışla Etkinlikleri (Barracks Events)",
        subtitle = "5 Etkinlik Yuvası • Stat, EXP, Eşya ve Sohbet",
        icon = Icons.Default.Celebration,
        initialExpanded = false,
        modifier = modifier
    ) {
        // Fast Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    confirmDialog = "Tüm kışla yuvaları ordunuzdaki ana karakterler ve faydalı hediyelerle otomatik doldurulacak. Onaylıyor musunuz?" to {
                        viewModel.fillAllBarracksEvents()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AwakeningGold,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tüm Kışlayı Doldur",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }

            OutlinedButton(
                onClick = {
                    confirmDialog = "Tüm kışla etkinlikleri sıfırlanarak temizlenecek. Onaylıyor musunuz?" to {
                        viewModel.clearBarracksEvents()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = AwakeningTextSecondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kışlayı Temizle",
                    fontSize = 11.sp,
                    color = AwakeningTextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // 5 Events List
        events.forEachIndexed { index, event ->
            BarracksEventCard(
                eventIndex = index,
                event = event,
                onSelectUnit1 = { unitPickerTarget = index to 1 },
                onSelectUnit2 = { unitPickerTarget = index to 2 },
                onChangeType = { newType ->
                    viewModel.setBarracksEvent(index, newType, event.unit1(), event.unit2(), event.eventIcon())
                },
                onClear = {
                    viewModel.setBarracksEvent(index, 0, 65535, 65535, 0)
                }
            )

            if (index < events.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Unit Picker Dialog
    if (unitPickerTarget != null) {
        val (eventIdx, slotNum) = unitPickerTarget!!
        BarracksUnitPickerDialog(
            title = if (slotNum == 1) "1. Karakteri Seç (Etkinlik #${eventIdx + 1})" else "2. Karakteri Seç (Sohbet Partneri)",
            onDismiss = { unitPickerTarget = null },
            onSelectUnit = { selectedId ->
                val targetEvent = events.getOrNull(eventIdx)
                if (targetEvent != null) {
                    val u1 = if (slotNum == 1) selectedId else targetEvent.unit1()
                    val u2 = if (slotNum == 2) selectedId else targetEvent.unit2()
                    viewModel.setBarracksEvent(eventIdx, targetEvent.eventType(), u1, u2, targetEvent.eventIcon())
                }
                unitPickerTarget = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarracksEventCard(
    eventIndex: Int,
    event: RawEvent,
    onSelectUnit1: () -> Unit,
    onSelectUnit2: () -> Unit,
    onChangeType: (Int) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val eventType = event.eventType()
    val unit1 = event.unit1()
    val unit2 = event.unit2()

    var showTypeDropdown by remember { mutableStateOf(false) }

    val currentTypeLabel = remember(eventType) {
        EVENT_TYPES.firstOrNull { it.first == eventType }?.second?.first ?: "Özel ($eventType)"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (eventType > 0) AwakeningGold.copy(alpha = 0.35f) else AwakeningBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Slot number and Event Type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (eventType > 0) AwakeningGold else AwakeningNavySurface,
                        shape = CircleShape,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${eventIndex + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (eventType > 0) AwakeningDarkBg else AwakeningTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kışla Yuvası #${eventIndex + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type selector pill
                    Box {
                        Surface(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showTypeDropdown = true },
                            color = if (eventType > 0) AwakeningGoldContainer else AwakeningNavySurface,
                            border = BorderStroke(1.dp, if (eventType > 0) AwakeningGold else AwakeningBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTypeLabel,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (eventType > 0) AwakeningGoldBright else AwakeningTextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = AwakeningGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTypeDropdown,
                            onDismissRequest = { showTypeDropdown = false },
                            modifier = Modifier.background(AwakeningNavySurface)
                        ) {
                            EVENT_TYPES.forEach { (typeId, pair) ->
                                val (title, desc) = pair
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(title, fontSize = 12.sp, fontWeight = if (typeId == eventType) FontWeight.Bold else FontWeight.Normal, color = AwakeningTextPrimary)
                                            Text(desc, fontSize = 10.sp, color = AwakeningTextSecondary)
                                        }
                                    },
                                    onClick = {
                                        onChangeType(typeId)
                                        showTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    if (eventType > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextTertiary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (eventType > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Character Selectors Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unit 1
                    BarracksUnitSlotBox(
                        modifier = Modifier.weight(1f),
                        label = "1. Karakter",
                        unitId = unit1,
                        onClick = onSelectUnit1
                    )

                    // Unit 2 (visible for all, but highlighted for Conversation)
                    BarracksUnitSlotBox(
                        modifier = Modifier.weight(1f),
                        label = if (eventType == 5) "2. Karakter (Sohbet)" else "2. Karakter (Opsiyonel)",
                        unitId = unit2,
                        onClick = onSelectUnit2
                    )
                }
            }
        }
    }
}

@Composable
private fun BarracksUnitSlotBox(
    modifier: Modifier = Modifier,
    label: String,
    unitId: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isValid = unitId != 65535 && unitId >= 0
    val name = if (isValid) UnitDb.getUnitName(unitId) else "Yok / Boş (Seç)"
    val portrait = remember(unitId) {
        if (isValid) PortraitManager.getPortraitByUnitId(context, unitId) else null
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = AwakeningNavySurface,
        border = BorderStroke(1.dp, if (isValid) AwakeningBorderSubtle else AwakeningBorderSubtle.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AwakeningCardSurface)
                    .border(1.dp, if (isValid) AwakeningGold else AwakeningBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (portrait != null) {
                    Image(
                        bitmap = portrait.asImageBitmap(),
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = AwakeningTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 9.5.sp,
                    color = AwakeningTextSecondary
                )
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = if (isValid) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isValid) AwakeningTextPrimary else AwakeningTextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = AwakeningGold.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun BarracksUnitPickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelectUnit: (Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val allUnits = remember {
        val list = mutableListOf<Pair<Int, String>>()
        for (i in 0..120) {
            val name = UnitDb.getUnitName(i)
            if (!name.startsWith("Invalid") && !name.startsWith("None") && name.isNotBlank()) {
                list.add(i to name)
            }
        }
        list
    }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) allUnits
        else allUnits.filter { it.second.contains(searchQuery, ignoreCase = true) }
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

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Karakter ara...", fontSize = 12.sp, color = AwakeningTextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = AwakeningTextPrimary),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AwakeningGold,
                        unfocusedBorderColor = AwakeningBorderSubtle,
                        focusedContainerColor = AwakeningCardSurface,
                        unfocusedContainerColor = AwakeningCardSurface
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(16.dp))
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // None / Clear Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectUnit(65535) },
                    color = AwakeningCardSurface,
                    border = BorderStroke(1.dp, AwakeningBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Yok / Boş (None - 0xFFFF)", color = AwakeningTextTertiary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList, key = { it.first }) { (unitId, unitName) ->
                        val portrait = remember(unitId) { PortraitManager.getPortraitByUnitId(context, unitId) }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectUnit(unitId) },
                            color = AwakeningCardSurface,
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AwakeningNavySurface)
                                        .border(1.dp, AwakeningGold.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (portrait != null) {
                                        Image(
                                            bitmap = portrait.asImageBitmap(),
                                            contentDescription = unitName,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = unitName,
                                    color = AwakeningTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
