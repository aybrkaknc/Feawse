package com.example.feawse.ui.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.data.ChapterDb
import com.example.feawse.savefile.map.RawMap
import com.example.feawse.theme.*
import com.example.feawse.ui.components.AwakeningSearchBar
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

private data class MapItem(
    val index: Int,
    val map: RawMap,
    val name: String,
    val isParalogue: Boolean,
    val state: Int
)

@Composable
fun ProgressScreen(
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

    val userBlock = chapter.blockUser
    val gmapBlock = chapter.blockGmap
    val modCount = uiState.modificationCount

    var goldText by remember(userBlock.money(), modCount) { mutableStateOf(userBlock.money().toString()) }
    var renownText by remember(userBlock.renown(), modCount) { mutableStateOf(userBlock.renown().toString()) }

    val compact = isCompact()
    val difficulties = remember(compact) {
        listOf("Normal", "Hard", "Lunatic")
    }
    val currentDiff = userBlock.difficulty().coerceIn(0, 2)
    val isCasual = userBlock.gameModeFlag(2)
    val isLunaticPlus = userBlock.isLunaticPlus()

    // Map section state
    var isMapSectionExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedMapFilter by rememberSaveable { mutableIntStateOf(0) } // 0: Tümü, 1: Ana Hikaye, 2: Paralogue
    var mapSearchQuery by rememberSaveable { mutableStateOf("") }
    var extraChaptersRevealed by rememberSaveable { mutableIntStateOf(0) }
    var confirmActionDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    val allMaps = remember(gmapBlock, modCount) {
        gmapBlock?.maps ?: emptyList()
    }
    val overWorldNames = remember { ChapterDb.getOverWorldNames() }

    // En son açık veya tamamlanmış olan bölümün indeksini tespit et
    val defaultCutoffIndex = remember(allMaps, modCount) {
        val lastActive = allMaps.indexOfLast { it.lockState() != 0 }
        if (lastActive >= 0) lastActive else 2
    }

    val filteredMaps = remember(allMaps, selectedMapFilter, mapSearchQuery, modCount) {
        allMaps.mapIndexed { index, map ->
            val name = if (index < overWorldNames.size) overWorldNames[index] else "Bölüm #${index + 1}"
            val isParalogue = name.contains("Paralogue", ignoreCase = true)
            MapItem(index, map, name, isParalogue, map.lockState())
        }.filter { item ->
            val matchesFilter = when (selectedMapFilter) {
                1 -> !item.isParalogue
                2 -> item.isParalogue
                else -> true
            }
            val matchesSearch = mapSearchQuery.isBlank() || item.name.contains(mapSearchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    // Parça parça kademeli genişletme: Sadece açık olanlar + kullanıcının istediği ekstra bölüm sayısı
    val maxVisibleIndex = defaultCutoffIndex + extraChaptersRevealed
    val visibleMaps = remember(filteredMaps, extraChaptersRevealed, defaultCutoffIndex, mapSearchQuery) {
        if (mapSearchQuery.isNotBlank()) {
            filteredMaps
        } else {
            filteredMaps.filter { it.index <= maxVisibleIndex || it.state != 0 }
        }
    }

    val hiddenCount = filteredMaps.size - visibleMaps.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding()),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Gold & Economy Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (compact) "Altın (Gold)" else "Altın (Gold / Para)",
                            fontWeight = FontWeight.SemiBold,
                            color = AwakeningTextPrimary,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Tavan: 999,999",
                            style = MaterialTheme.typography.labelSmall,
                            color = AwakeningTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = goldText,
                        onValueChange = { input ->
                            goldText = input
                            val num = input.toIntOrNull()
                            if (num != null) viewModel.setMoney(num.coerceIn(0, 999999))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    confirmActionDialog = "Altın miktarı maksimuma (999,999) ayarlanacak. Onaylıyor musunuz?" to {
                                        viewModel.setMoney(999999)
                                        goldText = "999999"
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Maks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AwakeningGold)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AwakeningTextPrimary,
                            unfocusedTextColor = AwakeningTextPrimary,
                            focusedBorderColor = AwakeningGold,
                            unfocusedBorderColor = AwakeningBorderSubtle,
                            focusedContainerColor = AwakeningCardSurface,
                            unfocusedContainerColor = AwakeningCardSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Quick Gold Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(10_000, 50_000, 100_000).forEach { addVal ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val current = userBlock.money()
                                        val newVal = (current + addVal).coerceIn(0, 999999)
                                        viewModel.setMoney(newVal)
                                        goldText = newVal.toString()
                                    },
                                color = AwakeningCardSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                            ) {
                                Text(
                                    text = "+${addVal / 1000}k",
                                    fontSize = 10.5.sp,
                                    color = AwakeningGold,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    confirmActionDialog = "Altın sıfırlanacak (0). Onaylıyor musunuz?" to {
                                        viewModel.setMoney(0)
                                        goldText = "0"
                                    }
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = "Sıfırla",
                                fontSize = 10.5.sp,
                                color = AwakeningTextSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Renown Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (compact) "Şöhret (Renown)" else "Şöhret Puanı (Renown)",
                            fontWeight = FontWeight.SemiBold,
                            color = AwakeningTextPrimary,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Tavan: 99,999",
                            style = MaterialTheme.typography.labelSmall,
                            color = AwakeningTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = renownText,
                        onValueChange = { input ->
                            renownText = input
                            val num = input.toIntOrNull()
                            if (num != null) viewModel.setRenown(num.coerceIn(0, 99999))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            TextButton(
                                onClick = {
                                    confirmActionDialog = "Şöhret puanı maksimuma (99,999) ayarlanacak. Onaylıyor musunuz?" to {
                                        viewModel.setRenown(99999)
                                        renownText = "99999"
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Maks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AwakeningGold)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AwakeningTextPrimary,
                            unfocusedTextColor = AwakeningTextPrimary,
                            focusedBorderColor = AwakeningGold,
                            unfocusedBorderColor = AwakeningBorderSubtle,
                            focusedContainerColor = AwakeningCardSurface,
                            unfocusedContainerColor = AwakeningCardSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Quick Renown Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1_000, 5_000, 10_000).forEach { addVal ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val current = userBlock.renown()
                                        val newVal = (current + addVal).coerceIn(0, 99999)
                                        viewModel.setRenown(newVal)
                                        renownText = newVal.toString()
                                    },
                                color = AwakeningCardSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                            ) {
                                Text(
                                    text = "+${addVal / 1000}k",
                                    fontSize = 10.5.sp,
                                    color = AwakeningGold,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    confirmActionDialog = "Şöhret puanı sıfırlanacak (0). Onaylıyor musunuz?" to {
                                        viewModel.setRenown(0)
                                        renownText = "0"
                                    }
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = "Sıfırla",
                                fontSize = 10.5.sp,
                                color = AwakeningTextSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { userBlock.resetRenownFlags() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (compact) "Ödülleri Sıfırla" else "Şöhret Ödüllerini Sıfırla",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Difficulty & Game Mode
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Oyun Modu ve Zorluk",
                        fontWeight = FontWeight.SemiBold,
                        color = AwakeningTextPrimary,
                        fontSize = 13.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Zorluk Derecesi:", style = MaterialTheme.typography.bodySmall, color = AwakeningTextSecondary, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        difficulties.forEachIndexed { index, name ->
                            FilterChip(
                                selected = currentDiff == index,
                                onClick = { viewModel.setDifficulty(index) },
                                label = {
                                    Text(
                                        text = name,
                                        fontSize = if (compact) 10.5.sp else 11.5.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = currentDiff == index,
                                    borderColor = AwakeningBorderSubtle,
                                    selectedBorderColor = AwakeningGold,
                                    borderWidth = 1.dp
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AwakeningGold,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                    containerColor = AwakeningCardSurface,
                                    labelColor = AwakeningTextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Lunatic+ Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Lunatic+ Modu", fontWeight = FontWeight.SemiBold, color = AwakeningTextPrimary, fontSize = 12.5.sp)
                                if (isLunaticPlus) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = AwakeningCrimsonContainer,
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.5.dp, AwakeningCrimson)
                                    ) {
                                        Text(
                                            text = "LUNATIC+",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AwakeningCrimson,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text("Düşmanlar ölümcül rastgele yeteneklere sahip olur", style = MaterialTheme.typography.bodySmall, color = AwakeningTextSecondary, fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isLunaticPlus,
                            onCheckedChange = { viewModel.setLunaticPlus(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningCrimson,
                                uncheckedThumbColor = AwakeningTextSecondary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Casual Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Rahat Mod (Casual)", fontWeight = FontWeight.SemiBold, color = AwakeningTextPrimary, fontSize = 12.5.sp)
                            Text("Ölen birimler harita bitince geri döner", style = MaterialTheme.typography.bodySmall, color = AwakeningTextSecondary, fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isCasual,
                            onCheckedChange = { viewModel.setCasual(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningGold,
                                uncheckedThumbColor = AwakeningTextSecondary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }
                }
            }
        }

        // 4. Play Time Card
        item {
            val totalSeconds = remember(userBlock.playtime(), modCount) { userBlock.playtime() / 60 }
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Oyun Süresi (Play Time)",
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary,
                                fontSize = 13.5.sp
                            )
                        }
                        Surface(
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                fontWeight = FontWeight.Bold,
                                color = AwakeningGoldBright,
                                fontSize = 12.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toplam süre: $hours saat, $minutes dakika",
                        style = MaterialTheme.typography.bodySmall,
                        color = AwakeningTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setPlaytime(hours + 1, minutes, seconds)
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = "+1 Saat",
                                fontSize = 10.5.sp,
                                color = AwakeningGold,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setPlaytime(hours + 5, minutes, seconds)
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = "+5 Saat",
                                fontSize = 10.5.sp,
                                color = AwakeningGold,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    confirmActionDialog = "Oyun süresi sıfırlanacak (00:00:00). Onaylıyor musunuz?" to {
                                        viewModel.setPlaytime(0, 0, 0)
                                    }
                                },
                            color = AwakeningCardSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, AwakeningBorderSubtle)
                        ) {
                            Text(
                                text = "Sıfırla",
                                fontSize = 10.5.sp,
                                color = AwakeningTextSecondary,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Special Story & Ending Flags
        item {
            val flagGoodEnding = remember(modCount) { userBlock.hasGlobalFlag(2) }
            val flagChromEmblem = remember(modCount) { userBlock.hasGlobalFlag(3) }
            val flagChromVoice = remember(modCount) { userBlock.hasGlobalFlag(4) }

            CollapsibleCard(
                title = "Özel Hikaye & Başarı Bayrakları",
                subtitle = "Chrom Ateş Amblemi, Özel Ses & İyi Son",
                icon = Icons.Default.AutoAwesome,
                initialExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flag 3: Chrom Emblem
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Chrom Kolunda Ateş Amblemi",
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Chrom'un sol kolundaki Ateş Amblemi görsel işaretini açar",
                                style = MaterialTheme.typography.bodySmall,
                                color = AwakeningTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                        Switch(
                            checked = flagChromEmblem,
                            onCheckedChange = { viewModel.setStoryGlobalFlag(3, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningGold,
                                uncheckedThumbColor = AwakeningTextSecondary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }

                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                    // Flag 4: Chrom Voice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Chrom Özel Sesi (\"Anything can change!\")",
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Kritik vuruş ve çift vuruşlarda alternatif ikonik replik",
                                style = MaterialTheme.typography.bodySmall,
                                color = AwakeningTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                        Switch(
                            checked = flagChromVoice,
                            onCheckedChange = { viewModel.setStoryGlobalFlag(4, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningGold,
                                uncheckedThumbColor = AwakeningTextSecondary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }

                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)

                    // Flag 2: Good Ending
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Fedakarlık Sonu (Good Ending)",
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "Robin'in fedakarlık yaptığı özel oyun sonu durumunu işaretler",
                                style = MaterialTheme.typography.bodySmall,
                                color = AwakeningTextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                        Switch(
                            checked = flagGoodEnding,
                            onCheckedChange = { viewModel.setStoryGlobalFlag(2, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AwakeningDarkBg,
                                checkedTrackColor = AwakeningGold,
                                uncheckedThumbColor = AwakeningTextSecondary,
                                uncheckedTrackColor = AwakeningCardSurface
                            )
                        )
                    }
                }
            }
        }

        // 4. World Map & Chapter Unlocks Header Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isMapSectionExpanded = !isMapSectionExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dünya Haritası & Bölüm Kilitleri",
                                    fontWeight = FontWeight.SemiBold,
                                    color = AwakeningTextPrimary,
                                    fontSize = 14.sp
                                )
                                val unlockedCount = allMaps.count { it.lockState() != 0 }
                                Text(
                                    text = "$unlockedCount / ${allMaps.size} Bölüm Açık / Tamamlandı",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AwakeningTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isMapSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AwakeningGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isMapSectionExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = AwakeningBorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Batch Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.unlockAllMaps() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Tümünü Aç",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = { viewModel.beatAllMaps() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AwakeningCardSurface, contentColor = AwakeningTextPrimary),
                                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Tümünü Bitir",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = { viewModel.lockAllParalogues() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AwakeningCardSurface, contentColor = AwakeningTextSecondary),
                                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Paralog Kilitle",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Geri Al (Undo) Bildirim Bannerı
                        AnimatedVisibility(visible = uiState.canUndoMap) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                color = AwakeningGoldContainer,
                                border = BorderStroke(1.dp, AwakeningGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = AwakeningGoldBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Toplu İşlem Yapıldı",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AwakeningTextPrimary
                                            )
                                            Text(
                                                text = "Harita durumlarını bir işlem daha yapmadan eski haline döndürebilirsiniz.",
                                                fontSize = 10.sp,
                                                color = AwakeningTextSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { viewModel.undoMapChanges() },
                                        modifier = Modifier.height(30.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AwakeningGold,
                                            contentColor = androidx.compose.ui.graphics.Color.White
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "Geri Al",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Geri Al",
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        AwakeningSearchBar(
                            query = mapSearchQuery,
                            onQueryChange = { mapSearchQuery = it },
                            placeholder = "Bölüm ara (örn. Paralogue 1, Chapter 10)..."
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedMapFilter == 0,
                                    onClick = { selectedMapFilter = 0 },
                                    label = { Text("Tümü (${allMaps.size})", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedMapFilter == 0,
                                        borderColor = AwakeningBorderSubtle,
                                        selectedBorderColor = AwakeningGold,
                                        borderWidth = 1.dp
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AwakeningGold,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                        containerColor = AwakeningCardSurface,
                                        labelColor = AwakeningTextSecondary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedMapFilter == 1,
                                    onClick = { selectedMapFilter = 1 },
                                    label = { Text("Ana Hikaye", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedMapFilter == 1,
                                        borderColor = AwakeningBorderSubtle,
                                        selectedBorderColor = AwakeningGold,
                                        borderWidth = 1.dp
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AwakeningGold,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                        containerColor = AwakeningCardSurface,
                                        labelColor = AwakeningTextSecondary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedMapFilter == 2,
                                    onClick = { selectedMapFilter = 2 },
                                    label = { Text("Paralogue", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedMapFilter == 2,
                                        borderColor = AwakeningBorderSubtle,
                                        selectedBorderColor = AwakeningGold,
                                        borderWidth = 1.dp
                                    ),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AwakeningGold,
                                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                        containerColor = AwakeningCardSurface,
                                        labelColor = AwakeningTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Individual Map/Chapter Rows (Virtual rendered in LazyColumn)
        if (isMapSectionExpanded) {
            items(visibleMaps, key = { "${it.index}_${it.state}" }) { item ->
                ChapterMapRow(
                    item = item,
                    currentState = item.state,
                    compact = compact,
                    onStateChange = { newState ->
                        viewModel.setMapState(item.index, newState)
                    }
                )
            }

            // "Daha Fazla Göster" / "Daralt" Parça Parça Kademeli Genişletme
            if (mapSearchQuery.isBlank()) {
                if (hiddenCount > 0) {
                    val nextBatch = minOf(8, hiddenCount)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { extraChaptersRevealed += 8 },
                                color = AwakeningCardSurface,
                                border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = AwakeningGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Daha Fazla (+$nextBatch • $hiddenCount Kalan)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AwakeningGoldBright,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            if (extraChaptersRevealed > 0) {
                                Surface(
                                    modifier = Modifier
                                        .clickable { extraChaptersRevealed = 0 },
                                    color = AwakeningCardSurface,
                                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            tint = AwakeningTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Daralt",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AwakeningTextSecondary,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (extraChaptersRevealed > 0) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { extraChaptersRevealed = 0 },
                            color = AwakeningCardSurface,
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = AwakeningTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bölümleri Daralt",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AwakeningTextSecondary,
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

        // 5. Harita Karşılaşmaları (Overworld Encounters: Risen & Anna)
        item {
            OverworldEncountersSection(
                chapter = chapter,
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // 6. Kışla Etkinlikleri (Barracks Events)
        item {
            BarracksSection(
                chapter = chapter,
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // 7. Sokak Geçişi Ekibi (StreetPass Team & Wireless)
        item {
            StreetPassTeamSection(
                chapter = chapter,
                viewModel = viewModel,
                uiState = uiState
            )
        }

        // 8. İkili Düello & DLC Rekorları
        item {
            DoubleDuelDlcSection(
                chapter = chapter,
                viewModel = viewModel,
                uiState = uiState
            )
        }
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
private fun ChapterMapRow(
    item: MapItem,
    currentState: Int,
    compact: Boolean,
    onStateChange: (Int) -> Unit
) {
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
            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    color = AwakeningTextPrimary,
                    fontSize = if (compact) 12.sp else 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isParalogue) "Yan Görev (Paralogue)" else "Ana Hikaye • Bölüm ${item.index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isParalogue) AwakeningFalchionBlue else AwakeningTextTertiary,
                    fontSize = 10.sp
                )
            }

            // 3-state Segmented Buttons: [Kilitli (0)] [Açık (2)] [Bitti (1)]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AwakeningCardSurface)
                    .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(6.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kilitli (State 0)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentState == 0) Color(0xFF3B1515) else Color.Transparent)
                        .clickable { onStateChange(0) }
                        .width(42.dp)
                        .height(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kilitli",
                        fontSize = 10.5.sp,
                        fontWeight = if (currentState == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentState == 0) Color(0xFFFF6B6B) else AwakeningTextTertiary,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                // Açık (State 2)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentState == 2) AwakeningGoldContainer else Color.Transparent)
                        .clickable { onStateChange(2) }
                        .width(42.dp)
                        .height(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Açık",
                        fontSize = 10.5.sp,
                        fontWeight = if (currentState == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentState == 2) AwakeningGoldBright else AwakeningTextTertiary,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                // Tamamlandı (State 1)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentState == 1) Color(0xFF0F3923) else Color.Transparent)
                        .clickable { onStateChange(1) }
                        .width(42.dp)
                        .height(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bitti",
                        fontSize = 10.5.sp,
                        fontWeight = if (currentState == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentState == 1) Color(0xFF4ADE80) else AwakeningTextTertiary,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
