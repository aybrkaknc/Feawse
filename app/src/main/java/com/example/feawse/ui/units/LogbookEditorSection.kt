package com.example.feawse.ui.units

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.feawse.data.model.EinherjarModel
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
fun LogbookEditorSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val modCount = uiState.modificationCount

    val hasLog = unit.rawLog != null
    val isEinherjar = remember(unit, modCount, hasLog) {
        if (hasLog) unit.rawLog.isEinherjar else false
    }

    var showConfirmRemoveDialog by remember { mutableStateOf(false) }
    var showEinherjarPicker by remember { mutableStateOf(false) }
    var expandedProfileDetails by remember { mutableStateOf(false) }

    // Subtitle text
    val subtitleText = remember(unit, modCount, hasLog, isEinherjar) {
        if (!hasLog) {
            "Logbook / Avatar bloğu yok (Eklemek için dokunun)"
        } else {
            val typeStr = if (isEinherjar) "Einherjar Kartı" else "Avatar (Robin MU)"
            val name = unit.rawLog.name
            val logId = unit.rawLog.logId.takeLast(6)
            "$typeStr • Ad: $name • ID: $logId"
        }
    }

    CollapsibleCard(
        title = "Logbook / Avatar & Einherjar Kartı",
        subtitle = subtitleText,
        icon = Icons.Default.Badge,
        initialExpanded = false,
        modifier = modifier
    ) {
        if (!hasLog) {
            // No LogBlock Card
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
                            text = "Bu birimde Logbook / Avatar veri bloğu bulunmuyor.",
                            color = AwakeningTextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Logbook bloğu ile bu birimi özel bir Avatar'a (Robin/MU) veya SpotPass/DLC Einherjar kartına (Marth, Roy, Lyn, Ike vb.) dönüştürebilir; serbestçe isim, yüz, vücut, ses ve StreetPass kart mesajları tanımlayabilirsiniz.",
                        color = AwakeningTextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.addUnitLogBlock(unit) },
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
                            text = "Logbook Bloğu Ekle (+ LogBlock)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            return@CollapsibleCard
        }

        // Active LogBlock State
        val log = unit.rawLog
        val portraitBmp = remember(unit, modCount) {
            PortraitManager.getPortrait(context, unit)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Status Bar & Unit Type Selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Portrait & Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (portraitBmp != null) {
                                Image(
                                    bitmap = portraitBmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AwakeningDarkBg)
                                        .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(6.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AwakeningDarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = log.name.ifBlank { unit.unitName() },
                                    color = AwakeningTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (log.isWest) "Bölge: US/EU (0x188)" else "Bölge: Japonya (0xFC)",
                                    color = AwakeningTextTertiary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        // Remove Block Button
                        OutlinedButton(
                            onClick = { showConfirmRemoveDialog = true },
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningCrimson),
                            border = BorderStroke(1.dp, AwakeningCrimson.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bloğu Kaldır", fontSize = 10.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unit Type Switch [ Avatar (Robin MU) ] vs [ Einherjar (Kart) ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setLogUnitEinherjar(unit, false) },
                            color = if (!isEinherjar) AwakeningGold else AwakeningCardSurface,
                            border = BorderStroke(1.dp, if (!isEinherjar) AwakeningGoldBright else AwakeningBorderSubtle)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Avatar (Robin MU)",
                                    color = if (!isEinherjar) AwakeningDarkBg else AwakeningTextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (!isEinherjar) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setLogUnitEinherjar(unit, true) },
                            color = if (isEinherjar) AwakeningFalchionBlue else AwakeningCardSurface,
                            border = BorderStroke(1.dp, if (isEinherjar) AwakeningFalchionBlue else AwakeningBorderSubtle)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Einherjar (SpotPass Kartı)",
                                    color = if (isEinherjar) AwakeningTextPrimary else AwakeningTextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isEinherjar) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Einherjar Preset Button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showEinherjarPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AwakeningCardSurface,
                            contentColor = AwakeningGoldBright
                        ),
                        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Einherjar Şablonu Seç (Marth, Roy, Lyn, Ike...)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 2. Identity & Appearance (Name, Gender, LogID, Build, Voice)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Kimlik & Karakter Görünümü",
                        color = AwakeningGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Character Name TextField
                    var nameInput by remember(log.name) { mutableStateOf(log.name) }
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it.take(log.NAME_CHARACTERS)
                            viewModel.setLogUnitName(unit, nameInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        label = { Text("Logbook Karakter Adı", fontSize = 11.sp) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.5.sp, color = AwakeningTextPrimary),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwakeningGold,
                            unfocusedBorderColor = AwakeningBorderSubtle,
                            focusedContainerColor = AwakeningNavySurface,
                            unfocusedContainerColor = AwakeningNavySurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gender Selector
                    val fullBuild = remember(unit, modCount) { log.fullBuild }
                    val isFemale = fullBuild[4] > 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cinsiyet:", color = AwakeningTextSecondary, fontSize = 11.5.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setLogUnitGender(unit, false) },
                                color = if (!isFemale) AwakeningFalchionBlue else AwakeningNavySurface,
                                border = BorderStroke(1.dp, if (!isFemale) AwakeningFalchionBlue else AwakeningBorderSubtle)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Erkek", color = AwakeningTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setLogUnitGender(unit, true) },
                                color = if (isFemale) AwakeningCrimson else AwakeningNavySurface,
                                border = BorderStroke(1.dp, if (isFemale) AwakeningCrimson else AwakeningBorderSubtle)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Kadın", color = AwakeningTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Build, Face, Hair, Voice Steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vücut Yapısı (Build: 0-2):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        NumberStepper(
                            value = fullBuild[0],
                            min = 0,
                            max = if (isFemale) 1 else 2,
                            onValueChange = { v -> viewModel.setLogUnitBuild(unit, 0, v) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Yüz Modeli (Face: 0-4):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        NumberStepper(
                            value = fullBuild[1].coerceIn(0, 4),
                            min = 0,
                            max = 4,
                            onValueChange = { v -> viewModel.setLogUnitBuild(unit, 1, v) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saç Stili (Hair: 0-4):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        NumberStepper(
                            value = fullBuild[2],
                            min = 0,
                            max = 4,
                            onValueChange = { v -> viewModel.setLogUnitBuild(unit, 2, v) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ses Tonu (Voice: 0-4):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        NumberStepper(
                            value = fullBuild[3],
                            min = 0,
                            max = 4,
                            onValueChange = { v -> viewModel.setLogUnitVoice(unit, v) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Asset & Flaw
                    val assetFlaw = remember(unit, modCount) { log.assetFlaw }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Asset Dropdown
                        var assetExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            val aLabel = if (assetFlaw[0] in 0 until MiscDb.modifNames.size) MiscDb.modifNames[assetFlaw[0]] else "None"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { assetExpanded = true },
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
                                    Text("+Asset: $aLabel", color = AwakeningSuccess, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AwakeningSuccess, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }, modifier = Modifier.background(AwakeningNavySurface)) {
                                MiscDb.modifNames.forEachIndexed { idx, name ->
                                    DropdownMenuItem(
                                        text = { Text(if (idx == 0) "Yok" else "+$name", fontSize = 11.5.sp) },
                                        onClick = {
                                            viewModel.setLogUnitAsset(unit, idx)
                                            assetExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Flaw Dropdown
                        var flawExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            val fLabel = if (assetFlaw[1] in 0 until MiscDb.modifNames.size) MiscDb.modifNames[assetFlaw[1]] else "None"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { flawExpanded = true },
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
                                    Text("-Flaw: $fLabel", color = AwakeningCrimson, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AwakeningCrimson, modifier = Modifier.size(16.dp))
                                }
                            }

                            DropdownMenu(expanded = flawExpanded, onDismissRequest = { flawExpanded = false }, modifier = Modifier.background(AwakeningNavySurface)) {
                                MiscDb.modifNames.forEachIndexed { idx, name ->
                                    DropdownMenuItem(
                                        text = { Text(if (idx == 0) "Yok" else "-$name", fontSize = 11.5.sp) },
                                        onClick = {
                                            viewModel.setLogUnitFlaw(unit, idx)
                                            flawExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Birthday: Day & Month
                    val bday = remember(unit, modCount) { log.birthday }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Doğum Günü (Gün / Ay):", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            NumberStepper(
                                value = bday[0].coerceIn(1, 31),
                                min = 1,
                                max = 31,
                                onValueChange = { d -> viewModel.setLogUnitBirthday(unit, d, bday[1]) }
                            )
                            Text("/", color = AwakeningTextTertiary, fontSize = 13.sp)
                            NumberStepper(
                                value = bday[1].coerceIn(1, 12),
                                min = 1,
                                max = 12,
                                onValueChange = { m -> viewModel.setLogUnitBirthday(unit, bday[0], m) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Log ID Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Log ID (26 Karakter):", fontSize = 10.sp, color = AwakeningTextSecondary)
                            Text(
                                text = log.logId,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AwakeningGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { viewModel.setLogUnitRandomId(unit) },
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningGold
                            ),
                            border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rastgele ID", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. StreetPass & Profile Messages (Collapsible)
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
                            .clickable { expandedProfileDetails = !expandedProfileDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = AwakeningGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "StreetPass & Profil Kart Mesajları",
                                color = AwakeningTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (expandedProfileDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AwakeningTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (expandedProfileDetails) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        var greetingInput by remember(unit, modCount) { mutableStateOf(log.getTextGreeting()) }
                        var challengeInput by remember(unit, modCount) { mutableStateOf(log.getTextChallenge()) }
                        var recruitInput by remember(unit, modCount) { mutableStateOf(log.getTextRecruit()) }
                        var streetInput by remember(unit, modCount) { mutableStateOf(log.getTextStreet()) }

                        // Greeting
                        OutlinedTextField(
                            value = greetingInput,
                            onValueChange = {
                                greetingInput = it.take(log.MESSAGE_CHARACTERS)
                                viewModel.setLogUnitMessages(unit, greetingInput, challengeInput, recruitInput, streetInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            label = { Text("Karşılama Mesajı (Greeting)", fontSize = 10.5.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp, color = AwakeningTextPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AwakeningGold,
                                unfocusedBorderColor = AwakeningBorderSubtle,
                                focusedContainerColor = AwakeningNavySurface,
                                unfocusedContainerColor = AwakeningNavySurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Challenge
                        OutlinedTextField(
                            value = challengeInput,
                            onValueChange = {
                                challengeInput = it.take(log.MESSAGE_CHARACTERS)
                                viewModel.setLogUnitMessages(unit, greetingInput, challengeInput, recruitInput, streetInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            label = { Text("Meydan Okuma Mesajı (Challenge)", fontSize = 10.5.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp, color = AwakeningTextPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AwakeningGold,
                                unfocusedBorderColor = AwakeningBorderSubtle,
                                focusedContainerColor = AwakeningNavySurface,
                                unfocusedContainerColor = AwakeningNavySurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Recruit
                        OutlinedTextField(
                            value = recruitInput,
                            onValueChange = {
                                recruitInput = it.take(log.MESSAGE_CHARACTERS)
                                viewModel.setLogUnitMessages(unit, greetingInput, challengeInput, recruitInput, streetInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            label = { Text("Katılma Mesajı (Recruit)", fontSize = 10.5.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp, color = AwakeningTextPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AwakeningGold,
                                unfocusedBorderColor = AwakeningBorderSubtle,
                                focusedContainerColor = AwakeningNavySurface,
                                unfocusedContainerColor = AwakeningNavySurface
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // StreetPass Status
                        OutlinedTextField(
                            value = streetInput,
                            onValueChange = {
                                streetInput = it.take(log.MESSAGE_CHARACTERS)
                                viewModel.setLogUnitMessages(unit, greetingInput, challengeInput, recruitInput, streetInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            label = { Text("StreetPass Durum Mesajı", fontSize = 10.5.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp, color = AwakeningTextPrimary),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AwakeningGold,
                                unfocusedBorderColor = AwakeningBorderSubtle,
                                focusedContainerColor = AwakeningNavySurface,
                                unfocusedContainerColor = AwakeningNavySurface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Difficulty Selector (Normal, Hard, Lunatic)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kart Zorluk Seviyesi:", fontSize = 11.5.sp, color = AwakeningTextSecondary)
                            val diff = log.difficulty()
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Normal" to 0, "Hard" to 1, "Lunatic" to 2).forEach { (dName, dVal) ->
                                    val isSel = diff == dVal
                                    Surface(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { viewModel.setLogUnitDifficulty(unit, dVal) },
                                        color = if (isSel) AwakeningGold else AwakeningNavySurface,
                                        border = BorderStroke(1.dp, if (isSel) AwakeningGoldBright else AwakeningBorderSubtle)
                                    ) {
                                        Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                                            Text(dName, color = if (isSel) AwakeningDarkBg else AwakeningTextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Game Mode Flags (Casual, Lunatic+, Game Beaten)
                        Text("Oyun Modu Rozetleri:", fontSize = 11.sp, color = AwakeningTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val flags = listOf(
                                Triple(0, "Rahat (Casual)", log.gameModeFlag(0)),
                                Triple(1, "Lunatic+", log.gameModeFlag(1)),
                                Triple(2, "Bitti (Beaten)", log.gameModeFlag(2))
                            )

                            flags.forEach { (slot, fName, isEnabled) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.setLogUnitGameModeFlag(unit, slot, !isEnabled) },
                                    color = if (isEnabled) AwakeningSuccess.copy(alpha = 0.2f) else AwakeningNavySurface,
                                    border = BorderStroke(1.dp, if (isEnabled) AwakeningSuccess else AwakeningBorderSubtle)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = fName,
                                            color = if (isEnabled) AwakeningSuccess else AwakeningTextTertiary,
                                            fontSize = 10.sp,
                                            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal
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

    // Confirm Remove Dialog
    if (showConfirmRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRemoveDialog = false },
            title = {
                Text(
                    text = "Logbook Bloğunu Kaldır",
                    color = AwakeningCrimson,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "${unit.unitName()} biriminin Logbook/Avatar veri bloğu silinecek. Birim normal standart birim yapısına dönecek. Onaylıyor musunuz?",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeUnitLogBlock(unit)
                        showConfirmRemoveDialog = false
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
                    onClick = { showConfirmRemoveDialog = false },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("İptal", color = AwakeningTextSecondary, fontSize = 11.5.sp)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Einherjar Picker Dialog
    if (showEinherjarPicker) {
        EinherjarPickerDialog(
            onDismiss = { showEinherjarPicker = false },
            onSelect = { einherjar ->
                viewModel.applyEinherjarPreset(unit, einherjar)
                showEinherjarPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinherjarPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (EinherjarModel) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val einherjarList = remember { UnitDb.getEinherjarList() }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) einherjarList
        else einherjarList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Einherjar Kartı Seç (${einherjarList.size})",
                            color = AwakeningGoldBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextTertiary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                AwakeningSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Einherjar ara (Marth, Lyn, Roy...)",
                    containerColor = AwakeningDarkBg
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = AwakeningBorderSubtle, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // List of Einherjar
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.logId }) { einherjar ->
                        val spPath = "portrait/spotpass/${einherjar.logId}.png"
                        val spBitmap = remember(einherjar.logId) {
                            try {
                                val isStream = context.assets.open(spPath)
                                val bmp = BitmapFactory.decodeStream(isStream)
                                isStream.close()
                                bmp
                            } catch (e: Exception) {
                                null
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSelect(einherjar) },
                            color = AwakeningCardSurface,
                            border = BorderStroke(1.dp, AwakeningBorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (spBitmap != null) {
                                    Image(
                                        bitmap = spBitmap.asImageBitmap(),
                                        contentDescription = einherjar.name,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AwakeningDarkBg)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AwakeningDarkBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = AwakeningTextTertiary, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = einherjar.name,
                                        color = AwakeningTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val assetName = if (einherjar.asset in 0 until MiscDb.modifNames.size) MiscDb.modifNames[einherjar.asset] else "None"
                                    val flawName = if (einherjar.flaw in 0 until MiscDb.modifNames.size) MiscDb.modifNames[einherjar.flaw] else "None"
                                    Text(
                                        text = "Asset: +$assetName • Flaw: -$flawName",
                                        color = AwakeningTextTertiary,
                                        fontSize = 10.5.sp
                                    )
                                }

                                Text(
                                    text = "#${einherjar.logId}",
                                    color = AwakeningGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
