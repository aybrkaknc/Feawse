package com.example.feawse.ui.cheats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feawse.theme.*
import com.example.feawse.ui.util.adaptivePadding
import com.example.feawse.ui.util.isCompact
import com.example.feawse.viewmodel.SaveFileViewModel
import com.example.feawse.viewmodel.SaveUiState

@Composable
fun CheatScreen(
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

    var confirmDialogAction by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = adaptivePadding()),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AwakeningNavySurface,
                border = BorderStroke(1.dp, AwakeningBorderSubtle),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Toplu Hile ve Optimizasyon: Tek tıkla ordunuza güçlendirme uygulayın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AwakeningTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            CheatItemCard(
                title = "Tüm Oyuncu Birimlerini Maksla (Legal Max)",
                description = "Oyuncu ordusundaki tüm karakterlerin seviyesini, silah deneyimlerini ve sınıf tavanlarına uyan istatistiklerini güvenle maksimuma çıkarır.",
                icon = Icons.Default.Verified,
                buttonText = "Uygula",
                onClick = {
                    confirmDialogAction = "Tüm oyuncu birimlerinin istatistikleri yasal tavana ulaştırılacak. Devam edilsin mi?" to {
                        viewModel.batchMaxPlayableStats()
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Destek Bağlarını 'S' Seviyesine Çıkar",
                description = "Ordudaki tüm karakterlerin birbiriyle olan bağını maksimuma çıkarır (evlilikler ve çocuk kilitleri için idealdir).",
                icon = Icons.Default.Favorite,
                buttonText = "Uygula",
                onClick = {
                    confirmDialogAction = "Tüm destek bağları S seviyesine yükseltilecek. Devam edilsin mi?" to {
                        viewModel.batchMaxSupports()
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Yasal Sınıf Yeteneklerini Aç",
                description = "Birimlerin sınıflarına göre öğrenebilecekleri tüm yasal yetenekleri (Skill) anında açar.",
                icon = Icons.Default.AutoAwesome,
                buttonText = "Uygula",
                onClick = {
                    confirmDialogAction = "Tüm yasal yetenekler açılacak. Devam edilsin mi?" to {
                        viewModel.batchUnlockLegalSkills()
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Konvoy Eşyalarını Tamir Et (99 Kullanım)",
                description = "Çantanızdaki ve deponuzdaki bütün kırık veya tükenmiş eşyaların dayanıklılığını 99 yapar.",
                icon = Icons.Default.Build,
                buttonText = "Tamir Et",
                onClick = {
                    viewModel.repairAllConvoy()
                }
            )
        }

        item {
            CheatItemCard(
                title = "Kışla & Tonik Güçlendirmelerini Aktifleştir",
                description = "Tüm karakterlere geçici stat artışları, kışla buffları ve saf su etkilerini ekler.",
                icon = Icons.Default.LocalHospital,
                buttonText = "Aktifleştir",
                onClick = {
                    viewModel.batchEnableBuffs()
                }
            )
        }

        item {
            CheatItemCard(
                title = "Maksimum Zenginlik (Altın & Şöhret)",
                description = "Parayı 999,999 Altına, Şöhret puanını (Renown) ise 99,999 puana tamamlar.",
                icon = Icons.Default.MonetizationOn,
                buttonText = "Zenginleş",
                onClick = {
                    viewModel.setMoney(999999)
                    viewModel.setRenown(99999)
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tanrı Modu (God Mode All - Nihai Hile)",
                description = "Tüm oyuncu birimlerini maksimum statlara, tüm yasal yeteneklere, S seviyesi bağlara, +2 Harekete ve tüm geçici bufflara kavuşturur.",
                icon = Icons.Default.Bolt,
                buttonText = "Tanrı Modu",
                onClick = {
                    confirmDialogAction = "Tüm orduya Tanrı Modu (Maks statlar, yetenekler, S destekler, +2 hareket ve tüm bufflar) uygulanacak. Devam edilsin mi?" to {
                        viewModel.batchGodAndPray()
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Orduya +2 Hareket (Boots Bonusu)",
                description = "Tüm oynanabilir birimlerin haritada hareket edebileceği kare sayısını kalıcı olarak 2 artırır.",
                icon = Icons.Default.Speed,
                buttonText = "+2 Hareket",
                onClick = {
                    confirmDialogAction = "Tüm oyuncu birimlerine +2 Hareket eklenecek. Devam edilsin mi?" to {
                        viewModel.batchAddMovement(2)
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Depo Eşyalarını 99 Adet Yap (Stok)",
                description = "Konvoydaki tüm standart ve dövülmüş silah/eşyaların stok adedini 99'a çıkarır.",
                icon = Icons.Default.Inventory2,
                buttonText = "99 Stok",
                onClick = {
                    confirmDialogAction = "Tüm konvoy eşyalarının adedi 99 yapılacak. Devam edilsin mi?" to {
                        viewModel.batchSetConvoyAmount(99)
                    }
                }
            )
        }

        item {
            CheatItemCard(
                title = "Tüm Karakter Savaş & Zaferlerini 100 Yap",
                description = "Tüm ordunun savaş tecrübesi kayıtlarını 100 savaş ve 100 zafer olarak eşitler.",
                icon = Icons.Default.EmojiEvents,
                buttonText = "100 Zafer",
                onClick = {
                    confirmDialogAction = "Tüm karakterlerin savaş ve zafer sayıları 100 olarak ayarlanacak. Devam edilsin mi?" to {
                        viewModel.batchSetBattlesVictories(100)
                    }
                }
            )
        }
    }

    confirmDialogAction?.let { (msg, action) ->
        AlertDialog(
            onDismissRequest = { confirmDialogAction = null },
            title = { Text("İşlemi Onaylayın", color = AwakeningGoldBright) },
            text = { Text(msg, color = AwakeningTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        action()
                        confirmDialogAction = null
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
                    onClick = { confirmDialogAction = null },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text("İptal", color = AwakeningTextSecondary, fontSize = 11.5.sp, maxLines = 1, textAlign = TextAlign.Center)
                }
            },
            containerColor = AwakeningNavySurface
        )
    }
}

@Composable
private fun CheatItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    val compact = isCompact()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AwakeningNavySurface,
        border = BorderStroke(1.dp, AwakeningBorderSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (compact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.SemiBold, color = AwakeningTextPrimary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AwakeningTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = AwakeningGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, color = AwakeningTextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall, color = AwakeningTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AwakeningGold, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
