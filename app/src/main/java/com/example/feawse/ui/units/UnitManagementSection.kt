package com.example.feawse.ui.units

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.feawse.savefile.units.Unit as SaveUnit
import com.example.feawse.theme.*
import com.example.feawse.ui.components.CollapsibleCard
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState
import java.io.File

@Composable
fun UnitManagementSection(
    unit: SaveUnit,
    viewModel: SaveFileViewModel,
    uiState: SaveUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMoveGroupConfirmDialog by remember { mutableStateOf<Pair<Int, String>?>(null) }

    // SAF CreateDocument launcher for .fe13u export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportUnitToUri(context, unit, uri)
        }
    }

    CollapsibleCard(
        title = "Birim Yönetimi",
        subtitle = "Kopyala, sırala, gruba taşı veya dışa aktar",
        icon = Icons.Default.ManageAccounts,
        initialExpanded = false,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Duplicate & Reorder Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Ordu Sıralaması & Kopyalama",
                        color = AwakeningTextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.duplicateUnit(unit) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningGold,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Birimi Kopyala",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { viewModel.reorderUnit(unit, moveUp = true) },
                            modifier = Modifier
                                .weight(0.85f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningTextPrimary
                            ),
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(13.dp), tint = AwakeningGold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Yukarı",
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { viewModel.reorderUnit(unit, moveUp = false) },
                            modifier = Modifier
                                .weight(0.85f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningTextPrimary
                            ),
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(13.dp), tint = AwakeningGold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Aşağı",
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 2. Move Unit to Another Group
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Birimi Başka Gruba Taşı",
                        color = AwakeningTextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val groups = listOf(
                        Triple(0, "Mavi (Harita)", AwakeningFalchionBlue),
                        Triple(3, "Ana Kadro", AwakeningGold),
                        Triple(1, "Düşman (Kırmızı)", AwakeningCrimson),
                        Triple(2, "NPC (Yeşil)", AwakeningSuccess),
                        Triple(4, "Ölü Birimler", AwakeningTextTertiary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        groups.forEach { (groupId, name, accentColor) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .clickable {
                                        showMoveGroupConfirmDialog = groupId to name
                                    },
                                color = AwakeningNavySurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name.split(" ").first(),
                                        color = accentColor,
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
                }
            }

            // 3. Export (.fe13u) & Share
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningCardSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, AwakeningBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Dışa Aktarma & Paylaşım (.fe13u)",
                        color = AwakeningTextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val cleanName = unit.unitName().replace(Regex("[^a-zA-Z0-9_]"), "_")
                                exportLauncher.launch("$cleanName.fe13u")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningFalchionBlue,
                                contentColor = AwakeningTextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dışa Aktar (.fe13u)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                shareUnitFile(context, unit)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AwakeningNavySurface,
                                contentColor = AwakeningTextPrimary
                            ),
                            border = BorderStroke(1.dp, AwakeningBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp), tint = AwakeningGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Birim Paylaş",
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Delete Unit
            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AwakeningCrimson),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AwakeningCrimson.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bu Birimi Ordudan Sil",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Move Group Confirmation Dialog
    showMoveGroupConfirmDialog?.let { (groupId, groupName) ->
        AlertDialog(
            onDismissRequest = { showMoveGroupConfirmDialog = null },
            title = {
                Text(
                    text = "Gruba Taşı",
                    color = AwakeningGoldBright,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "${unit.unitName()} birimini $groupName grubuna taşımak istediğinizden emin misiniz?",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.moveUnitToGroup(unit, groupId)
                        showMoveGroupConfirmDialog = null
                    },
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Taşı", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMoveGroupConfirmDialog = null },
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("İptal", color = AwakeningTextSecondary, fontSize = 11.sp)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // Delete Unit Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Birimi Sil",
                    color = AwakeningCrimson,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "${unit.unitName()} birimi kayıt dosyasından kalıcı olarak silinecek. Bu işlem geri alınamaz!\n\nOnaylıyor musunuz?",
                    color = AwakeningTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteUnit(unit) {
                            onBack()
                        }
                    },
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningCrimson, contentColor = AwakeningTextPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Evet, Sil", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Vazgeç", color = AwakeningTextSecondary, fontSize = 11.sp)
                }
            },
            containerColor = AwakeningNavySurface,
            shape = RoundedCornerShape(10.dp)
        )
    }
}

private fun shareUnitFile(context: Context, unit: SaveUnit) {
    try {
        val cleanName = unit.unitName().replace(Regex("[^a-zA-Z0-9_]"), "_")
        val cacheDir = File(context.cacheDir, "exported_units")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val tempFile = File(cacheDir, "$cleanName.fe13u")
        tempFile.writeBytes(unit.getUnitBytes())

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "FE:A Birim - ${unit.unitName()}")
            putExtra(Intent.EXTRA_TEXT, "${unit.unitName()} Fire Emblem Awakening birim verisi (.fe13u)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Birimi Paylaş"))
    } catch (e: Exception) {
        // Fallback simple share
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${unit.unitName()} birimi dışa aktarıldı.")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Birimi Paylaş"))
    }
}
