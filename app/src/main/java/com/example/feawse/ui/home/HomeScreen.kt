package com.example.feawse.ui.home

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.theme.*
import com.example.feawse.ui.components.BackupWarningBanner
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.ui.components.EmulatorGuideDialog
import com.example.feawse.ui.components.StatusBadge
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.util.BackupEntry
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@Composable
fun HomeScreen(
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    modifier: Modifier = Modifier,
    onRequestSave: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showGuideDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf<BackupEntry?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<BackupEntry?>(null) }
    var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileName(context, uri) ?: "chapter0"
            viewModel.openSaveFile(uri, fileName, context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBackups(context)
        if (uiState.chapterFile == null && uiState.globalFile == null) {
            val appSample = java.io.File(context.getExternalFilesDir(null), "Chapter2")
            if (appSample.exists()) {
                viewModel.openSaveFile(appSample, context)
            } else {
                val sample = java.io.File("/sdcard/Download/Chapter2")
                if (sample.exists()) {
                    viewModel.openSaveFile(sample, context)
                }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        modifier = modifier,
        onOpenFile = { filePicker.launch(arrayOf("*/*")) },
        onManualBackup = { viewModel.createManualBackup(context) },
        onRestoreBackup = { showRestoreConfirmDialog = it },
        onDeleteBackup = { showDeleteConfirmDialog = it },
        onDeleteAllBackups = { showDeleteAllConfirmDialog = true },
        onRefreshBackups = { viewModel.loadBackups(context) },
        onSaveFile = {
            if (onRequestSave != null) {
                onRequestSave()
            } else {
                showSaveConfirmDialog = true
            }
        },
        showGuideDialog = showGuideDialog,
        onToggleGuideDialog = { showGuideDialog = it },
        showRestoreConfirmDialog = showRestoreConfirmDialog,
        onConfirmRestore = { backup ->
            viewModel.restoreBackup(backup, context)
            showRestoreConfirmDialog = null
        },
        onDismissRestoreConfirm = { showRestoreConfirmDialog = null },
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        onConfirmDelete = { backup ->
            viewModel.deleteBackup(backup, context)
            showDeleteConfirmDialog = null
        },
        onDismissDeleteConfirm = { showDeleteConfirmDialog = null },
        showDeleteAllConfirmDialog = showDeleteAllConfirmDialog,
        onConfirmDeleteAll = {
            viewModel.deleteAllBackups(context)
            showDeleteAllConfirmDialog = false
        },
        onDismissDeleteAllConfirm = { showDeleteAllConfirmDialog = false },
        onNavigateTab = { viewModel.setActiveTab(it) }
    )

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
                        text = "Kayıt Dosyası Kaydedilsin mi?",
                        fontWeight = FontWeight.Bold,
                        color = AwakeningTextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Yapılan düzenlemeler kayıt dosyasına kalıcı olarak yazılacaktır. Devam etmek istiyor musunuz?",
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
                                    text = uiState.currentFileName ?: "Kayıt Dosyası",
                                    color = AwakeningTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                            uiState.slotInfo?.let { slot ->
                                Text(
                                    text = slot.displayName,
                                    color = AwakeningFalchionCyan,
                                    fontSize = 11.5.sp
                                )
                            }
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

@Composable
fun HomeContent(
    uiState: SaveUiState,
    modifier: Modifier = Modifier,
    onOpenFile: () -> Unit = {},
    onManualBackup: () -> Unit = {},
    onRestoreBackup: (BackupEntry) -> Unit = {},
    onDeleteBackup: (BackupEntry) -> Unit = {},
    onDeleteAllBackups: () -> Unit = {},
    onRefreshBackups: () -> Unit = {},
    onSaveFile: () -> Unit = {},
    showGuideDialog: Boolean = false,
    onToggleGuideDialog: (Boolean) -> Unit = {},
    showRestoreConfirmDialog: BackupEntry? = null,
    onConfirmRestore: (BackupEntry) -> Unit = {},
    onDismissRestoreConfirm: () -> Unit = {},
    showDeleteConfirmDialog: BackupEntry? = null,
    onConfirmDelete: (BackupEntry) -> Unit = {},
    onDismissDeleteConfirm: () -> Unit = {},
    showDeleteAllConfirmDialog: Boolean = false,
    onConfirmDeleteAll: () -> Unit = {},
    onDismissDeleteAllConfirm: () -> Unit = {},
    onNavigateTab: (Int) -> Unit = {}
) {
    val compact = isCompact()
    val isFileLoaded = uiState.chapterFile != null || uiState.globalFile != null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding()),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ==================== 1. YEDEK ALMAYI UNUTMA! (Genişletilebilir Rehber Kartı) ====================
        item {
            var isGuideExpanded by remember { mutableStateOf(false) }
            val arrowRotation by animateFloatAsState(
                targetValue = if (isGuideExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "guideArrowRotation"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)),
                color = AwakeningNavySurface,
                border = BorderStroke(
                    1.dp,
                    if (isGuideExpanded) AwakeningGold.copy(alpha = 0.55f) else AwakeningWarning.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGuideExpanded = !isGuideExpanded }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AwakeningWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Yedek Almayı Unutma!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AwakeningGoldBright,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (isGuideExpanded) "Kayıt dosyası ve konum ipuçları" else "Değişiklik öncesi orijinal dosyanızın yedeğini saklayın.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AwakeningTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isGuideExpanded) AwakeningGold.copy(alpha = 0.18f) else AwakeningDarkBg,
                            border = BorderStroke(1.dp, if (isGuideExpanded) AwakeningGold else AwakeningBorderSubtle),
                            modifier = Modifier.clickable { isGuideExpanded = !isGuideExpanded }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = AwakeningGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rehber",
                                    color = AwakeningGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isGuideExpanded) "Daralt" else "Genişlet",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .graphicsLayer { rotationZ = arrowRotation },
                                    tint = AwakeningGold
                                )
                            }
                        }
                    }

                    if (isGuideExpanded) {
                        HorizontalDivider(
                            color = AwakeningBorderSubtle.copy(alpha = 0.6f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Dosya Yuvaları
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AwakeningDarkBg,
                                    border = BorderStroke(0.8.dp, AwakeningBorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text("Dosya Yuvaları", color = AwakeningGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                        Text("• chapter0-2 : Yuva 1, 2, 3", color = AwakeningTextPrimary, fontSize = 10.5.sp)
                                        Text("• chapter3 : Savaş Kaydı", color = AwakeningTextPrimary, fontSize = 10.5.sp)
                                    }
                                }

                                // 2. Nerede Bulunur?
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = AwakeningDarkBg,
                                    border = BorderStroke(0.8.dp, AwakeningBorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text("Dosya Konumu", color = AwakeningGoldBright, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        }
                                        Text("Citra / Lime3DS / Azahar:", color = AwakeningTextSecondary, fontSize = 9.5.sp)
                                        Text("sdmc / Nintendo 3DS", color = AwakeningTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                                    }
                                }
                            }

                            // 3. Otomatik Yedek Bilgisi
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = AwakeningNavySurface.copy(alpha = 0.6f),
                                border = BorderStroke(0.6.dp, AwakeningBorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Uygulama her kaydetmede otomatik '.bak' yedeği alır; dilediğiniz an tek tıkla geri yükleyebilirsiniz.",
                                        color = AwakeningTextSecondary,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 2. SAVE DOSYASI SEÇ ====================
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (!isFileLoaded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = AwakeningGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Save Dosyası Seç",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AwakeningTextPrimary,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = "Chapter0/1/2 veya global",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AwakeningTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenFile,
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AwakeningRoyalBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Aç", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AwakeningSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = uiState.currentFileName ?: "Kayıt Dosyası",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AwakeningGoldBright,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = uiState.slotInfo?.displayName ?: if (uiState.isModified) "Değişiklikler var" else "Hazır",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.isModified) AwakeningCrimson else AwakeningTextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = onSaveFile,
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.isModified) AwakeningRoyalBlue else AwakeningCardSurface,
                                        contentColor = if (uiState.isModified) Color.White else AwakeningTextPrimary
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kaydet", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = onOpenFile,
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningTextSecondary),
                                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Farklı Seç", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 3. GÜVENLİK YEDEKLERİ ====================
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = AwakeningGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Güvenlik Yedekleri",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AwakeningTextPrimary,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(text = "${uiState.backups.size}")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isFileLoaded) {
                                TextButton(
                                    onClick = onManualBackup,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = AwakeningGold)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Yedek Al", color = AwakeningGold, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (uiState.backups.isNotEmpty()) {
                                TextButton(
                                    onClick = onDeleteAllBackups,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Tümünü Sil",
                                        tint = AwakeningCrimson,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Temizle", color = AwakeningCrimson, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                            }

                            IconButton(
                                onClick = onRefreshBackups,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Yenile",
                                    tint = AwakeningTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if (uiState.backups.isEmpty()) {
                        Text(
                            text = "Henüz yedek bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AwakeningTextTertiary,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.backups.forEach { backup ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = AwakeningCardSurface,
                                    border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = backup.originalName,
                                                fontWeight = FontWeight.Medium,
                                                color = AwakeningTextPrimary,
                                                fontSize = 12.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${backup.formattedDate} • ${backup.sizeFormatted}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AwakeningTextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(
                                                onClick = { onRestoreBackup(backup) },
                                                modifier = Modifier.height(26.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = AwakeningFalchionBlue
                                                ),
                                                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                                                shape = RoundedCornerShape(5.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text("Yükle", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { onDeleteBackup(backup) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Yedeği Sil",
                                                    tint = AwakeningCrimson,
                                                    modifier = Modifier.size(15.dp)
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

    // ==================== DIALOGS ====================

    // Restore Confirmation Dialog
    showRestoreConfirmDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = onDismissRestoreConfirm,
            title = {
                Text(
                    "Yedek Geri Yüklensin mi?",
                    fontWeight = FontWeight.Bold,
                    color = AwakeningGoldBright
                )
            },
            text = {
                Text(
                    "Seçilen yedek (${backup.originalName} - ${backup.formattedDate}) düzenleme ekranına yüklenecektir. Mevcut kaydedilmemiş değişiklikler kaybolabilir.",
                    color = AwakeningTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmRestore(backup) },
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningRoyalBlue, contentColor = Color.White)
                ) {
                    Text("Evet, Yükle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRestoreConfirm) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }

    // Single Backup Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirm,
            title = {
                Text(
                    "Yedek Silinsin mi?",
                    fontWeight = FontWeight.Bold,
                    color = AwakeningCrimson
                )
            },
            text = {
                Text(
                    "\"${backup.originalName}\" (${backup.formattedDate}) güvenlik yedeği cihazınızdan kalıcı olarak silinecektir. Bu işlem geri alınamaz.",
                    color = AwakeningTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmDelete(backup) },
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCrimson, contentColor = Color.White)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteConfirm) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }

    // Delete All Backups Confirmation Dialog
    if (showDeleteAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteAllConfirm,
            title = {
                Text(
                    "Tüm Yedekler Silinsin mi?",
                    fontWeight = FontWeight.Bold,
                    color = AwakeningCrimson
                )
            },
            text = {
                Text(
                    "Kayıtlı olan tüm (${uiState.backups.size}) güvenlik yedeği kalıcı olarak silinecektir. Bu işlem geri alınamaz.",
                    color = AwakeningTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeleteAll,
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCrimson, contentColor = Color.White)
                ) {
                    Text("Tümünü Temizle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteAllConfirm) {
                    Text("İptal", color = AwakeningTextSecondary)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }
}

@Composable
private fun FormatChip(text: String) {
    Surface(
        color = AwakeningCardSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, AwakeningBorderSubtle)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AwakeningGold,
            fontSize = 10.5.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}


@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AwakeningTextSecondary,
            fontSize = 10.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AwakeningGoldBright,
            fontSize = 13.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1 && result != null) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

@Preview(showBackground = true, backgroundColor = 0xFF080C14)
@Composable
fun HomeScreenPreview() {
    FEAWSETheme {
        HomeContent(
            uiState = SaveUiState(),
            onOpenFile = {},
            onManualBackup = {},
            onRestoreBackup = {},
            onDeleteBackup = {},
            onDeleteAllBackups = {},
            onRefreshBackups = {},
            showGuideDialog = false,
            onToggleGuideDialog = {}
        )
    }
}
