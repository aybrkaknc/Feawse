package com.example.feawse.ui.global

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.feawse.data.ClassDb
import com.example.feawse.data.ItemDb
import com.example.feawse.data.SkillDb
import com.example.feawse.savefile.units.Stats
import com.example.feawse.savefile.wireless.UnitDu
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.ui.components.PortraitAvatar
import com.example.feawse.ui.components.StatusBadge
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState
import java.io.File
import java.io.FileOutputStream

@Composable
fun GlobalScreen(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val global = uiState.globalFile ?: return
    val context = LocalContext.current
    var selectedSubTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }

    val tabs = listOf(
        "📖 Günlük (99)" to Icons.Default.MenuBook,
        "🏆 Şöhret & Kilitler" to Icons.Default.MilitaryTech,
        "🌐 Sistem & Bölge" to Icons.Default.Settings
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Selector Pills
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = adaptivePadding(), vertical = 8.dp),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    val isSelected = selectedSubTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clickable { selectedSubTab = index },
                        color = if (isSelected) AwakeningGold else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) AwakeningDarkBg else AwakeningTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title.split(" ").first(),
                                color = if (isSelected) AwakeningDarkBg else AwakeningTextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Sub Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                0 -> GlobalLogbookTab(viewModel = viewModel, uiState = uiState)
                1 -> GlobalRenownAndUnlocksTab(viewModel = viewModel, uiState = uiState)
                2 -> GlobalSystemTab(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

// =========================================================================
// Sekme 1: Global Logbook (99 Birim)
// =========================================================================

@Composable
fun GlobalLogbookTab(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val global = uiState.globalFile ?: return
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var unitToInspect by remember { mutableStateOf<UnitDu?>(null) }
    var unitToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    var unitToExport by remember { mutableStateOf<UnitDu?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    viewModel.importUnitBytesToGlobal(bytes)
                }
            } catch (e: Exception) {
                // Handled in VM
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null && unitToExport != null) {
            viewModel.exportUnitToUri(context, unitToExport!!.toUnit(), uri)
            unitToExport = null
        }
    }

    val unitList = global.glUnitBlock?.unitList ?: emptyList()
    val filteredList = remember(unitList, searchQuery, uiState.modificationCount) {
        if (searchQuery.isBlank()) {
            unitList.mapIndexed { idx, u -> idx to u }
        } else {
            unitList.mapIndexed { idx, u -> idx to u }.filter { (_, u) ->
                val name = u.name.lowercase()
                val className = ClassDb.getClassName(u.unitClass).lowercase()
                name.contains(searchQuery.lowercase()) || className.contains(searchQuery.lowercase())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding())
    ) {
        // Quick Actions Card (Unified Awakening Design)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            border = BorderStroke(1.dp, AwakeningBorderSubtle),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Logbook Birimleri",
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(
                        text = "${unitList.size} / 99",
                        backgroundColor = if (unitList.size >= 95) AwakeningCrimson else AwakeningCardSurface,
                        textColor = if (unitList.size >= 95) Color.White else AwakeningGold
                    )
                }

                Button(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = unitList.size < 99,
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningGold,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ".fe13u Ekle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Unified Awakening Search Bar
        AwakeningSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Birim adı veya sınıfı ile ara (örn. Marth, Paladin)..."
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "Günlükte kayıtlı hiç Avatar veya Einherjar birimi yok." else "Aramaya uygun birim bulunamadı.",
                    color = AwakeningTextTertiary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = filteredList,
                    key = { _, (originalIdx, unit) -> "$originalIdx-${unit.name}-${unit.unitClass}-${unit.level}" }
                ) { _, (originalIdx, unitDu) ->
                    GlobalUnitCard(
                        index = originalIdx,
                        totalCount = unitList.size,
                        unitDu = unitDu,
                        onInspect = { unitToInspect = unitDu },
                        onMoveUp = { viewModel.moveGlobalUnit(originalIdx, originalIdx - 1) },
                        onMoveDown = { viewModel.moveGlobalUnit(originalIdx, originalIdx + 1) },
                        onExport = {
                            unitToExport = unitDu
                            val cleanName = unitDu.name.replace(Regex("[^a-zA-Z0-9_]"), "_")
                            exportLauncher.launch("$cleanName.fe13u")
                        },
                        onShare = {
                            shareGlobalUnit(context, unitDu)
                        },
                        onDelete = { unitToDeleteIndex = originalIdx }
                    )
                }
            }
        }
    }

    // Detail Inspector Dialog
    if (unitToInspect != null) {
        GlobalUnitDetailDialog(
            unitDu = unitToInspect!!,
            onDismiss = { unitToInspect = null }
        )
    }

    // Delete Confirmation Dialog
    if (unitToDeleteIndex != null) {
        val idx = unitToDeleteIndex!!
        val unit = unitList.getOrNull(idx)
        AlertDialog(
            onDismissRequest = { unitToDeleteIndex = null },
            title = {
                Text(
                    text = "Birimi Günlükten Sil?",
                    color = AwakeningTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "\"${unit?.name ?: "Birim"}\" global kayıt günlüğünden kalıcı olarak silinecek. Emin misiniz?",
                    color = AwakeningTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGlobalUnit(idx)
                        unitToDeleteIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningCrimson,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { unitToDeleteIndex = null }) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }
}

@Composable
fun GlobalUnitCard(
    index: Int,
    totalCount: Int,
    unitDu: UnitDu,
    onInspect: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unit = remember(unitDu) { unitDu.toUnit() }
    val portraitBitmap = remember(unit) { PortraitManager.getPortrait(context, unit) }
    val isOutrealm = unitDu.hasDuFlag(1)
    val isSpotPass = unitDu.hasDuFlag(2)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() },
        color = AwakeningNavySurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index & Portrait
            Box(contentAlignment = Alignment.BottomEnd) {
                PortraitAvatar(bitmap = portraitBitmap, size = 52)
                Surface(
                    color = AwakeningDarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, AwakeningGold.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "#${index + 1}",
                        fontSize = 9.sp,
                        color = AwakeningGoldBright,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = unitDu.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AwakeningGoldBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isSpotPass) {
                        StatusBadge(text = "SpotPass", backgroundColor = AwakeningFalchionBlue.copy(alpha = 0.25f), textColor = AwakeningFalchionBlue)
                    } else if (isOutrealm) {
                        StatusBadge(text = "Outrealm", backgroundColor = AwakeningCrimson.copy(alpha = 0.25f), textColor = AwakeningCrimson)
                    } else {
                        StatusBadge(text = "Avatar", backgroundColor = AwakeningCardSurface, textColor = AwakeningGold)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val currentStats = remember(unit) { Stats.calcCurrentStats(unit, false) }
                val hp = currentStats.getOrNull(0) ?: 0

                Text(
                    text = "${ClassDb.getClassName(unitDu.unitClass)} • Lv. ${unitDu.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )

                Text(
                    text = "Can: $hp • Hareket: ${unitDu.movement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            // Quick Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Move Up
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(enabled = index > 0) { onMoveUp() },
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Yukarı",
                                tint = if (index > 0) AwakeningGold else AwakeningTextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Move Down
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(enabled = index < totalCount - 1) { onMoveDown() },
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Aşağı",
                                tint = if (index < totalCount - 1) AwakeningGold else AwakeningTextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Export (.fe13u)
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onExport() },
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = "Dışa Aktar",
                                tint = AwakeningFalchionBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Share
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onShare() },
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Paylaş",
                                tint = AwakeningTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Delete
                    Surface(
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDelete() },
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningCrimson.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = AwakeningCrimson,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Sekme 2: Şöhret & Kilit Açıcılar (Renown & Unlocks)
// =========================================================================

@Composable
fun GlobalRenownAndUnlocksTab(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val global = uiState.globalFile ?: return
    val user = global.glUserBlock ?: return
    var showFullUnlockConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = adaptivePadding(), vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Master Unlocker Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, AwakeningGoldBright)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = AwakeningGoldBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🌟 Büyük Kilit Açıcı (Master Unlocker)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AwakeningGoldBright
                        )
                        Text(
                            text = "Tüm Destek Günlüğü & Birim Galerisi",
                            style = MaterialTheme.typography.bodySmall,
                            color = AwakeningTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Oyunun Sohbet Tiyatrosu (Support Log) kütüphanesindeki tüm karakter sohbetlerini, Karakter Galerisini ve saç rengi bayraklarını tek dokunuşla %100 açar. Birimler kilitli veya soluk görünmez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showFullUnlockConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningGold,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tüm Destekleri & Galeriyi Aç",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 2. Global Renown Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Genel Şöhret (Global Renown)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AwakeningTextPrimary
                        )
                        Text(
                            text = "Yeni oyunlarda aktarılan toplam şöhret puanı",
                            style = MaterialTheme.typography.bodySmall,
                            color = AwakeningTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    NumberStepper(
                        value = user.renown,
                        onValueChange = { viewModel.setGlobalRenown(it) },
                        min = 0,
                        max = 999_999,
                        step = 500
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "+1.000" to 1_000,
                        "+5.000" to 5_000,
                        "+10.000" to 10_000,
                        "Maks (999.999)" to 999_999,
                        "Sıfırla" to 0
                    )

                    presets.forEach { (label, value) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clickable {
                                    val target = if (label.startsWith("+")) user.renown + value else value
                                    viewModel.setGlobalRenown(target)
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    color = if (label.contains("Maks")) AwakeningGoldBright else AwakeningTextPrimary,
                                    fontWeight = if (label.contains("Maks")) FontWeight.Bold else FontWeight.Medium,
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

        // 3. Game Clear & Difficulty Flags
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Oyun Modu & Zorluk Bayrakları",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AwakeningTextPrimary
                )
                Text(
                    text = "Ana menü kilitleri ve zorluk dereceleri",
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val flags = listOf(
                    Triple(1, "Oyun Bitirme Kilidi (Game Clear)", "Tiyatro, Destek Günlüğü ve Birim Galerisini ana menüde aktif eder."),
                    Triple(2, "Lunatic+ Modu (Klasik & Rahat)", "En yüksek zorluk seviyesi olan Lunatic+ modunu açar."),
                    Triple(3, "Lunatic+ Modu (Sadece Klasik)", "Lunatic+ modunda permadeath zorunlu kılınan özel bayrak.")
                )

                flags.forEach { (bit, title, desc) ->
                    val isChecked = user.hasGlobalFlag(bit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = if (isChecked) AwakeningGoldBright else AwakeningTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = desc,
                                color = AwakeningTextTertiary,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { viewModel.setGlobalFlag(bit, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningGold,
                                uncheckedThumbColor = AwakeningTextTertiary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }
                    if (bit != 3) {
                        HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (showFullUnlockConfirm) {
        AlertDialog(
            onDismissRequest = { showFullUnlockConfirm = false },
            title = {
                Text("Tüm Destekleri ve Galeriyi Aç?", fontWeight = FontWeight.Bold, color = AwakeningGoldBright)
            },
            text = {
                Text(
                    "Bu işlem, tüm oyun içi destek konuşmalarını, karakter galerisini ve tüm saç renklerini 100% açacaktır. Emin misiniz?",
                    color = AwakeningTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unlockAllSupportsAndGallery()
                        showFullUnlockConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningGold,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text("Evet, Hepsini Aç", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullUnlockConfirm = false }) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }
}

// =========================================================================
// Sekme 3: Sistem & Bölge Bilgileri (System & Region)
// =========================================================================

@Composable
fun GlobalSystemTab(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val global = uiState.globalFile ?: return
    val context = LocalContext.current
    val isWest = global.region()
    var showRegionChangeConfirm by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = adaptivePadding(), vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Region Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Bölge & Dil Ayarı (Save Region)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AwakeningTextPrimary
                )
                Text(
                    text = "Global dosyasının başlık ve metin kodlaması",
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mevcut Bölge:",
                            color = AwakeningTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isWest) "USA / Europe (Batı)" else "Japan (Japonya)",
                            color = AwakeningGoldBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { showRegionChangeConfirm = true },
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningCardSurface,
                            contentColor = AwakeningGold
                        ),
                        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWest) "Japonya'ya Çevir" else "Batı'ya Çevir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // File Details Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Dosya Bilgileri",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AwakeningTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val details = listOf(
                    "Dosya Türü" to "Nintendo 3DS Global Save File",
                    "Dosya Adı" to (uiState.currentFileName ?: "global"),
                    "Logbook Birim Sayısı" to "${global.glUnitBlock?.unitList?.size ?: 0} / 99",
                    "Kayıt Durumu" to if (uiState.isModified) "Değişiklikler Var (Kaydedilmedi)" else "Güncel"
                )

                details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = AwakeningTextSecondary, fontSize = 11.5.sp)
                        Text(
                            text = value,
                            color = if (label.contains("Durumu") && uiState.isModified) AwakeningGoldBright else AwakeningTextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Save & Backup Actions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Kayıt & Güvenlik İşlemleri",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AwakeningTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.createManualBackup(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningCardSurface,
                            contentColor = AwakeningTextPrimary
                        ),
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp), tint = AwakeningGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Güvenlik Yedeği", fontSize = 11.5.sp, maxLines = 1, softWrap = false)
                    }

                    Button(
                        onClick = { showSaveConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningRoyalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kaydet (global)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }

    if (showRegionChangeConfirm) {
        val target = if (isWest) "Japonya (JPN)" else "Batı (USA/EUR)"
        AlertDialog(
            onDismissRequest = { showRegionChangeConfirm = false },
            title = {
                Text("Bölgeyi Değiştir?", fontWeight = FontWeight.Bold, color = AwakeningGoldBright)
            },
            text = {
                Text(
                    "Global dosyasının bölgesi $target olarak güncellenecek. Einherjar birimlerinin isim kodlaması dönüştürülecektir. Onaylıyor musunuz?",
                    color = AwakeningTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeGlobalRegion(!isWest)
                        showRegionChangeConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningRoyalBlue, contentColor = Color.White)
                ) {
                    Text("Onayla", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegionChangeConfirm = false }) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }

    // Save Confirmation Dialog (Fire Emblem Awakening Theme)
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AwakeningRoyalBlueContainer,
                        modifier = Modifier.size(36.dp),
                        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = AwakeningGoldBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Global Kayıt Kaydedilsin mi?",
                        fontWeight = FontWeight.Bold,
                        color = AwakeningTextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Global dosyasında yapılan tüm değişiklikler kalıcı olarak yazılacaktır. Devam etmek istiyor musunuz?",
                        color = AwakeningTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AwakeningDarkBg,
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = AwakeningRoyalBlueBright,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "global",
                                    color = AwakeningTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "Genel Veri (Global Save - Şöhret & Ekstralar)",
                                color = AwakeningFalchionCyan,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmDialog = false
                        viewModel.saveFile(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwakeningRoyalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Evet, Kaydet", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSaveConfirmDialog = false },
                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningTextSecondary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("İptal", fontSize = 12.5.sp)
                }
            }
        )
    }
}

// =========================================================================
// Global Unit Detail Inspector Dialog
// =========================================================================

@Composable
fun GlobalUnitDetailDialog(
    unitDu: UnitDu,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val unit = remember(unitDu) { unitDu.toUnit() }
    val portraitBitmap = remember(unit) { PortraitManager.getPortrait(context, unit) }
    val currentStats = remember(unit) { Stats.calcCurrentStats(unit, false) }
    val hp = currentStats.getOrNull(0) ?: (unitDu.growth.getOrNull(0) ?: 0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Portrait & Basic Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PortraitAvatar(bitmap = portraitBitmap, size = 64)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = unitDu.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AwakeningGoldBright
                        )
                        Text(
                            text = "${ClassDb.getClassName(unitDu.unitClass)} • Seviye ${unitDu.level}",
                            color = AwakeningTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Can (HP): $hp • Hareket: ${unitDu.movement}",
                            color = AwakeningTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                // Stats Summary
                Text(
                    text = "Birim İstatistikleri / Büyümeler:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AwakeningTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val stats = listOf(
                    "HP" to hp,
                    "Str" to (currentStats.getOrNull(1) ?: (unitDu.growth.getOrNull(0) ?: 0)),
                    "Mag" to (currentStats.getOrNull(2) ?: (unitDu.growth.getOrNull(1) ?: 0)),
                    "Skl" to (currentStats.getOrNull(3) ?: (unitDu.growth.getOrNull(2) ?: 0)),
                    "Spd" to (currentStats.getOrNull(4) ?: (unitDu.growth.getOrNull(3) ?: 0)),
                    "Lck" to (currentStats.getOrNull(5) ?: (unitDu.growth.getOrNull(4) ?: 0)),
                    "Def" to (currentStats.getOrNull(6) ?: (unitDu.growth.getOrNull(5) ?: 0)),
                    "Res" to (currentStats.getOrNull(7) ?: (unitDu.growth.getOrNull(6) ?: 0))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stats.forEach { (statName, statVal) ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = statName, fontSize = 9.sp, color = AwakeningTextTertiary)
                                Text(text = "$statVal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AwakeningGold)
                            }
                        }
                    }
                }

                // Active Skills
                Text(
                    text = "Aktif Yetenekler:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AwakeningTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val skills = unitDu.activeSkills.filter { it > 0 }
                if (skills.isEmpty()) {
                    Text(text = "Hiç aktif yetenek atanmamış.", fontSize = 11.sp, color = AwakeningTextTertiary)
                } else {
                    val skillNames = remember { SkillDb.getSkillNames() }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        skills.forEach { skillId ->
                            val skillName = if (skillId in skillNames.indices) skillNames[skillId] else "Yetenek #$skillId"
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = AwakeningCardSurface,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                            ) {
                                Text(
                                    text = skillName,
                                    fontSize = 10.sp,
                                    color = AwakeningTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // Weapons & Inventory
                Text(
                    text = "Kuşanılan Eşyalar & Silahlar:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AwakeningTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val validItems = unitDu.itemList.filter { it.itemId > 0 }
                if (validItems.isEmpty()) {
                    Text(text = "Envanter boş.", fontSize = 11.sp, color = AwakeningTextTertiary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        validItems.forEach { item ->
                            val itemName = ItemDb.getItemName(item.itemId)
                            val forgedName = item.name
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = AwakeningCardSurface,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (forgedName.isNotBlank() && forgedName != itemName) "⚔️ $forgedName ($itemName)" else "🗡️ $itemName",
                                        fontSize = 11.sp,
                                        color = if (forgedName.isNotBlank() && forgedName != itemName) AwakeningGoldBright else AwakeningTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (item.isRefinement) {
                                        Text(
                                            text = "Mt:+${item.might()} Hit:+${item.hit()} Crit:+${item.crit()}",
                                            fontSize = 10.sp,
                                            color = AwakeningGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Profile Messages
                val greeting = unitDu.rawLog?.getTextGreeting() ?: ""
                val challenge = unitDu.rawLog?.getTextChallenge() ?: ""
                val recruit = unitDu.rawLog?.getTextRecruit() ?: ""

                if (greeting.isNotBlank() || challenge.isNotBlank() || recruit.isNotBlank()) {
                    Text(
                        text = "Profil / StreetPass Mesajları:",
                        style = MaterialTheme.typography.labelSmall,
                        color = AwakeningTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (greeting.isNotBlank()) Text("💬 Karşılama: $greeting", fontSize = 10.5.sp, color = AwakeningTextPrimary)
                            if (challenge.isNotBlank()) Text("⚔️ Meydan Okuma: $challenge", fontSize = 10.5.sp, color = AwakeningCrimson)
                            if (recruit.isNotBlank()) Text("🤝 Katılma: $recruit", fontSize = 10.5.sp, color = AwakeningFalchionBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCardSurface, contentColor = AwakeningTextPrimary),
                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Kapat", fontSize = 12.sp)
                }
            }
        }
    }
}

// Helper to share unit via Android Share Sheet
private fun shareGlobalUnit(context: Context, unitDu: UnitDu) {
    try {
        val unit = unitDu.toUnit()
        val bytes = unit.getUnitBytes()
        val cleanName = unitDu.name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val cacheDir = File(context.cacheDir, "exported_units")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val cacheFile = File(cacheDir, "$cleanName.fe13u")
        cacheFile.writeBytes(bytes)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "FE: Awakening Birimi: ${unitDu.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Birimi Paylaş (.fe13u)"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
