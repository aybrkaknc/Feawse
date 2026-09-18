package com.example.feawse.ui.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.data.ClassDb
import com.example.feawse.data.ItemDb
import com.example.feawse.data.MiscDb
import com.example.feawse.data.SkillDb
import com.example.feawse.savefile.inventory.Refinement
import com.example.feawse.savefile.units.Stats
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.ui.components.PortraitAvatar
import com.example.feawse.ui.components.StatusBadge
import com.example.feawse.util.PortraitManager
import com.example.feawse.viewmodel.SaveFileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitEditScreen(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    uiState: com.example.feawse.viewmodel.SaveUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val modCount = uiState.modificationCount
    val portraitBitmap = remember(unit) {
        PortraitManager.getPortrait(context, unit)
    }
    val className = remember(unit, unit.rawBlock1.unitClass(), modCount) {
        ClassDb.getClassName(unit.rawBlock1.unitClass())
    }
    val isDead = remember(unit, modCount) { unit.isDead }

    var showSkillPickerSlot by remember { mutableStateOf<Int?>(null) }
    var showItemPickerSlot by remember { mutableStateOf<Int?>(null) }
    var confirmActionDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    val statNames = listOf("HP", "Güç (Str)", "Büyü (Mag)", "Beceri (Skl)", "Hız (Spd)", "Şans (Lck)", "Savunma (Def)", "Direnç (Res)")
    val currentGrowths = remember(unit, modCount) { unit.rawBlock1.growth() }
    val currentStats = remember(unit, modCount) { Stats.calcCurrentStats(unit, false) }
    val maxCaps = remember(unit, modCount) { Stats.calcMaxStats(unit, false) }

    val weaponNames = listOf("Kılıç (Sword)", "Mızrak (Lance)", "Balta (Axe)", "Yay (Bow)", "Büyü (Tome)", "Asa (Staff)")
    val weaponExp = remember(unit, modCount) { unit.rawBlock2.weaponExp }

    val compact = isCompact()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(unit.unitName(), color = AwakeningGoldBright, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = AwakeningGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AwakeningDarkBg)
            )
        },
        containerColor = AwakeningDarkBg,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = adaptivePadding()),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Card with Portrait and Class info
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AwakeningNavySurface,
                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (compact) 12.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PortraitAvatar(bitmap = portraitBitmap, size = if (compact) 50 else 60)
                        Spacer(modifier = Modifier.width(if (compact) 10.dp else 14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unit.unitName(),
                                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sınıf: $className",
                                color = AwakeningGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Reyting: ${Stats.rating(unit, false)} • Gizli: ${unit.rawFlags.hiddenLevel()}",
                                color = AwakeningTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isDead) {
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge(text = "ÖLÜ / EMEKLİ", backgroundColor = AwakeningCrimsonContainer, textColor = AwakeningCrimson)
                                    Button(
                                        onClick = { viewModel.reviveUnit(unit) },
                                        modifier = Modifier.height(26.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningEmerald, contentColor = androidx.compose.ui.graphics.Color.White),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Canlandır", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. High-Impact Quick Presets (Legal Max vs God Mode)
            item {
                CollapsibleCard(
                    title = "Hızlı Eylemler",
                    subtitle = "Yasal Tavan, 99 Hile, Yetenekler",
                    icon = Icons.Default.Bolt,
                    initialExpanded = false
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                confirmActionDialog = "${unit.unitName()} biriminin tüm istatistikleri yasal tavan değerlerine yükseltilecek. Onaylıyor musunuz?" to {
                                    viewModel.setUnitLegalMax(unit)
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
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yasal Tavan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                confirmActionDialog = "${unit.unitName()} birimine 99 Hile Modu uygulanacak (HP ve istatistikler 99 yapılacak). Onaylıyor musunuz?" to {
                                    viewModel.setUnitGodMode(unit)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningCrimson,
                                contentColor = AwakeningTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "99 Hile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            confirmActionDialog = "${unit.unitName()} için tüm yasal sınıf yetenekleri açılacak. Onaylıyor musunuz?" to {
                                viewModel.unlockAllUnitSkills(unit)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningFalchionBlue),
                        border = BorderStroke(1.dp, AwakeningFalchionBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tüm Yetenekleri Aç",
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Level, EXP & HP
            item {
                CollapsibleCard(
                    title = "Seviye & Can Değerleri",
                    subtitle = "Lv: ${unit.rawBlock1.level()} • EXP: ${unit.rawBlock1.exp()} • HP: ${unit.rawBlock1.currentHp()}",
                    icon = Icons.Default.Favorite,
                    initialExpanded = false
                ) {
                    StepperRow(
                        label = "Seviye (Level)",
                        value = unit.rawBlock1.level(),
                        min = 1,
                        max = 30,
                        onValueChange = { viewModel.setUnitLevel(unit, it, unit.rawBlock1.exp()) }
                    )

                    StepperRow(
                        label = "Deneyim (EXP)",
                        value = unit.rawBlock1.exp(),
                        min = 0,
                        max = 99,
                        onValueChange = { viewModel.setUnitLevel(unit, unit.rawBlock1.level(), it) }
                    )

                    StepperRow(
                        label = "Mevcut Can (HP)",
                        value = unit.rawBlock1.currentHp(),
                        min = 1,
                        max = 99,
                        onValueChange = { viewModel.setUnitCurrentHp(unit, it) }
                    )

                    StepperRow(
                        label = "Ekstra Hareket (Boots / Mov)",
                        value = unit.rawBlock1.movement(),
                        min = 0,
                        max = 15,
                        onValueChange = { viewModel.setUnitMovement(unit, it) }
                    )

                    StepperRow(
                        label = "Gizli Seviye (Hidden Level)",
                        value = unit.rawFlags.hiddenLevel(),
                        min = 0,
                        max = 255,
                        onValueChange = { viewModel.setUnitHiddenLevel(unit, it) }
                    )

                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    val armyNames = remember { MiscDb.getArmyNames() }
                    val currentArmy = remember(unit, modCount) { unit.rawFlags.army() }
                    var armyDropdownExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Ordu Bağlılığı (Army)", fontSize = 13.sp, color = AwakeningTextPrimary, fontWeight = FontWeight.Medium)
                            Text(
                                text = if (currentArmy in armyNames.indices) armyNames[currentArmy] else "Ordu #$currentArmy",
                                fontSize = 11.sp,
                                color = AwakeningGoldBright
                            )
                        }

                        Box {
                            OutlinedButton(
                                onClick = { armyDropdownExpanded = true },
                                modifier = Modifier.height(30.dp),
                                border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (currentArmy in armyNames.indices) armyNames[currentArmy] else "Ordu #$currentArmy",
                                    fontSize = 11.sp,
                                    color = AwakeningTextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = AwakeningGold)
                            }

                            DropdownMenu(
                                expanded = armyDropdownExpanded,
                                onDismissRequest = { armyDropdownExpanded = false },
                                modifier = Modifier.background(AwakeningNavySurface)
                            ) {
                                armyNames.forEachIndexed { index, name ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = name,
                                                color = if (index == currentArmy) AwakeningGoldBright else AwakeningTextPrimary,
                                                fontWeight = if (index == currentArmy) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            viewModel.setUnitArmy(unit, index)
                                            armyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val coords = remember(unit, modCount) { unit.rawBlock1.coordinates1() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Harita Konumu (X / Y)", fontSize = 13.sp, color = AwakeningTextPrimary, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            NumberStepper(
                                value = coords[0],
                                min = 0,
                                max = 255,
                                onValueChange = { viewModel.setUnitCoordinates(unit, it, coords[1]) }
                            )
                            NumberStepper(
                                value = coords[1],
                                min = 0,
                                max = 255,
                                onValueChange = { viewModel.setUnitCoordinates(unit, coords[0], it) }
                            )
                        }
                    }
                }
            }

            // 4. Detailed Stat Modifiers Grid
            item {
                CollapsibleCard(
                    title = if (compact) "İstatistikler" else "İstatistik Gelişimi (Stats)",
                    subtitle = "8 Temel Değer (Değer / Tavan)",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    initialExpanded = false
                ) {
                    statNames.forEachIndexed { index, name ->
                        val current = if (index < currentStats.size) currentStats[index] else 0
                        val cap = if (index < maxCaps.size) maxCaps[index] else 0
                        val growth = if (index < currentGrowths.size) currentGrowths[index] else 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(name, fontSize = 13.sp, color = AwakeningTextPrimary, fontWeight = FontWeight.Medium)
                                Text("Stat: $current / $cap", fontSize = 11.sp, color = AwakeningTextSecondary)
                            }

                            NumberStepper(
                                value = growth,
                                min = 0,
                                max = 99,
                                onValueChange = { viewModel.setUnitStatGrowth(unit, index, it) }
                            )
                        }
                        if (index < statNames.lastIndex) {
                            HorizontalDivider(color = AwakeningBorderSubtle, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            // 5. Weapon Rank EXP
            item {
                CollapsibleCard(
                    title = if (compact) "Silah Seviyeleri" else "Silah Seviyeleri (Weapon EXP)",
                    subtitle = "6 Silah Türü (EXP ve Rütbeler)",
                    icon = Icons.Default.SportsMartialArts,
                    initialExpanded = false
                ) {
                    weaponNames.forEachIndexed { index, weapon ->
                        val exp = if (index < weaponExp.size) weaponExp[index] else 0
                        val rankLetter = when {
                            exp >= 90 -> "A / S (Maks)"
                            exp >= 70 -> "B"
                            exp >= 40 -> "C"
                            exp >= 20 -> "D"
                            exp > 0 -> "E"
                            else -> "Yok (-)"
                        }
                        val rankShort = when {
                            exp >= 90 -> "A/S"
                            exp >= 70 -> "B"
                            exp >= 40 -> "C"
                            exp >= 20 -> "D"
                            exp > 0 -> "E"
                            else -> "-"
                        }
                        val rankSubtitle = if (compact) "Rütbe: $rankShort • $exp EXP" else "Rütbe: $rankLetter (EXP: $exp)"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(weapon, fontSize = 13.sp, color = AwakeningTextPrimary)
                                Text(
                                    text = rankSubtitle,
                                    fontSize = 11.sp,
                                    color = AwakeningTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilledTonalButton(
                                    onClick = { viewModel.setUnitWeaponRankExp(unit, index, 0) },
                                    modifier = Modifier
                                        .height(26.dp)
                                        .defaultMinSize(minWidth = 34.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("0", fontSize = 10.sp, textAlign = TextAlign.Center)
                                }
                                FilledTonalButton(
                                    onClick = { viewModel.setUnitWeaponRankExp(unit, index, 90) },
                                    modifier = Modifier
                                        .height(26.dp)
                                        .defaultMinSize(minWidth = 46.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = AwakeningGoldContainer,
                                        contentColor = AwakeningGoldBright
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text("A (90)", fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        if (index < weaponNames.lastIndex) {
                            HorizontalDivider(color = AwakeningCardBorder.copy(alpha = 0.3f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                confirmActionDialog = "${unit.unitName()} biriminin tüm silah seviyeleri sıfırlanacak (0). Onaylıyor musunuz?" to {
                                    for (i in 0..5) viewModel.setUnitWeaponRankExp(unit, i, 0)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Tümünü Sıfırla",
                                fontSize = 11.sp,
                                color = AwakeningTextSecondary,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = {
                                confirmActionDialog = "${unit.unitName()} biriminin tüm silah seviyeleri A rütbesine (90 EXP) maksılancak. Onaylıyor musunuz?" to {
                                    for (i in 0..5) viewModel.setUnitWeaponRankExp(unit, i, 90)
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
                            Text(
                                text = "Tümünü Maksla",
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

            // 6. Savaş Kayıtları & Geçici İksirler (Tonics)
            item {
                UnitBattleAndTonicsSection(
                    unit = unit,
                    viewModel = viewModel,
                    modCount = modCount
                )
            }

            // 7. Gelişmiş Nitelik & Savaş Bayrakları (Flags)
            item {
                UnitFlagsSection(
                    unit = unit,
                    viewModel = viewModel,
                    modCount = modCount
                )
            }

            // 8. Equipped Skills (5 slots)
            item {
                CollapsibleCard(
                    title = "Kuşanılan Yetenekler",
                    subtitle = "5 Yuva (Düzenlemek için dokunun)",
                    icon = Icons.Default.Stars,
                    initialExpanded = false
                ) {
                    val currentSkills = unit.rawBlock2.currentSkills
                    for (slot in 0..4) {
                        val skillId = if (slot < currentSkills.size) currentSkills[slot] else 0
                        val skillName = if (skillId > 0 && skillId < SkillDb.getSkillNames().size) {
                            SkillDb.getSkillNames()[skillId]
                        } else "Boş Yuva"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { showSkillPickerSlot = slot },
                            colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = if (skillId > 0) AwakeningGold else AwakeningTextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Yuva ${slot + 1}: $skillName",
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (skillId > 0) AwakeningTextPrimary else AwakeningTextTertiary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = AwakeningGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 7. Equipped Items (5 slots)
            item {
                CollapsibleCard(
                    title = "Kuşanılan Eşyalar (Envanter)",
                    subtitle = "5 Slot (Eşyaları değiştirmek için dokunun)",
                    icon = Icons.Default.Shield,
                    initialExpanded = false
                ) {
                    val items = unit.rawInventory?.items ?: emptyList()
                    val itemNames = ItemDb.getItemNamesRegular()
                    val refiList = uiState.chapterFile?.blockRefi?.refiList ?: emptyList()
                    for (slot in 0..4) {
                        val item = if (slot < items.size) items[slot] else null
                        val itemId = item?.itemId() ?: 0
                        val uses = item?.uses() ?: 0

                        val isForged = itemId > ItemDb.MOD_MAX_ID
                        val forgedRefi = if (isForged) {
                            val pos = itemId - ItemDb.MOD_MAX_ID - 1
                            refiList.find { it.position() == pos }
                        } else null

                        val itemName = when {
                            forgedRefi != null -> "⚔️ ${forgedRefi.name} (+${forgedRefi.might()}Mt, +${forgedRefi.hit()}Hit)"
                            isForged -> "⚔️ Dövülmüş Silah #${itemId - ItemDb.MOD_MAX_ID}"
                            itemId in 1 until itemNames.size -> itemNames[itemId]
                            else -> "Boş Slot"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { showItemPickerSlot = slot },
                            colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isForged) Icons.Default.Star else Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isForged) AwakeningGold else if (itemId > 0) AwakeningFalchionBlue else AwakeningTextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Slot ${slot + 1}: $itemName",
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isForged) AwakeningGoldBright else if (itemId > 0) AwakeningTextPrimary else AwakeningTextTertiary,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (itemId > 0) {
                                            Text(
                                                text = if (forgedRefi != null) "Kullanım: $uses (Temel: ${ItemDb.getItemName(forgedRefi.weaponId())})" else "Kullanım: $uses",
                                                color = AwakeningTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                Icon(Icons.Default.Edit, contentDescription = "Değiştir", tint = AwakeningGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 7. Support Relationships Editor
            item {
                SupportEditorSection(
                    unit = unit,
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            // 8. Child & Parent Block Editor
            item {
                ChildEditorSection(
                    unit = unit,
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            // 9. Logbook / Avatar & Einherjar Card Editor
            item {
                LogbookEditorSection(
                    unit = unit,
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            // 10. Unit Management (Duplicate, Move, Export, Delete)
            item {
                UnitManagementSection(
                    unit = unit,
                    viewModel = viewModel,
                    uiState = uiState,
                    onBack = onBack
                )
            }
        }
    }

    // Skill Picker Dialog
    showSkillPickerSlot?.let { slot ->
        SkillPickerDialog(
            onDismiss = { showSkillPickerSlot = null },
            onSkillSelected = { skillId ->
                viewModel.setUnitEquippedSkill(unit, slot, skillId)
                showSkillPickerSlot = null
            }
        )
    }

    // Item Picker Dialog
    showItemPickerSlot?.let { slot ->
        val refiList = uiState.chapterFile?.blockRefi?.refiList ?: emptyList()
        ItemPickerDialog(
            refiList = refiList,
            onDismiss = { showItemPickerSlot = null },
            onItemSelected = { itemId, uses ->
                viewModel.setUnitEquippedItem(unit, slot, itemId, uses)
                showItemPickerSlot = null
            }
        )
    }

    if (confirmActionDialog != null) {
        val (message, action) = confirmActionDialog!!
        AlertDialog(
            onDismissRequest = { confirmActionDialog = null },
            title = { Text("İşlemi Onaylayın", color = AwakeningGoldBright, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
            text = { Text(message, color = AwakeningTextPrimary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        action()
                        confirmActionDialog = null
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
                    onClick = { confirmActionDialog = null },
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

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = AwakeningTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f, fill = false)
        )
        NumberStepper(
            value = value,
            min = min,
            max = max,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun SkillPickerDialog(
    onDismiss: () -> Unit,
    onSkillSelected: (Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val allSkillNames = remember { SkillDb.getSkillNames() }
    val filteredSkills = remember(search) {
        val list = mutableListOf<Pair<Int, String>>()
        for (idx in allSkillNames.indices) {
            val name = allSkillNames[idx]
            if (search.isBlank() || name.contains(search, ignoreCase = true)) {
                list.add(Pair(idx, name))
            }
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yetenek Seç (Skill Picker)", color = AwakeningGoldBright) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AwakeningSearchBar(
                    query = search,
                    onQueryChange = { search = it },
                    placeholder = "Yetenek ara (örn. Galeforce, Aether)..."
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(filteredSkills.size) { i ->
                        val itemPair = filteredSkills[i]
                        val id = itemPair.first
                        val name = itemPair.second
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onSkillSelected(id) },
                            colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(name, fontWeight = FontWeight.Bold, color = AwakeningTextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSkillSelected(0) }) {
                Text("Boşalt (Kaldır)", color = AwakeningCrimson)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = AwakeningTextSecondary)
            }
        },
        containerColor = AwakeningNavySurface
    )
}

@Composable
fun ItemPickerDialog(
    refiList: List<Refinement> = emptyList(),
    onDismiss: () -> Unit,
    onItemSelected: (Int, Int) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Standart Eşyalar, 1: Dövülmüş Silahlar

    val allItemNames = remember { ItemDb.getItemNamesRegular() }
    val filteredStandard = remember(search) {
        val list = mutableListOf<Pair<Int, String>>()
        for (idx in allItemNames.indices) {
            val name = allItemNames[idx]
            if (search.isBlank() || name.contains(search, ignoreCase = true)) {
                list.add(Pair(idx, name))
            }
        }
        list
    }

    val filteredForged = remember(search, refiList) {
        refiList.filter { refi ->
            if (search.isBlank()) true
            else {
                refi.name.contains(search, ignoreCase = true) ||
                        ItemDb.getItemName(refi.weaponId()).contains(search, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Eşya / Silah Kuşan", color = AwakeningGoldBright, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Standart Eşyalar", fontSize = 11.sp) },
                        shape = RoundedCornerShape(6.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AwakeningGold,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = AwakeningCardSurface,
                            labelColor = AwakeningTextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Dövülmüş Silahlar (${refiList.size})", fontSize = 11.sp) },
                        shape = RoundedCornerShape(6.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AwakeningGold,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = AwakeningCardSurface,
                            labelColor = AwakeningTextSecondary
                        )
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AwakeningSearchBar(
                    query = search,
                    onQueryChange = { search = it },
                    placeholder = if (selectedTab == 0) "Standart eşya ara (örn. Falchion, Elixir)..." else "Dövülmüş silah ara..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Standard Items List
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(filteredStandard.size) { i ->
                            val itemPair = filteredStandard[i]
                            val id = itemPair.first
                            val name = itemPair.second
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        val uses = if (id < ItemDb.getItemCountVanilla()) ItemDb.getItemUses(id) else 30
                                        onItemSelected(id, uses)
                                    },
                                colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontWeight = FontWeight.Bold, color = AwakeningTextPrimary, fontSize = 13.sp)
                                    if (id < ItemDb.getItemCountVanilla()) {
                                        Text("${ItemDb.getItemUses(id)} Kullanım", color = AwakeningGold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Forged Weapons List
                    if (refiList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Kayıtlı dövülmüş silah yok.\nKonvoy ekranındaki Dövülmüş Silah Atölyesinden yeni silah dövebilirsiniz.",
                                color = AwakeningTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(filteredForged.size) { i ->
                                val refi = filteredForged[i]
                                val forgedId = ItemDb.MOD_MAX_ID + 1 + refi.position()
                                val defaultUses = ItemDb.getItemUses(refi.weaponId()).let { if (it <= 0) 30 else it }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            onItemSelected(forgedId, defaultUses)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = AwakeningCardSurface),
                                    border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(refi.name, fontWeight = FontWeight.Bold, color = AwakeningGoldBright, fontSize = 13.sp)
                                            }
                                            Text(
                                                text = "Temel: ${ItemDb.getItemName(refi.weaponId())} | +${refi.might()}Mt, +${refi.hit()}Hit, +${refi.crit()}Crit",
                                                fontSize = 11.sp,
                                                color = AwakeningTextSecondary
                                            )
                                        }

                                        Text("$defaultUses Kul.", color = AwakeningGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onItemSelected(0, 0) }) {
                Text("Boşalt (Kaldır)", color = AwakeningCrimson)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = AwakeningTextSecondary)
            }
        },
        containerColor = AwakeningNavySurface
    )
}
