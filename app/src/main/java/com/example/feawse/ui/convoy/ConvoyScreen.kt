package com.example.feawse.ui.convoy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.data.ItemDb
import com.example.feawse.savefile.inventory.Refinement
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvoyScreen(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val chapter = uiState.chapterFile
    if (chapter == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Lütfen önce Ana Ekrandan bir kayıt dosyası açın.", color = AwakeningTextSecondary)
        }
        return
    }

    val tranBlock = chapter.blockTran
    val refiBlock = chapter.blockRefi
    val compact = isCompact()

    var selectedScreenTab by rememberSaveable { mutableIntStateOf(0) } // 0: Normal Eşyalar, 1: Dövülmüş Silahlar

    // Normal Convoy State
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) } // 0: All, 1: Weapons, 2: Items

    val allItemNames = remember { ItemDb.getItemNamesRegular() }
    val convoyMain = remember(tranBlock, uiState.modificationCount) {
        tranBlock?.inventoryMain ?: emptyList()
    }

    val displayedItems = remember(convoyMain, searchQuery, selectedCategory) {
        val list = mutableListOf<Triple<Int, String, Int>>() // id, name, uses
        for (id in 0 until minOf(convoyMain.size, allItemNames.size)) {
            val uses = convoyMain[id]
            val name = allItemNames[id]
            if (name.isBlank() || name == "None") continue

            val matchesSearch = searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                1 -> uses > 0 // Only existing items
                2 -> uses == 0 // Available to add
                else -> true
            }

            if (matchesSearch && matchesCategory) {
                list.add(Triple(id, name, uses))
            }
        }
        list
    }

    // Forged Weapons State
    val refiList = remember(refiBlock, uiState.modificationCount) {
        refiBlock?.refiList ?: emptyList()
    }
    var forgedSearchQuery by rememberSaveable { mutableStateOf("") }
    var isForgedSearchExpanded by rememberSaveable { mutableStateOf(false) }

    val displayedForged = remember(refiList, forgedSearchQuery, uiState.modificationCount) {
        refiList.filter { refi ->
            if (forgedSearchQuery.isBlank()) true
            else {
                refi.name.contains(forgedSearchQuery, ignoreCase = true) ||
                        ItemDb.getItemName(refi.weaponId()).contains(forgedSearchQuery, ignoreCase = true)
            }
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingRefinement by remember { mutableStateOf<Refinement?>(null) }
    var weaponToDelete by remember { mutableStateOf<Refinement?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Screen Mode Segmented Tabs
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            TabRow(
                selectedTabIndex = selectedScreenTab,
                containerColor = Color.Transparent,
                contentColor = AwakeningGold,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedScreenTab]),
                        color = AwakeningGold,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedScreenTab == 0,
                    onClick = { selectedScreenTab = 0 },
                    text = {
                        Text(
                            text = "Normal Eşyalar",
                            fontSize = 13.sp,
                            fontWeight = if (selectedScreenTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedScreenTab == 0) AwakeningGoldBright else AwakeningTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedScreenTab == 1,
                    onClick = { selectedScreenTab = 1 },
                    text = {
                        Text(
                            text = "Dövülmüş Silahlar (${refiList.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedScreenTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedScreenTab == 1) AwakeningGoldBright else AwakeningTextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedScreenTab == 0) {
            // ==================== TAB 0: NORMAL ITEMS ====================

            // Quick Actions Card
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
                    Text(
                        text = "Konvoy Deposu",
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = { viewModel.repairAllConvoy() },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningGold,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (compact) "Tamir Et" else "Tümünü Tamir Et",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Normal Items Search Bar (Unified Awakening Design)
            AwakeningSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Eşya veya silah ara (örn. Killing Edge, Elixir)..."
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == 0,
                        onClick = { selectedCategory = 0 },
                        label = { Text("Tüm Eşyalar", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == 0,
                            borderColor = AwakeningBorderSubtle,
                            selectedBorderColor = AwakeningGold,
                            borderWidth = 1.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AwakeningGold,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = AwakeningNavySurface,
                            labelColor = AwakeningTextSecondary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == 1,
                        onClick = { selectedCategory = 1 },
                        label = { Text("Mevcutlar", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == 1,
                            borderColor = AwakeningBorderSubtle,
                            selectedBorderColor = AwakeningGold,
                            borderWidth = 1.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AwakeningGold,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = AwakeningNavySurface,
                            labelColor = AwakeningTextSecondary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == 2,
                        onClick = { selectedCategory = 2 },
                        label = { Text("Sıfır / Eklenebilir", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == 2,
                            borderColor = AwakeningBorderSubtle,
                            selectedBorderColor = AwakeningGold,
                            borderWidth = 1.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AwakeningGold,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = AwakeningNavySurface,
                            labelColor = AwakeningTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(displayedItems) { (id, name, uses) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningNavySurface,
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AwakeningTextPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val maxDefaultUses = if (id < ItemDb.getItemCountVanilla()) ItemDb.getItemUses(id) else 30
                                Text(
                                    text = "Varsayılan: $maxDefaultUses kullanım",
                                    fontSize = 11.sp,
                                    color = AwakeningTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            NumberStepper(
                                value = uses,
                                min = 0,
                                max = 99,
                                step = 1,
                                onValueChange = { newUses ->
                                    viewModel.setConvoyItemUses(id, newUses)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // ==================== TAB 1: FORGED WEAPONS ====================

            // Action Header Card for Forge
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
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Silah Atölyesi",
                            fontWeight = FontWeight.Bold,
                            color = AwakeningGoldBright,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AwakeningGoldContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${refiList.size} / 150",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwakeningGoldBright,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningGold,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            enabled = refiList.size < 150
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Yeni Silah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.repairAllForgedWeapons() },
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Yenile",
                                fontSize = 11.sp,
                                color = AwakeningTextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Forged Search Bar (Unified Awakening Design)
            AwakeningSearchBar(
                query = forgedSearchQuery,
                onQueryChange = { forgedSearchQuery = it },
                placeholder = "Dövülmüş silah ara..."
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Forged Weapons List or Empty State
            if (refiList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = AwakeningGold.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Henüz hiç dövülmüş silah yok.",
                            fontWeight = FontWeight.SemiBold,
                            color = AwakeningTextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yukarıdaki 'Yeni Silah' butonuna tıklayarak istediğiniz temel silahı geliştirip isimlendirebilirsiniz.",
                            color = AwakeningTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("İlk Silahı Döv", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedForged) { refi ->
                        val convoyUses = tranBlock?.inventoryRefi?.getOrNull(refi.position()) ?: 0
                        val baseName = ItemDb.getItemName(refi.weaponId())

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AwakeningNavySurface,
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Top row: Title + Badges + Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AwakeningGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = refi.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = AwakeningGoldBright,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (refi.isEnemy) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AwakeningCrimson.copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, AwakeningCrimson),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Düşman",
                                                    color = AwakeningCrimson,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Action buttons (Edit & Delete)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { editingRefinement = refi },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = AwakeningGold, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = { weaponToDelete = refi },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = AwakeningCrimson, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Temel: $baseName (Yuva #${refi.position() + 1})",
                                    fontSize = 11.sp,
                                    color = AwakeningTextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Stats pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatChip(
                                        label = "Mt",
                                        bonus = refi.might(),
                                        total = refi.totalMight(),
                                        color = Color(0xFFEF5350)
                                    )
                                    StatChip(
                                        label = "Hit",
                                        bonus = refi.hit(),
                                        total = refi.totalHit(),
                                        color = AwakeningFalchionBlue
                                    )
                                    StatChip(
                                        label = "Crit",
                                        bonus = refi.crit(),
                                        total = refi.totalCrit(),
                                        color = AwakeningGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = AwakeningBorderSubtle.copy(alpha = 0.5f), thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Bottom row: Convoy Uses Stepper
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Konvoy Deposu Kullanımı:",
                                        fontSize = 12.sp,
                                        color = AwakeningTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )

                                    NumberStepper(
                                        value = convoyUses,
                                        min = 0,
                                        max = 999,
                                        step = 1,
                                        onValueChange = { newUses ->
                                            viewModel.setForgedWeaponUses(refi.position(), newUses)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create New Forged Weapon
    if (showCreateDialog && refiBlock != null) {
        ForgeWeaponDialog(
            refiBlock = refiBlock,
            isWest = chapter.isWest,
            existingRefinement = null,
            initialUses = 30,
            onDismiss = { showCreateDialog = false },
            onSave = { newRefi, uses ->
                viewModel.addOrUpdateForgedWeapon(newRefi, uses)
                showCreateDialog = false
            }
        )
    }

    // Dialog: Edit Existing Forged Weapon
    editingRefinement?.let { refi ->
        val uses = tranBlock?.inventoryRefi?.getOrNull(refi.position()) ?: 30
        ForgeWeaponDialog(
            refiBlock = refiBlock ?: return@let,
            isWest = chapter.isWest,
            existingRefinement = refi,
            initialUses = uses,
            onDismiss = { editingRefinement = null },
            onSave = { updatedRefi, updatedUses ->
                viewModel.addOrUpdateForgedWeapon(updatedRefi, updatedUses)
                editingRefinement = null
            }
        )
    }

    // Confirmation Dialog: Delete Forged Weapon
    weaponToDelete?.let { refi ->
        AlertDialog(
            onDismissRequest = { weaponToDelete = null },
            title = {
                Text(
                    text = "Silahı Sil: ${refi.name}",
                    fontWeight = FontWeight.Bold,
                    color = AwakeningCrimson,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "\"${refi.name}\" dövülmüş silahını kalıcı olarak silmek istediğinize emin misiniz?\n\nEğer bu silah herhangi bir birim tarafından kuşanılmışsa, save dosyasının bozulmaması için birimin envanterinden de otomatik olarak temizlenecektir.",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteForgedWeapon(refi)
                        weaponToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { weaponToDelete = null }
                ) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    bonus: Int,
    total: Int,
    color: Color
) {
    Surface(
        color = AwakeningCardSurface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AwakeningTextSecondary
            )
            Text(
                text = "$total",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (bonus > 0) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "(+$bonus)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AwakeningGoldBright
                )
            }
        }
    }
}
