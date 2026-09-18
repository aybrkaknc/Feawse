package com.example.feawse.ui.units

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
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.viewmodel.SaveFileViewModel

@Composable
fun UnitFlagsSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Trait Flags, 1: Battle Flags, 2: Rally Buffs, 3: AI
    var searchQuery by remember { mutableStateOf("") }

    val traitFlags = remember { MiscDb.traitFlags }
    val battleFlags = remember { MiscDb.battleFlags }
    val buffSkills = remember { MiscDb.buffSkills }

    CollapsibleCard(
        title = "Gelişmiş Nitelik & Savaş Bayrakları",
        subtitle = "32 Nitelik • 32 Savaş • 11 Ralli • 4 AI Parametresi",
        icon = Icons.Default.Flag,
        initialExpanded = false,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Tab Selector (4 Tabs)
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
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf("Nitelik (32)", "Savaş (32)", "Ralli (11)", "Yapay Zeka (AI)").forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AwakeningGoldContainer else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AwakeningGoldBright else AwakeningTextSecondary,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (selectedTab != 3) {
                // Quick Search Field for Flags
                AwakeningSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Bayrak veya özellik ara..."
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: Trait Flags (32)
                    val filtered = traitFlags.mapIndexed { index, name -> index to name }
                        .filter { (_, name) -> searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true) }

                    filtered.forEach { (index, name) ->
                        val isSet = remember(unit, index, modCount) { unit.rawFlags.hasTraitFlag(index) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.setUnitTraitFlag(unit, index, !isSet) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "$index: $name",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSet) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSet) AwakeningTextPrimary else AwakeningTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isSet,
                                onCheckedChange = { viewModel.setUnitTraitFlag(unit, index, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AwakeningDarkBg,
                                    checkedTrackColor = AwakeningGold,
                                    uncheckedThumbColor = AwakeningTextSecondary,
                                    uncheckedTrackColor = AwakeningCardSurface
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (index != filtered.lastOrNull()?.first) {
                            HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }

                1 -> {
                    // TAB 1: Battle Flags (32)
                    val filtered = battleFlags.mapIndexed { index, name -> index to name }
                        .filter { (_, name) -> searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true) }

                    filtered.forEach { (index, name) ->
                        val isSet = remember(unit, index, modCount) { unit.rawFlags.hasBattleFlag(index) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.setUnitBattleFlag(unit, index, !isSet) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "$index: $name",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSet) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSet) AwakeningTextPrimary else AwakeningTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isSet,
                                onCheckedChange = { viewModel.setUnitBattleFlag(unit, index, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AwakeningDarkBg,
                                    checkedTrackColor = AwakeningGold,
                                    uncheckedThumbColor = AwakeningTextSecondary,
                                    uncheckedTrackColor = AwakeningCardSurface
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (index != filtered.lastOrNull()?.first) {
                            HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }

                2 -> {
                    // TAB 2: Rally / Skill Buffs (11)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setAllUnitSkillBuffs(unit, true) },
                            modifier = Modifier.weight(1f).height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Tüm Rallileri Aç", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { viewModel.setAllUnitSkillBuffs(unit, false) },
                            modifier = Modifier.weight(1f).height(30.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Tümünü Kapat", fontSize = 11.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                        }
                    }

                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                    val filtered = buffSkills.mapIndexed { index, name -> index to name }
                        .filter { (_, name) -> searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true) }

                    filtered.forEach { (index, name) ->
                        val isSet = remember(unit, index, modCount) { unit.rawFlags.hasSkillFlag(index) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { viewModel.setUnitSkillBuff(unit, index, !isSet) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = name,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSet) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSet) AwakeningGoldBright else AwakeningTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isSet,
                                onCheckedChange = { viewModel.setUnitSkillBuff(unit, index, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AwakeningDarkBg,
                                    checkedTrackColor = AwakeningGold,
                                    uncheckedThumbColor = AwakeningTextSecondary,
                                    uncheckedTrackColor = AwakeningCardSurface
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (index != filtered.lastOrNull()?.first) {
                            HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }

                3 -> {
                    // TAB 3: AI Parameters (Yapay Zeka - Action, Mission, Attack, Move)
                    Surface(
                        color = AwakeningCardSurface,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Düşman (Kırmızı) ve Tarafsız (Yeşil) NPC birimlerinin haritadaki karar mantığını yönetir. Oyuncu birimlerinde bu alanlar normalde 0'dır.",
                            fontSize = 11.sp,
                            color = AwakeningTextSecondary,
                            modifier = Modifier.padding(8.dp),
                            lineHeight = 15.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetUnitAi(unit) },
                            modifier = Modifier.height(28.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = AwakeningTextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Sıfırla (0)", fontSize = 10.5.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                        }
                    }

                    val aiCategories = listOf(
                        Triple(0, "Eylem (Action AI)", MiscDb.getAiAction(unit.rawBlockEnd.aiType(0))),
                        Triple(1, "Görev (Mission AI)", MiscDb.getAiMission(unit.rawBlockEnd.aiType(1))),
                        Triple(2, "Saldırı (Attack AI)", MiscDb.getAiAttack(unit.rawBlockEnd.aiType(2))),
                        Triple(3, "Hareket (Move AI)", MiscDb.getAiMove(unit.rawBlockEnd.aiType(3)))
                    )

                    aiCategories.forEach { (slot, categoryTitle, aiName) ->
                        val currentType = remember(unit, slot, modCount) { unit.rawBlockEnd.aiType(slot) }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(categoryTitle, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = AwakeningGoldBright)
                                        Text(
                                            text = "Tanım: $aiName",
                                            fontSize = 10.5.sp,
                                            color = AwakeningTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    NumberStepper(
                                        value = currentType,
                                        min = 0,
                                        max = 255,
                                        onValueChange = { viewModel.setUnitAiType(unit, slot, it) }
                                    )
                                }

                                HorizontalDivider(color = AwakeningBorderSubtle.copy(alpha = 0.3f), thickness = 0.5.dp)

                                Text("Parametreler (Param 1 - 4)", fontSize = 10.5.sp, color = AwakeningTextTertiary, fontWeight = FontWeight.Medium)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (paramIdx in 0..3) {
                                        val paramVal = remember(unit, slot, paramIdx, modCount) {
                                            unit.rawBlockEnd.aiParam(slot, paramIdx)
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("P${paramIdx + 1}", fontSize = 10.sp, color = AwakeningTextSecondary)
                                            NumberStepper(
                                                value = paramVal,
                                                min = 0,
                                                max = 65535,
                                                onValueChange = { viewModel.setUnitAiParam(unit, slot, paramIdx, it) },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
