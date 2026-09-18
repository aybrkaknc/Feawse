package com.example.feawse.ui.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.ui.util.isExpanded
import com.example.feawse.data.ClassDb
import com.example.feawse.data.UnitDb
import com.example.feawse.savefile.units.Stats
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.PortraitAvatar
import com.example.feawse.ui.components.StatusBadge
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitListScreen(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    onSelectUnit: (SaveUnit) -> kotlin.Unit,
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedGroupFilter by remember { mutableIntStateOf(0) } // 0: All Playable, 1: G0, 2: G3, 3: Dead (G4), 4: Enemies (G1), 5: NPCs (G2)

    val chapter = uiState.chapterFile
    if (chapter == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Lütfen önce Ana Ekrandan bir kayıt dosyası açın.",
                color = AwakeningTextSecondary
            )
        }
        return
    }

    val unitListContainer = chapter.blockUnit?.unitList ?: emptyList()

    val allUnits: List<SaveUnit> = remember(chapter, selectedGroupFilter, uiState.modificationCount) {
        when (selectedGroupFilter) {
            0 -> {
                val g0 = if (unitListContainer.isNotEmpty()) unitListContainer[0] else emptyList()
                val g3 = if (unitListContainer.size > 3) unitListContainer[3] else emptyList()
                g0 + g3
            }
            1 -> if (unitListContainer.isNotEmpty()) unitListContainer[0] else emptyList()
            2 -> if (unitListContainer.size > 3) unitListContainer[3] else emptyList()
            3 -> if (unitListContainer.size > 4) unitListContainer[4] else emptyList()
            4 -> if (unitListContainer.size > 1) unitListContainer[1] else emptyList()
            5 -> if (unitListContainer.size > 2) unitListContainer[2] else emptyList()
            else -> emptyList()
        }
    }

    val filteredUnits = remember(allUnits, searchQuery) {
        if (searchQuery.isBlank()) allUnits
        else {
            val q = searchQuery.trim().lowercase()
            allUnits.filter { u ->
                val name = u.unitName().lowercase()
                val cls = ClassDb.getClassName(u.rawBlock1.unitClass()).lowercase()
                name.contains(q) || cls.contains(q)
            }
        }
    }

    val expanded = isExpanded()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row (full width, no action buttons)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedGroupFilter == 0,
                    onClick = { selectedGroupFilter = 0 },
                    label = { Text("Tüm Oynanabilir", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 0,
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
                    selected = selectedGroupFilter == 1,
                    onClick = { selectedGroupFilter = 1 },
                    label = { Text("Çobanlar (Grup 0)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 1,
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
                    selected = selectedGroupFilter == 2,
                    onClick = { selectedGroupFilter = 2 },
                    label = { Text("Aktif Kadro (Grup 3)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 2,
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
                    selected = selectedGroupFilter == 3,
                    onClick = { selectedGroupFilter = 3 },
                    label = { Text("Kayıplar (Grup 4)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 3,
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
                    selected = selectedGroupFilter == 4,
                    onClick = { selectedGroupFilter = 4 },
                    label = { Text("Düşmanlar (Grup 1)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 4,
                        borderColor = AwakeningBorderSubtle,
                        selectedBorderColor = AwakeningCrimson,
                        borderWidth = 1.dp
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AwakeningCrimson,
                        selectedLabelColor = AwakeningTextPrimary,
                        containerColor = AwakeningNavySurface,
                        labelColor = AwakeningTextSecondary
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedGroupFilter == 5,
                    onClick = { selectedGroupFilter = 5 },
                    label = { Text("NPC'ler (Grup 2)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGroupFilter == 5,
                        borderColor = AwakeningBorderSubtle,
                        selectedBorderColor = AwakeningSuccess,
                        borderWidth = 1.dp
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AwakeningSuccess,
                        selectedLabelColor = AwakeningTextPrimary,
                        containerColor = AwakeningNavySurface,
                        labelColor = AwakeningTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Toplam ${filteredUnits.size} birim listeleniyor",
            style = MaterialTheme.typography.labelSmall,
            color = AwakeningTextTertiary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Responsive Unit List / Grid
        if (expanded) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUnits) { unit ->
                    UnitCard(
                        unit = unit,
                        onClick = { onSelectUnit(unit) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUnits) { unit ->
                    UnitCard(
                        unit = unit,
                        onClick = { onSelectUnit(unit) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitCard(
    unit: SaveUnit,
    onClick: () -> kotlin.Unit
) {
    val context = LocalContext.current
    val compact = isCompact()
    val portraitBitmap = remember(unit) {
        PortraitManager.getPortrait(context, unit)
    }
    val className = remember(unit) {
        ClassDb.getClassName(unit.rawBlock1.unitClass())
    }
    val rating = remember(unit) {
        Stats.rating(unit, false)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = AwakeningNavySurface,
        border = BorderStroke(1.dp, AwakeningBorderSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PortraitAvatar(bitmap = portraitBitmap, size = if (compact) 44 else 50)
            Spacer(modifier = Modifier.width(if (compact) 8.dp else 10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = unit.unitName(),
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        fontSize = if (compact) 14.sp else 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lv ${unit.rawBlock1.level()}",
                        color = AwakeningGoldBright,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = className,
                        color = AwakeningTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "• HP ${unit.rawBlock1.currentHp()}",
                        color = AwakeningTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    text = if (compact) "R: $rating" else "Rating $rating",
                    textColor = AwakeningGold
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(16.dp))
        }
    }
}
