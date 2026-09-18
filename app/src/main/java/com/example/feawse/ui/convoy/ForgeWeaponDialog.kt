package com.example.feawse.ui.convoy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.feawse.data.ItemDb
import com.example.feawse.savefile.inventory.RefiBlock
import com.example.feawse.savefile.inventory.Refinement
import com.example.feawse.theme.*
import com.example.feawse.ui.components.NumberStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgeWeaponDialog(
    refiBlock: RefiBlock,
    isWest: Boolean,
    existingRefinement: Refinement? = null,
    initialUses: Int = 0,
    onDismiss: () -> Unit,
    onSave: (Refinement, Int) -> Unit
) {
    val isEdit = existingRefinement != null
    var selectedWeaponId by remember {
        mutableIntStateOf(existingRefinement?.weaponId() ?: 1)
    }
    var customName by remember {
        mutableStateOf(existingRefinement?.name ?: ItemDb.getItemName(selectedWeaponId))
    }
    var mightBonus by remember {
        mutableIntStateOf(existingRefinement?.might() ?: 0)
    }
    var hitBonus by remember {
        mutableIntStateOf(existingRefinement?.hit() ?: 0)
    }
    var critBonus by remember {
        mutableIntStateOf(existingRefinement?.crit() ?: 0)
    }
    var isEnemy by remember {
        mutableStateOf(existingRefinement?.isEnemy ?: false)
    }
    var convoyUses by remember {
        val defaultUses = ItemDb.getItemUses(selectedWeaponId).let { if (it <= 0) 30 else it }
        mutableIntStateOf(if (isEdit) initialUses else defaultUses)
    }

    var showWeaponPicker by remember { mutableStateOf(false) }
    val maxNameLength = if (isWest) 18 else 10

    // Live stat calculations
    val baseMight = remember(selectedWeaponId) { ItemDb.getItemMight(selectedWeaponId) }
    val baseHit = remember(selectedWeaponId) { ItemDb.getItemHit(selectedWeaponId) }
    val baseCrit = remember(selectedWeaponId) { ItemDb.getItemCrit(selectedWeaponId) }
    val baseMaxUses = remember(selectedWeaponId) {
        ItemDb.getItemUses(selectedWeaponId).let { if (it <= 0) 30 else it }
    }

    val totalMight = remember(baseMight, mightBonus) { (baseMight + mightBonus) % 256 }
    val totalHit = remember(baseHit, hitBonus) { (baseHit + hitBonus) % 256 }
    val totalCrit = remember(baseCrit, critBonus) { (baseCrit + critBonus) % 256 }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = AwakeningNavySurface,
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = AwakeningGoldBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEdit) "Dövülmüş Silahı Düzenle" else "Yeni Silah Döv (Forge)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AwakeningGoldBright
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Live Preview Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
                        border = BorderStroke(1.dp, AwakeningGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AwakeningGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = customName.ifBlank { ItemDb.getItemName(selectedWeaponId) },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AwakeningGoldBright,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isEnemy) {
                                    Surface(
                                        color = AwakeningCrimson.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, AwakeningCrimson),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Düşman / SpotPass",
                                            color = AwakeningCrimson,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Temel Silah: ${ItemDb.getItemName(selectedWeaponId)} | Konvoyda: $convoyUses kullanım",
                                fontSize = 11.sp,
                                color = AwakeningTextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stat Row Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPreviewBadge(
                                    modifier = Modifier.weight(1f),
                                    label = "Might (Hasar)",
                                    base = baseMight,
                                    bonus = mightBonus,
                                    total = totalMight,
                                    accentColor = Color(0xFFEF5350)
                                )
                                StatPreviewBadge(
                                    modifier = Modifier.weight(1f),
                                    label = "Hit (İsabet)",
                                    base = baseHit,
                                    bonus = hitBonus,
                                    total = totalHit,
                                    accentColor = AwakeningFalchionBlue
                                )
                                StatPreviewBadge(
                                    modifier = Modifier.weight(1f),
                                    label = "Crit (Kritik)",
                                    base = baseCrit,
                                    bonus = critBonus,
                                    total = totalCrit,
                                    accentColor = AwakeningGold
                                )
                            }
                        }
                    }

                    // 2. Base Weapon Selection
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Temel Silah (Base Weapon)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AwakeningNavySurface, RoundedCornerShape(8.dp))
                                    .border(1.dp, AwakeningBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { showWeaponPicker = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ItemDb.getItemName(selectedWeaponId),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AwakeningGoldBright
                                    )
                                    Text(
                                        text = "Mt: $baseMight | Hit: $baseHit% | Crit: $baseCrit% | Maks: $baseMaxUses kullanım",
                                        fontSize = 11.sp,
                                        color = AwakeningTextSecondary
                                    )
                                }

                                Button(
                                    onClick = { showWeaponPicker = true },
                                    modifier = Modifier.height(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("Değiştir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // 3. Custom Name Input
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
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
                                    text = "Özel Silah Adı",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AwakeningTextPrimary
                                )
                                Text(
                                    text = "${customName.length} / $maxNameLength karakter",
                                    fontSize = 11.sp,
                                    color = if (customName.length >= maxNameLength) AwakeningCrimson else AwakeningTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = customName,
                                onValueChange = {
                                    if (it.length <= maxNameLength) {
                                        customName = it
                                    }
                                },
                                placeholder = { Text("Silah adı girin...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AwakeningGold,
                                    unfocusedBorderColor = AwakeningBorderSubtle,
                                    focusedTextColor = AwakeningTextPrimary,
                                    unfocusedTextColor = AwakeningTextPrimary,
                                    cursorColor = AwakeningGold
                                ),
                                trailingIcon = {
                                    if (customName.isNotEmpty()) {
                                        IconButton(onClick = { customName = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // 4. Stat Bonuses (+Mt, +Hit, +Crit)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
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
                                    text = "Silah Geliştirme Bonusları",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AwakeningTextPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            mightBonus = 5
                                            hitBonus = 15
                                            critBonus = 9
                                        },
                                        modifier = Modifier.height(26.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, AwakeningGold)
                                    ) {
                                        Text("+Standart Maks", fontSize = 10.sp, color = AwakeningGoldBright)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            mightBonus = 0
                                            hitBonus = 0
                                            critBonus = 0
                                        },
                                        modifier = Modifier.height(26.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        border = BorderStroke(1.dp, AwakeningBorderSubtle)
                                    ) {
                                        Text("Sıfırla", fontSize = 10.sp, color = AwakeningTextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Might Stepper
                            StatStepperRow(
                                label = "+Might (Hasar Bonusu)",
                                value = mightBonus,
                                min = 0,
                                max = 255,
                                accentColor = Color(0xFFEF5350),
                                onValueChange = { mightBonus = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Hit Stepper
                            StatStepperRow(
                                label = "+Hit (İsabet Bonusu)",
                                value = hitBonus,
                                min = 0,
                                max = 255,
                                accentColor = AwakeningFalchionBlue,
                                onValueChange = { hitBonus = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Crit Stepper
                            StatStepperRow(
                                label = "+Crit (Kritik Bonusu)",
                                value = critBonus,
                                min = 0,
                                max = 255,
                                accentColor = AwakeningGold,
                                onValueChange = { critBonus = it }
                            )
                        }
                    }

                    // 5. Convoy Quantity & Flags
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AwakeningCardSurface,
                        border = BorderStroke(1.dp, AwakeningBorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Depo & Özel Bayraklar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AwakeningTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Konvoy Kullanım Sayısı", fontSize = 13.sp, color = AwakeningTextPrimary)
                                    Text(
                                        text = "Varsayılan 1 silah: $baseMaxUses kullanım",
                                        fontSize = 11.sp,
                                        color = AwakeningTextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    NumberStepper(
                                        value = convoyUses,
                                        min = 0,
                                        max = 999,
                                        step = 5,
                                        onValueChange = { convoyUses = it }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = AwakeningBorderSubtle, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Enemy Weapon Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("Düşman / SpotPass Silahı", fontSize = 13.sp, color = AwakeningTextPrimary)
                                    Text(
                                        text = "Bu silahı düşman ya da kablosuz/SpotPass birimleri için işaretler.",
                                        fontSize = 11.sp,
                                        color = AwakeningTextSecondary
                                    )
                                }

                                Switch(
                                    checked = isEnemy,
                                    onCheckedChange = { isEnemy = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AwakeningGoldBright,
                                        checkedTrackColor = AwakeningGold.copy(alpha = 0.5f),
                                        uncheckedThumbColor = AwakeningTextSecondary,
                                        uncheckedTrackColor = AwakeningNavySurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("İptal", color = AwakeningTextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val finalRefi = if (existingRefinement != null) {
                                existingRefinement.apply {
                                    setWeaponId(selectedWeaponId)
                                    setName(customName.ifBlank { ItemDb.getItemName(selectedWeaponId) })
                                    setMight(mightBonus)
                                    setHit(hitBonus)
                                    setCrit(critBonus)
                                    setFlagEnemy(isEnemy)
                                }
                            } else {
                                val nextId = refiBlock.newRefiId()
                                if (nextId == -1) {
                                    return@Button
                                }
                                Refinement(isWest).apply {
                                    setPosition(nextId)
                                    setWeaponId(selectedWeaponId)
                                    setName(customName.ifBlank { ItemDb.getItemName(selectedWeaponId) })
                                    setMight(mightBonus)
                                    setHit(hitBonus)
                                    setCrit(critBonus)
                                    setFlagEnemy(isEnemy)
                                }
                            }
                            onSave(finalRefi, convoyUses)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEdit) "Güncelle" else "Silahı Döv",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Sub-dialog: Base Weapon Picker
    if (showWeaponPicker) {
        BaseWeaponPickerDialog(
            currentWeaponId = selectedWeaponId,
            onDismiss = { showWeaponPicker = false },
            onWeaponSelected = { weaponId ->
                selectedWeaponId = weaponId
                val weaponName = ItemDb.getItemName(weaponId)
                if (customName.isBlank() || customName == ItemDb.getItemName(selectedWeaponId)) {
                    customName = weaponName.take(maxNameLength)
                }
                if (convoyUses == 0) {
                    convoyUses = ItemDb.getItemUses(weaponId).let { if (it <= 0) 30 else it }
                }
                showWeaponPicker = false
            }
        )
    }
}

@Composable
private fun StatPreviewBadge(
    modifier: Modifier = Modifier,
    label: String,
    base: Int,
    bonus: Int,
    total: Int,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        color = AwakeningNavySurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = AwakeningTextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$total",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                if (bonus > 0) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "(+$bonus)",
                        fontSize = 10.sp,
                        color = AwakeningGoldBright,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatStepperRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, color = AwakeningTextPrimary)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NumberStepper(
                value = value,
                min = min,
                max = max,
                step = 1,
                onValueChange = onValueChange
            )
        }
    }
}

@Composable
fun BaseWeaponPickerDialog(
    currentWeaponId: Int,
    onDismiss: () -> Unit,
    onWeaponSelected: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableIntStateOf(0) } // 0: All, 1: Kılıç, 2: Mızrak, 3: Balta, 4: Yay, 5: Büyü/Asa, 6: Diğer

    val allRegularNames = remember { ItemDb.getItemNamesRegular() }
    val filteredList = remember(searchQuery, selectedCategory) {
        val list = mutableListOf<Triple<Int, String, Int>>() // id, name, uses
        for (i in 1 until allRegularNames.size) {
            val name = allRegularNames[i]
            if (name.isBlank() || name == "None") continue
            val matchesSearch = searchQuery.isBlank() || name.contains(searchQuery, ignoreCase = true)
            val type = ItemDb.getItemType(i)
            val matchesCategory = when (selectedCategory) {
                1 -> type == 0 // Sword
                2 -> type == 1 // Lance
                3 -> type == 2 // Axe
                4 -> type == 3 // Bow
                5 -> type == 4 || type == 5 // Tome or Staff
                6 -> type > 5 // Stones/items
                else -> true
            }
            if (matchesSearch && matchesCategory) {
                list.add(Triple(i, name, ItemDb.getItemUses(i)))
            }
        }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(14.dp),
            color = AwakeningNavySurface,
            border = BorderStroke(1.dp, AwakeningBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temel Silah Seçimi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AwakeningGoldBright)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = AwakeningTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Silah ara (örn. Silver Sword, Brave, Killer)...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AwakeningGold,
                        unfocusedBorderColor = AwakeningBorderSubtle,
                        focusedTextColor = AwakeningTextPrimary,
                        unfocusedTextColor = AwakeningTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("Tümü", "Kılıç", "Mızrak", "Balta", "Yay", "Büyü/Asa", "Diğer")
                    items(categories.size) { idx ->
                        FilterChip(
                            selected = selectedCategory == idx,
                            onClick = { selectedCategory = idx },
                            label = { Text(categories[idx], fontSize = 11.sp) },
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

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList) { (id, name, defaultUses) ->
                        val isCurrent = id == currentWeaponId
                        val mt = ItemDb.getItemMight(id)
                        val hit = ItemDb.getItemHit(id)
                        val crit = ItemDb.getItemCrit(id)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onWeaponSelected(id) },
                            color = if (isCurrent) AwakeningCardSurface.copy(alpha = 0.9f) else AwakeningCardSurface,
                            border = BorderStroke(1.dp, if (isCurrent) AwakeningGold else AwakeningBorderSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isCurrent) AwakeningGoldBright else AwakeningTextPrimary
                                    )
                                    Text(
                                        text = "Mt: $mt | Hit: $hit% | Crit: $crit% | ${if (defaultUses > 0) "$defaultUses kullanım" else "Sınırsız"}",
                                        fontSize = 11.sp,
                                        color = AwakeningTextSecondary
                                    )
                                }

                                if (isCurrent) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = AwakeningGold,
                                        modifier = Modifier.size(18.dp)
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
