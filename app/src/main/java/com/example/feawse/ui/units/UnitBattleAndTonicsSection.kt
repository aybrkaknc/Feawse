package com.example.feawse.ui.units

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.NumberStepper
import com.example.feawse.viewmodel.SaveFileViewModel

@Composable
fun UnitBattleAndTonicsSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    modCount: Long,
    modifier: Modifier = Modifier
) {
    val battles = remember(unit, modCount) { unit.rawBlockEnd.battleCount() }
    val victories = remember(unit, modCount) { unit.rawBlockEnd.victoryCount() }
    val pureWater = remember(unit, modCount) { unit.rawFlags.resBuff() }
    val isDead = remember(unit, modCount) { unit.isDead }

    val tonicNames = listOf(
        "HP (+5 Can)",
        "Güç (+2 Str)",
        "Büyü (+2 Mag)",
        "Beceri (+2 Skl)",
        "Hız (+2 Spd)",
        "Şans (+2 Lck)",
        "Savunma (+2 Def)",
        "Direnç (+2 Res)"
    )

    CollapsibleCard(
        title = "Savaş Kayıtları & Geçici İksirler (Tonics)",
        subtitle = "Savaş: $battles • Zafer: $victories • Saf Su: $pureWater",
        icon = Icons.Default.EmojiEvents,
        initialExpanded = false,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Canlılık / Emeklilik Durumu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Hayatta Kalma Durumu",
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isDead) "Birim düştü veya emekli edildi (Klasik Mod)." else "Birim aktif ve hayatta.",
                        color = if (isDead) AwakeningCrimson else AwakeningEmerald,
                        fontSize = 11.sp
                    )
                }

                if (isDead) {
                    Button(
                        onClick = { viewModel.reviveUnit(unit) },
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningEmerald, contentColor = androidx.compose.ui.graphics.Color.White),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Canlandır", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.killUnit(unit) },
                        modifier = Modifier.height(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningCrimson),
                        border = BorderStroke(1.dp, AwakeningCrimson.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text("Emekli Et", fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                    }
                }
            }

            HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

            // Savaş ve Zafer Sayıcıları
            Text(
                text = "Karakter Savaş Kayıtları",
                fontWeight = FontWeight.SemiBold,
                color = AwakeningGold,
                fontSize = 12.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Katıldığı Savaş Sayısı", fontSize = 13.sp, color = AwakeningTextPrimary)
                    Text("Toplam çatışma sayısı", fontSize = 10.5.sp, color = AwakeningTextSecondary)
                }
                NumberStepper(
                    value = battles,
                    min = 0,
                    max = 65535,
                    onValueChange = { viewModel.setUnitBattles(unit, it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kazanılan Zafer Sayısı", fontSize = 13.sp, color = AwakeningTextPrimary)
                    Text("Birim tarafından alt edilen düşman", fontSize = 10.5.sp, color = AwakeningTextSecondary)
                }
                NumberStepper(
                    value = victories,
                    min = 0,
                    max = 65535,
                    onValueChange = { viewModel.setUnitVictories(unit, it) }
                )
            }

            // Hızlı presets (+10, +50, Sıfırla)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.setUnitBattles(unit, battles + 10)
                        viewModel.setUnitVictories(unit, victories + 10)
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+10 Her İkisi", fontSize = 10.sp, maxLines = 1, softWrap = false)
                }
                FilledTonalButton(
                    onClick = {
                        viewModel.setUnitBattles(unit, battles + 50)
                        viewModel.setUnitVictories(unit, victories + 50)
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+50 Her İkisi", fontSize = 10.sp, maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = {
                        viewModel.setUnitBattles(unit, 0)
                        viewModel.setUnitVictories(unit, 0)
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sıfırla", fontSize = 10.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                }
            }

            HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

            // Geçici İksirler (Tonikler - 8 Adet)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Geçici Stat İksirleri (Tonics)",
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningGold,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = "Tek haritalık geçici stat artışları (+5 HP, +2 Diğerleri)",
                        fontSize = 10.5.sp,
                        color = AwakeningTextSecondary
                    )
                }
            }

            // 2 sütunlu tonik ızgarası
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 2) {
                            val tonicIndex = row * 2 + col
                            val isActive = remember(unit, tonicIndex, modCount) {
                                unit.rawFlags.hasTonicFlag(tonicIndex)
                            }
                            val name = tonicNames[tonicIndex]

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.setUnitTonic(unit, tonicIndex, !isActive)
                                    },
                                color = if (isActive) AwakeningGoldContainer else AwakeningCardSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) AwakeningGold else AwakeningBorderSubtle
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isActive) AwakeningGoldBright else AwakeningTextSecondary,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isActive) AwakeningGold else AwakeningTextTertiary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Batch Buttons for Tonics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { viewModel.setAllUnitTonics(unit, true) },
                    modifier = Modifier.weight(1f).height(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Tüm İksirleri İçir", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = { viewModel.setAllUnitTonics(unit, false) },
                    modifier = Modifier.weight(1f).height(30.dp),
                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("İksirleri Temizle", fontSize = 11.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                }
            }

            HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

            // Saf Su (Pure Water - RES Boost)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saf Su (Pure Water)", fontSize = 13.sp, color = AwakeningTextPrimary, fontWeight = FontWeight.Medium)
                    Text("Geçici Direnç (Res) artışı (0-15)", fontSize = 10.5.sp, color = AwakeningTextSecondary)
                }
                NumberStepper(
                    value = pureWater,
                    min = 0,
                    max = 15,
                    onValueChange = { viewModel.setUnitPureWater(unit, it) }
                )
            }

            // Kışla Güçlendirmeleri (Barracks Buffs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.setAllUnitBarrackBuffs(unit, true) },
                    modifier = Modifier.weight(1f).height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+4 Kışla Buffları", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                }
                OutlinedButton(
                    onClick = { viewModel.setAllUnitBarrackBuffs(unit, false) },
                    modifier = Modifier.weight(1f).height(30.dp),
                    border = BorderStroke(0.5.dp, AwakeningBorderSubtle),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Kışlayı Sıfırla", fontSize = 10.5.sp, color = AwakeningTextSecondary, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
