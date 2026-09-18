package com.example.feawse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feawse.data.ItemDb
import com.example.feawse.savefile.Batch
import com.example.feawse.savefile.Chapter13
import com.example.feawse.savefile.Constants
import com.example.feawse.savefile.SaveFile
import com.example.feawse.savefile.inventory.Refinement
import com.example.feawse.savefile.units.Stats
import com.example.feawse.savefile.units.Unit
import com.example.feawse.util.BackupEntry
import com.example.feawse.util.BackupManager
import com.example.feawse.util.EmulatorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.feawse.savefile.global.Global
import com.example.feawse.savefile.wireless.UnitDu
import java.io.ByteArrayOutputStream

data class SaveUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val chapterFile: Chapter13? = null,
    val globalFile: Global? = null,
    val currentFileName: String? = null,
    val currentUri: Uri? = null,
    val slotInfo: EmulatorHelper.SlotInfo? = null,
    val regionInfo: EmulatorHelper.RegionDetection? = null,
    val isModified: Boolean = false,
    val modificationCount: Long = 0L,
    val canUndoMap: Boolean = false,
    val backups: List<BackupEntry> = emptyList(),
    val selectedUnit: Unit? = null,
    val activeTab: Int = 0 // 0: Home, 1: Units, 2: Convoy, 3: Progress, 4: Cheats
)

class SaveFileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SaveUiState())
    val uiState: StateFlow<SaveUiState> = _uiState.asStateFlow()

    fun loadBackups(context: Context) {
        val backups = BackupManager.listBackups(context)
        _uiState.update { it.copy(backups = backups) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectUnit(unit: Unit?) {
        _uiState.update { it.copy(selectedUnit = unit) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun openSaveFile(file: java.io.File, context: Context) {
        if (!file.exists()) return
        openSaveFile(Uri.fromFile(file), file.name, context)
    }

    /**
     * Reads a save file from Uri, creates an automatic backup for safety, and parses Chapter13.
     */
    fun openSaveFile(uri: Uri, filename: String, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rawBytes = withContext(Dispatchers.IO) {
                    if (uri.scheme == "file" && uri.path != null) {
                        java.io.File(uri.path!!).readBytes()
                    } else {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val buffer = ByteArrayOutputStream()
                            val chunk = ByteArray(8192)
                            var read: Int
                            while (stream.read(chunk).also { read = it } != -1) {
                                buffer.write(chunk, 0, read)
                            }
                            buffer.toByteArray()
                        } ?: throw IllegalStateException("Dosya açılamadı!")
                    }
                }

                // Create automatic safety backup
                val backupFile = withContext(Dispatchers.IO) {
                    BackupManager.createBackup(context, filename, rawBytes)
                }

                val region = EmulatorHelper.detectRegion(rawBytes, filename)
                val slot = EmulatorHelper.getSlotInfo(filename)

                if (region.isGlobal || slot.isGlobal) {
                    val global = withContext(Dispatchers.Default) {
                        Global(rawBytes)
                    }
                    loadBackups(context)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterFile = null,
                            globalFile = global,
                            currentFileName = filename,
                            currentUri = uri,
                            slotInfo = slot,
                            regionInfo = region,
                            isModified = false,
                            selectedUnit = null,
                            activeTab = 0,
                            infoMessage = "Genel sistem kaydı (global) başarıyla yüklendi! Otomatik güvenlik yedeği alındı: ${backupFile?.name ?: "Yedeklendi"}"
                        )
                    }
                } else {
                    val chapter13 = withContext(Dispatchers.Default) {
                        Chapter13(rawBytes)
                    }
                    loadBackups(context)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterFile = chapter13,
                            globalFile = null,
                            currentFileName = filename,
                            currentUri = uri,
                            slotInfo = slot,
                            regionInfo = region,
                            isModified = false,
                            selectedUnit = null,
                            infoMessage = "Kayıt başarıyla yüklendi! Otomatik güvenlik yedeği alındı: ${backupFile?.name ?: "Yedeklendi"}"
                        )
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Kayıt dosyası açılırken hata oluştu: ${e.localizedMessage ?: e.message}"
                    )
                }
            }
        }
    }

    /**
     * Saves the current modified state back to Uri with automatic backup.
     */
    fun saveFile(context: Context, targetUri: Uri? = null) {
        val chapter = _uiState.value.chapterFile
        val global = _uiState.value.globalFile
        if (chapter == null && global == null) return
        val uri = targetUri ?: _uiState.value.currentUri ?: return
        val filename = _uiState.value.currentFileName ?: if (global != null) "global" else "chapter0"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Compress bytes in background thread
                val compressedBytes = withContext(Dispatchers.Default) {
                    if (global != null) {
                        global.bytesComp
                    } else {
                        chapter!!.bytesComp
                    }
                }

                // Create safety backup before overwriting
                withContext(Dispatchers.IO) {
                    BackupManager.createBackup(context, "${filename}_preSave", compressedBytes)
                    context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                        output.write(compressedBytes)
                        output.flush()
                    } ?: throw IllegalStateException("Dosyaya yazılamadı!")
                }

                loadBackups(context)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isModified = false,
                        infoMessage = "Dosya başarıyla kaydedildi! (${if (global != null) "global" else filename})"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Kaydetme sırasında hata: ${e.localizedMessage ?: e.message}"
                    )
                }
            }
        }
    }

    fun restoreBackup(entry: BackupEntry, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bytes = withContext(Dispatchers.IO) {
                    BackupManager.readBackupBytes(entry) ?: throw IllegalStateException("Yedek dosyası okunamadı")
                }
                val region = EmulatorHelper.detectRegion(bytes, entry.originalName)
                val slot = EmulatorHelper.getSlotInfo(entry.originalName)

                if (region.isGlobal || slot.isGlobal) {
                    val global = withContext(Dispatchers.Default) {
                        Global(bytes)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterFile = null,
                            globalFile = global,
                            currentFileName = entry.originalName,
                            slotInfo = slot,
                            regionInfo = region,
                            isModified = true,
                            selectedUnit = null,
                            activeTab = 0,
                            infoMessage = "Genel sistem yedeği başarıyla geri yüklendi: ${entry.file.name}"
                        )
                    }
                } else {
                    val chapter13 = withContext(Dispatchers.Default) {
                        Chapter13(bytes)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterFile = chapter13,
                            globalFile = null,
                            currentFileName = entry.originalName,
                            slotInfo = slot,
                            regionInfo = region,
                            isModified = true,
                            selectedUnit = null,
                            infoMessage = "Yedek başarıyla geri yüklendi: ${entry.file.name}"
                        )
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Yedek yüklenirken hata: ${e.localizedMessage ?: e.message}"
                    )
                }
            }
        }
    }

    fun createManualBackup(context: Context) {
        val chapter = _uiState.value.chapterFile
        val global = _uiState.value.globalFile
        if (chapter == null && global == null) return
        val filename = _uiState.value.currentFileName ?: if (global != null) "global" else "chapter0"
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.Default) {
                    if (global != null) global.bytesComp else chapter!!.bytesComp
                }
                val file = withContext(Dispatchers.IO) {
                    BackupManager.createBackup(context, "${filename}_manual", bytes)
                }
                loadBackups(context)
                _uiState.update {
                    it.copy(infoMessage = "Manuel güvenlik yedeği oluşturuldu: ${file?.name}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Yedek alınamadı: ${e.message}") }
            }
        }
    }

    fun deleteBackup(entry: BackupEntry, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                BackupManager.deleteBackup(entry)
            }
            loadBackups(context)
            _uiState.update { it.copy(infoMessage = "Yedek silindi: ${entry.file.name}") }
        }
    }

    fun deleteAllBackups(context: Context) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                BackupManager.deleteAllBackups(context)
            }
            loadBackups(context)
            _uiState.update { it.copy(infoMessage = "$count adet güvenlik yedeği silindi.") }
        }
    }


    private fun notifyModified(infoMsg: String? = null) {
        _uiState.update {
            it.copy(
                isModified = true,
                modificationCount = it.modificationCount + 1,
                infoMessage = infoMsg ?: it.infoMessage
            )
        }
    }

    // --- Unit Operations ---

    fun setUnitLegalMax(unit: Unit) {
        Stats.setMaxStatsHigh(unit)
        notifyModified()
    }

    fun setUnitGodMode(unit: Unit) {
        Stats.setGodModeStats(unit)
        notifyModified()
    }

    fun unlockAllUnitSkills(unit: Unit) {
        unit.rawSkill.setAll(true)
        notifyModified()
    }

    fun setUnitClass(unit: Unit, classId: Int) {
        unit.rawBlock1.setUnitClass(classId)
        notifyModified()
    }

    fun setUnitLevel(unit: Unit, level: Int, exp: Int) {
        unit.rawBlock1.setLevel(level)
        unit.rawBlock1.setExp(exp)
        notifyModified()
    }

    fun setUnitCurrentHp(unit: Unit, hp: Int) {
        unit.rawBlock1.setCurrentHp(hp)
        notifyModified()
    }

    fun setUnitStatGrowth(unit: Unit, statIndex: Int, value: Int) {
        if (statIndex in 0..7) {
            unit.rawBlock1.setGrowth(value, statIndex)
            notifyModified()
        }
    }

    fun setUnitEquippedSkill(unit: Unit, slot: Int, skillId: Int) {
        if (slot in 0..4) {
            unit.rawBlock2.setCurrentSkill(skillId, slot)
            notifyModified()
        }
    }

    fun setUnitWeaponRankExp(unit: Unit, weaponIndex: Int, exp: Int) {
        if (weaponIndex in 0..5) {
            unit.rawBlock2.setWeaponExp(exp, weaponIndex)
            notifyModified()
        }
    }

    fun setUnitEquippedItem(unit: Unit, slot: Int, itemId: Int, uses: Int) {
        if (slot in 0..4 && unit.rawInventory != null && unit.rawInventory.items.size > slot) {
            val item = unit.rawInventory.items[slot]
            item.setItemId(itemId)
            item.setUses(uses)
            notifyModified()
        }
    }

    // --- Support Operations ---

    fun setUnitSupportLevel(unit: Unit, slot: Int, level: Int, sync: Boolean = true) {
        val chapter = _uiState.value.chapterFile ?: return
        unit.rawSupport?.expandBlock()
        unit.rawSupport?.setSupportLevel(slot, level)

        if (sync) {
            val unitId = unit.rawBlock1.unitId()
            val validUnits = com.example.feawse.data.UnitDb.getUnitSupportUnits(unitId)
            if (slot in validUnits.indices) {
                val partnerId = validUnits[slot]
                val allPlayerUnits = (chapter.blockUnit?.unitList?.getOrNull(3).orEmpty() +
                        chapter.blockUnit?.unitList?.getOrNull(0).orEmpty())
                for (partnerUnit in allPlayerUnits) {
                    if (partnerUnit.rawBlock1.unitId() == partnerId) {
                        partnerUnit.rawSupport?.expandBlock()
                        partnerUnit.rawSupport?.setSupportLevelByUnit(unitId, level)
                    }
                }
            }
        }
        notifyModified()
    }

    fun setUnitSupportPoints(unit: Unit, slot: Int, points: Int, sync: Boolean = true) {
        val chapter = _uiState.value.chapterFile ?: return
        unit.rawSupport?.expandBlock()
        unit.rawSupport?.setSupportValue(slot, points)

        if (sync) {
            val unitId = unit.rawBlock1.unitId()
            val validUnits = com.example.feawse.data.UnitDb.getUnitSupportUnits(unitId)
            if (slot in validUnits.indices) {
                val partnerId = validUnits[slot]
                val allPlayerUnits = (chapter.blockUnit?.unitList?.getOrNull(3).orEmpty() +
                        chapter.blockUnit?.unitList?.getOrNull(0).orEmpty())
                for (partnerUnit in allPlayerUnits) {
                    if (partnerUnit.rawBlock1.unitId() == partnerId) {
                        partnerUnit.rawSupport?.expandBlock()
                        partnerUnit.rawSupport?.setSupportValueByUnit(unitId, points)
                    }
                }
            }
        }
        notifyModified()
    }

    fun setAllUnitSupports(unit: Unit, level: Int, sync: Boolean = true) {
        val chapter = _uiState.value.chapterFile ?: return
        unit.rawSupport?.expandBlock()
        val allPlayerUnits = (chapter.blockUnit?.unitList?.getOrNull(3).orEmpty() +
                chapter.blockUnit?.unitList?.getOrNull(0).orEmpty())
        val playerUnitIds = allPlayerUnits.map { it.rawBlock1.unitId() }

        unit.rawSupport?.setAllSupportsTo(level, playerUnitIds)
        if (level == 0) {
            unit.rawSupport?.removeExtraSupports()
        }

        if (sync) {
            val unitId = unit.rawBlock1.unitId()
            val validUnits = com.example.feawse.data.UnitDb.getUnitSupportUnits(unitId)
            for (partnerId in validUnits) {
                for (partnerUnit in allPlayerUnits) {
                    if (partnerUnit.rawBlock1.unitId() == partnerId) {
                        partnerUnit.rawSupport?.expandBlock()
                        partnerUnit.rawSupport?.setSupportLevelByUnit(unitId, level)
                    }
                }
            }
        }
        notifyModified()
    }

    fun setChromVillageMaiden(unit: Unit, enabled: Boolean) {
        unit.rawFlags?.setBattleFlag(22, enabled)
        notifyModified()
    }

    fun reviveUnit(unit: Unit) {
        unit.revive()
        notifyModified("${unit.unitName()} birimi canlandırıldı!")
    }

    fun killUnit(unit: Unit) {
        unit.kill()
        notifyModified("${unit.unitName()} birimi ölü/emekli olarak işaretlendi.")
    }

    fun setUnitBattles(unit: Unit, count: Int) {
        unit.rawBlockEnd.setBattles(count.coerceIn(0, 65535))
        notifyModified()
    }

    fun setUnitVictories(unit: Unit, count: Int) {
        unit.rawBlockEnd.setVictories(count.coerceIn(0, 65535))
        notifyModified()
    }

    fun setUnitTonic(unit: Unit, index: Int, enabled: Boolean) {
        unit.rawFlags.setTonicFlag(index, enabled)
        notifyModified()
    }

    fun setAllUnitTonics(unit: Unit, enabled: Boolean) {
        unit.rawFlags.setAllTonicFlags(enabled)
        notifyModified()
    }

    fun setUnitPureWater(unit: Unit, value: Int) {
        unit.rawFlags.setResBuff(value.coerceIn(0, 255))
        notifyModified()
    }

    fun setDlcTurn(slot: Int, turns: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockDu26?.setDlcTurn(slot, turns.coerceIn(0, 255))
        notifyModified()
    }

    fun setDuelScore(slot: Int, score: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockDu26?.setDuelScore(slot, score.coerceIn(0, 255))
        notifyModified()
    }

    fun setDuelBeaten(slot: Int, beaten: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockDu26?.setDuelBeaten(slot, beaten)
        notifyModified()
    }

    fun setAllDuelsBeaten(beaten: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        for (i in 0 until 22) {
            du.setDuelBeaten(i, beaten)
        }
        notifyModified(if (beaten) "Tüm ikili düellolar kazanıldı olarak işaretlendi." else "Düello kazanma durumları sıfırlandı.")
    }

    fun setAllDuelScores(score: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        for (i in 0 until 22) {
            du.setDuelScore(i, score.coerceIn(0, 255))
        }
        notifyModified("Tüm düello skorları $score olarak ayarlandı.")
    }

    fun setAllDlcTurns(turns: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        for (i in 0 until 25) {
            du.setDlcTurn(i, turns.coerceIn(0, 255))
        }
        notifyModified("Tüm DLC tur sayıları $turns olarak ayarlandı.")
    }

    fun setUnitBarrackBuff(unit: Unit, slot: Int, enabled: Boolean) {
        unit.rawFlags.setBarrackFlag(slot, enabled)
        notifyModified()
    }

    fun setAllUnitBarrackBuffs(unit: Unit, enabled: Boolean) {
        unit.rawFlags.setAllBarrackFlags(enabled)
        notifyModified(if (enabled) "Kışla güçlendirmeleri uygulandı." else "Kışla güçlendirmeleri temizlendi.")
    }

    fun setUnitMovement(unit: Unit, movement: Int) {
        unit.rawBlock1.setMovement(movement.coerceIn(0, 15))
        notifyModified()
    }

    fun setUnitHiddenLevel(unit: Unit, level: Int) {
        unit.rawFlags.setHiddenLevel(level.coerceIn(0, 255))
        notifyModified()
    }

    fun setUnitArmy(unit: Unit, army: Int) {
        unit.rawFlags.setArmy(army.coerceIn(0, 255))
        notifyModified()
    }

    fun setUnitCoordinates(unit: Unit, x: Int, y: Int) {
        unit.rawBlock1.setCoordinates1(x.coerceIn(0, 255), y.coerceIn(0, 255))
        notifyModified()
    }

    fun setUnitDeploySlot(unit: Unit, slot: Int) {
        unit.rawFlags.setSlotParty(slot.coerceIn(0, 255))
        notifyModified()
    }

    fun setUnitTraitFlag(unit: Unit, flag: Int, enabled: Boolean) {
        unit.rawFlags.setTraitFlag(flag, enabled)
        notifyModified()
    }

    fun setUnitBattleFlag(unit: Unit, flag: Int, enabled: Boolean) {
        unit.rawFlags.setBattleFlag(flag, enabled)
        notifyModified()
    }

    fun setUnitSkillBuff(unit: Unit, slot: Int, enabled: Boolean) {
        unit.rawFlags.setSkillBuffFlag(slot, enabled)
        notifyModified()
    }

    fun setAllUnitSkillBuffs(unit: Unit, enabled: Boolean) {
        for (i in 0 until 11) {
            unit.rawFlags.setSkillBuffFlag(i, enabled)
        }
        notifyModified(if (enabled) "Tüm beceri güçlendirmeleri uygulandı." else "Beceri güçlendirmeleri temizlendi.")
    }

    fun setUnitAiType(unit: Unit, slot: Int, value: Int) {
        unit.rawBlockEnd.setAiType(slot, value.coerceIn(0, 255))
        notifyModified()
    }

    fun setUnitAiParam(unit: Unit, aiSlot: Int, paramSlot: Int, value: Int) {
        unit.rawBlockEnd.setAiParam(aiSlot, paramSlot, value.coerceIn(0, 65535))
        notifyModified()
    }

    fun resetUnitAi(unit: Unit) {
        for (i in 0..3) {
            unit.rawBlockEnd.setAiType(i, 0)
            for (p in 0..3) {
                unit.rawBlockEnd.setAiParam(i, p, 0)
            }
        }
        notifyModified("Yapay zeka (AI) ayarları sıfırlandı.")
    }

    fun setChildSupportParent(unit: Unit, isFather: Boolean, level: Int) {
        val child = unit.rawChild ?: return
        val type = 4
        val maxValues = com.example.feawse.data.UnitDb.getSupportValues(type)
        val value = if (level == 0) 0 else maxValues[level - 1]
        child.setSupportParent(isFather, value)
        notifyModified()
    }

    fun setChildSupportSibling(unit: Unit, level: Int) {
        val child = unit.rawChild ?: return
        val type = 4
        val maxValues = com.example.feawse.data.UnitDb.getSupportValues(type)
        val value = if (level == 0) 0 else maxValues[level - 1]
        child.setSupportSibling(value)
        notifyModified()
    }

    fun setChildSupportParentPoints(unit: Unit, isFather: Boolean, points: Int) {
        val child = unit.rawChild ?: return
        child.setSupportParent(isFather, points.coerceIn(0, 22))
        notifyModified()
    }

    fun setChildSupportSiblingPoints(unit: Unit, points: Int) {
        val child = unit.rawChild ?: return
        child.setSupportSibling(points.coerceIn(0, 22))
        notifyModified()
    }

    fun addUnitChildBlock(unit: Unit) {
        unit.addBlockChild()
        notifyModified()
    }

    fun removeUnitChildBlock(unit: Unit) {
        unit.removeBlockExtra(false)
        notifyModified()
    }

    fun setUnitParent(unit: Unit, slot: Int, parentId: Int) {
        val child = unit.rawChild ?: return
        child.setParentId(slot, parentId)
        notifyModified()
    }

    fun setUnitParentAsset(unit: Unit, slot: Int, asset: Int) {
        val child = unit.rawChild ?: return
        child.setAsset(slot, asset)
        notifyModified()
    }

    fun setUnitParentFlaw(unit: Unit, slot: Int, flaw: Int) {
        val child = unit.rawChild ?: return
        child.setFlaw(slot, flaw)
        notifyModified()
    }

    fun setUnitHairColor(unit: Unit, hexColor: String) {
        val clean = if (hexColor.startsWith("#")) hexColor.substring(1) else hexColor
        unit.rawBlockEnd?.setHairColor(clean)
        unit.rawLog?.let { log ->
            try {
                val color = com.example.feawse.util.Hex.hexToColor(clean)
                log.setHairColorFx(color)
            } catch (ignored: Exception) {}
        }
        notifyModified()
    }

    // --- Unit Management & Import / Export ---

    fun duplicateUnit(unit: Unit) {
        val chapter = _uiState.value.chapterFile ?: return
        val block = chapter.blockUnit ?: return
        for (g in block.unitList.indices) {
            val list = block.unitList[g]
            val idx = list.indexOf(unit)
            if (idx != -1) {
                if (list.size < Constants.UNIT_LIMIT) {
                    val clonedBytes = unit.getUnitBytes()
                    val duplicated = Unit(clonedBytes)
                    list.add(idx + 1, duplicated)
                    notifyModified("${unit.unitName()} birimi başarıyla kopyalandı!")
                } else {
                    _uiState.update { it.copy(errorMessage = "Gruptaki birim sayısı sınırına ulaşıldı (${Constants.UNIT_LIMIT})") }
                }
                return
            }
        }
    }

    fun deleteUnit(unit: Unit, onDeleted: () -> kotlin.Unit) {
        val chapter = _uiState.value.chapterFile ?: return
        val block = chapter.blockUnit ?: return
        for (g in block.unitList.indices) {
            val list = block.unitList[g]
            if (list.remove(unit)) {
                notifyModified("${unit.unitName()} birimi silindi.")
                onDeleted()
                return
            }
        }
    }

    fun moveUnitToGroup(unit: Unit, targetGroup: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val block = chapter.blockUnit ?: return
        if (targetGroup !in 0 until block.unitList.size) return
        val targetList = block.unitList[targetGroup]
        if (targetList.size >= Constants.UNIT_LIMIT) {
            _uiState.update { it.copy(errorMessage = "Hedef grupta yer kalmadı!") }
            return
        }

        for (g in block.unitList.indices) {
            val list = block.unitList[g]
            if (list.remove(unit)) {
                if (targetGroup == 4 && g != 4) {
                    unit.kill()
                } else if (targetGroup != 4 && g == 4) {
                    unit.revive()
                }
                targetList.add(unit)
                val groupNames = listOf("Mavi (Harita)", "Düşman (Kırmızı)", "NPC (Yeşil)", "Ana Kadro (Oyuncu)", "Ölü Birimler", "Diğer")
                val targetName = if (targetGroup in groupNames.indices) groupNames[targetGroup] else "Grup $targetGroup"
                notifyModified("${unit.unitName()} -> $targetName grubuna taşındı.")
                return
            }
        }
    }

    fun reorderUnit(unit: Unit, moveUp: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        val block = chapter.blockUnit ?: return
        for (g in block.unitList.indices) {
            val list = block.unitList[g]
            val idx = list.indexOf(unit)
            if (idx != -1) {
                val targetIdx = if (moveUp) idx - 1 else idx + 1
                if (targetIdx in list.indices) {
                    list.removeAt(idx)
                    list.add(targetIdx, unit)
                    notifyModified()
                }
                return
            }
        }
    }

    fun exportUnitToUri(context: Context, unit: Unit, uri: Uri) {
        try {
            val bytes = unit.getUnitBytes()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(bytes)
            }
            notifyModified("${unit.unitName()} birimi başarıyla dışa aktarıldı (.fe13u)!")
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Dışa aktarma hatası: ${e.message}") }
        }
    }

    fun importUnitFromUri(context: Context, uri: Uri) {
        try {
            val chapter = _uiState.value.chapterFile ?: run {
                _uiState.update { it.copy(errorMessage = "Lütfen önce bir save dosyası açın!") }
                return
            }
            val block = chapter.blockUnit ?: return
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            } ?: throw IllegalArgumentException("Dosya okunamadı!")

            val imported = Unit(bytes)
            val targetGroup = if (block.unitList.size > 3 && block.unitList[3].isNotEmpty()) 3 else 0
            if (block.unitList[targetGroup].size < Constants.UNIT_LIMIT) {
                block.unitList[targetGroup].add(imported)
                notifyModified("${imported.unitName()} birimi başarıyla save dosyasına aktarıldı!")
            } else {
                _uiState.update { it.copy(errorMessage = "Birim listesi dolu!") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "İçe aktarma hatası: ${e.message}") }
        }
    }

    // --- Convoy Operations ---

    fun repairAllConvoy() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setConvoyUsesTo(chapter, 99)
        notifyModified("Tüm konvoy eşyaları ve dövülmüş silahlar maksimum kullanıma yenilendi!")
    }

    fun setConvoyItemUses(itemId: Int, uses: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockTran?.setItemUses(itemId, uses)
        notifyModified()
    }

    fun addOrUpdateForgedWeapon(refinement: Refinement, convoyUses: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val refiBlock = chapter.blockRefi ?: return
        val tranBlock = chapter.blockTran ?: return

        val existing = refiBlock.refiList.find { it.position() == refinement.position() }
        if (existing == null) {
            refiBlock.addRefinement(refinement)
        } else if (existing !== refinement) {
            existing.setName(refinement.name)
            existing.setWeaponId(refinement.weaponId())
            existing.setMight(refinement.might())
            existing.setHit(refinement.hit())
            existing.setCrit(refinement.crit())
            existing.setFlagEnemy(refinement.isEnemy())
        }

        if (refinement.position() in 0 until tranBlock.inventoryRefi.size) {
            tranBlock.setForgedUses(refinement.position(), convoyUses)
        }

        notifyModified("Dövülmüş silah kaydedildi: ${refinement.name}")
    }

    fun deleteForgedWeapon(refinement: Refinement) {
        val chapter = _uiState.value.chapterFile ?: return
        val refiBlock = chapter.blockRefi ?: return
        val tranBlock = chapter.blockTran ?: return
        val unitBlock = chapter.blockUnit

        val position = refinement.position()
        refiBlock.removeRefinement(position)
        if (position in 0 until tranBlock.inventoryRefi.size) {
            tranBlock.setForgedUses(position, 0)
        }

        // Clean up from units if any unit is carrying this forged weapon
        val forgedItemId = ItemDb.MOD_MAX_ID + 1 + position
        unitBlock?.unitList?.forEach { group ->
            group.forEach { u ->
                u.rawInventory?.items?.forEach { item ->
                    if (item.itemId() == forgedItemId) {
                        item.removeItem()
                    }
                }
            }
        }

        notifyModified("Dövülmüş silah silindi: ${refinement.name}")
    }

    fun setForgedWeaponUses(position: Int, uses: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val tranBlock = chapter.blockTran ?: return
        if (position in 0 until tranBlock.inventoryRefi.size) {
            tranBlock.setForgedUses(position, uses)
            notifyModified()
        }
    }

    fun repairAllForgedWeapons() {
        val chapter = _uiState.value.chapterFile ?: return
        val refiBlock = chapter.blockRefi ?: return
        val tranBlock = chapter.blockTran ?: return

        tranBlock.setAllForgedUsesTo(99, refiBlock.refiList)
        notifyModified("Tüm dövülmüş silahların kullanımları 99'a yenilendi!")
    }

    // --- User / Story Progress Operations ---

    fun setMoney(money: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setMoney(money)
        notifyModified()
    }

    fun setRenown(renown: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setRenown(renown)
        chapter.blockDu26?.playerTeam?.setRenown(renown)
        notifyModified()
    }

    fun setDifficulty(diff: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setDifficulty(diff)
        chapter.blockHeader?.setDifficulty(diff)
        notifyModified()
    }

    fun setLunaticPlus(enabled: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setLunaticPlus(enabled)
        chapter.blockHeader?.setLunaticPlus(enabled)
        notifyModified()
    }

    fun setCasual(casual: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setGameModeFlag(2, casual)
        chapter.blockHeader?.setGameModeFlag(2, casual)
        notifyModified()
    }

    fun setPlaytime(hours: Int, minutes: Int, seconds: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val totalSeconds = (hours.coerceAtLeast(0) * 3600) + (minutes.coerceIn(0, 59) * 60) + seconds.coerceIn(0, 59)
        val frames = totalSeconds * 60
        chapter.blockUser.setPlaytime(frames)
        chapter.blockHeader?.setPlaytime(frames)
        notifyModified()
    }

    fun setStoryGlobalFlag(slot: Int, set: Boolean) {
        val chapter = _uiState.value.chapterFile ?: return
        chapter.blockUser.setGlobalFlag(slot, set)
        notifyModified()
    }

    // --- Map / Chapter Operations ---

    private var previousMapSnapshot: List<Int>? = null

    fun setMapState(mapIndex: Int, state: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        if (mapIndex in gmap.maps.indices) {
            gmap.maps[mapIndex].setLockState(state)
            previousMapSnapshot = null
            _uiState.update {
                it.copy(
                    isModified = true,
                    modificationCount = it.modificationCount + 1,
                    canUndoMap = false
                )
            }
        }
    }

    fun unlockAllMaps() {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        previousMapSnapshot = gmap.maps.map { it.lockState() }
        for (map in gmap.maps) {
            map.setLockState(2) // 2: Unlocked (Açık)
        }
        _uiState.update {
            it.copy(
                isModified = true,
                modificationCount = it.modificationCount + 1,
                canUndoMap = true,
                infoMessage = "Tüm ana bölümler ve yan görevler (Paralogue) haritada açıldı! 'Geri Al' ile önceki duruma dönebilirsiniz."
            )
        }
    }

    fun beatAllMaps() {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        previousMapSnapshot = gmap.maps.map { it.lockState() }
        for (map in gmap.maps) {
            map.setLockState(1) // 1: Beaten (Tamamlandı)
        }
        _uiState.update {
            it.copy(
                isModified = true,
                modificationCount = it.modificationCount + 1,
                canUndoMap = true,
                infoMessage = "Tüm bölümler tamamlandı olarak işaretlendi! 'Geri Al' ile önceki duruma dönebilirsiniz."
            )
        }
    }

    fun lockAllParalogues() {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        previousMapSnapshot = gmap.maps.map { it.lockState() }
        val overWorldNames = com.example.feawse.data.ChapterDb.getOverWorldNames()
        for (i in gmap.maps.indices) {
            val name = if (i < overWorldNames.size) overWorldNames[i] else ""
            if (name.contains("Paralogue", ignoreCase = true)) {
                gmap.maps[i].setLockState(0) // 0: Locked (Kilitli)
            }
        }
        _uiState.update {
            it.copy(
                isModified = true,
                modificationCount = it.modificationCount + 1,
                canUndoMap = true,
                infoMessage = "Tüm Paralogue kilitleri kapatıldı! 'Geri Al' ile önceki duruma dönebilirsiniz."
            )
        }
    }

    fun undoMapChanges() {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        val snapshot = previousMapSnapshot ?: return
        for (i in 0 until minOf(gmap.maps.size, snapshot.size)) {
            gmap.maps[i].setLockState(snapshot[i])
        }
        previousMapSnapshot = null
        _uiState.update {
            it.copy(
                isModified = true,
                modificationCount = it.modificationCount + 1,
                canUndoMap = false,
                infoMessage = "Harita durumları önceki haline geri alındı!"
            )
        }
    }

    // --- Cheats (Batch) ---

    fun batchMaxPlayableStats() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setMaxStats(chapter, 0)
        notifyModified("Tüm oyuncu birimleri yasal tavan istatistiklerine ulaştırıldı!")
    }

    fun batchMaxSupports() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setSupports(chapter, 0, 4) // S rank / Max
        notifyModified("Tüm destek bağları maksimum (S Seviyesi / Evlilik) seviyesine çıkarıldı!")
    }

    fun batchUnlockLegalSkills() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setSkillsLegal(chapter, 0)
        notifyModified("Tüm yasal sınıf yetenekleri açıldı!")
    }

    fun batchEnableBuffs() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setTemporalBuffs(chapter, 0, true)
        notifyModified("Kışla ve tonik güçlendirmeleri aktifleştirildi!")
    }

    fun batchAddMovement(bonus: Int = 2) {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setMovement(chapter, 0, bonus)
        notifyModified("Tüm oyuncu birimlerine +$bonus Hareket (Çizme) bonusu eklendi!")
    }

    fun batchSetBattlesVictories(count: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setBattlesVictories(chapter, 0, count)
        notifyModified("Tüm oyuncu birimlerinin savaş ve zafer sayıları $count yapıldı!")
    }

    fun batchSetConvoyAmount(amount: Int = 99) {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setConvoyAmountTo(chapter, amount)
        notifyModified("Tüm konvoy eşyalarının stok adedi $amount yapıldı!")
    }

    fun batchSetConvoyUses(uses: Int = 99) {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setConvoyUsesTo(chapter, uses)
        notifyModified("Tüm konvoy eşyalarının kullanım sayısı $uses yapıldı!")
    }

    fun batchGodAndPray() {
        val chapter = _uiState.value.chapterFile ?: return
        Batch.setMaxStats(chapter, 0)
        Batch.setSkillsLegal(chapter, 0)
        Batch.setSupports(chapter, 0, 4)
        Batch.setMovement(chapter, 0, 2)
        Batch.setTemporalBuffs(chapter, 0, true)
        notifyModified("Tanrı Modu: Maks Statlar, Yasal Yetenekler, S Destekler, +2 Hareket ve Tüm Bufflar uygulandı!")
    }

    // --- Logbook / Avatar & Einherjar ---

    fun addUnitLogBlock(unit: Unit) {
        unit.addBlockLog()
        val isWest = _uiState.value.regionInfo?.isWest ?: true
        unit.rawLog?.changeRegion(isWest)
        unit.rawLog?.setName(unit.unitName())
        unit.rawLog?.setAsset(0)
        unit.rawLog?.setFlaw(0)
        unit.rawLog?.setHairColorFx(unit.rawBlockEnd?.getHairColorFx())
        unit.rawLog?.setGender(unit.isFemale())
        unit.rawLog?.setLogIdRandom()
        notifyModified()
    }

    fun removeUnitLogBlock(unit: Unit) {
        unit.removeBlockExtra(true)
        notifyModified()
    }

    fun setLogUnitName(unit: Unit, name: String) {
        val log = unit.rawLog ?: return
        log.setName(name)
        notifyModified()
    }

    fun setLogUnitEinherjar(unit: Unit, isEinherjar: Boolean) {
        val log = unit.rawLog ?: return
        log.setEinherjar(isEinherjar)
        notifyModified()
    }

    fun setLogUnitGender(unit: Unit, female: Boolean) {
        val log = unit.rawLog ?: return
        log.setGender(female)
        notifyModified()
    }

    fun setLogUnitBuild(unit: Unit, slot: Int, value: Int) {
        val log = unit.rawLog ?: return
        log.setBuild(slot, value)
        notifyModified()
    }

    fun setLogUnitVoice(unit: Unit, voice: Int) {
        val log = unit.rawLog ?: return
        log.setVoice(voice)
        notifyModified()
    }

    fun setLogUnitAsset(unit: Unit, asset: Int) {
        val log = unit.rawLog ?: return
        log.setAsset(asset)
        notifyModified()
    }

    fun setLogUnitFlaw(unit: Unit, flaw: Int) {
        val log = unit.rawLog ?: return
        log.setFlaw(flaw)
        notifyModified()
    }

    fun setLogUnitBirthday(unit: Unit, day: Int, month: Int) {
        val log = unit.rawLog ?: return
        log.setBirthday(day.coerceIn(1, 31), month.coerceIn(1, 12))
        notifyModified()
    }

    fun setLogUnitId(unit: Unit, logIdHex: String) {
        val log = unit.rawLog ?: return
        log.setLogId(logIdHex)
        notifyModified()
    }

    fun setLogUnitRandomId(unit: Unit) {
        val log = unit.rawLog ?: return
        log.setLogIdRandom()
        notifyModified()
    }

    fun applyEinherjarPreset(unit: Unit, einherjar: com.example.feawse.data.model.EinherjarModel) {
        val log = unit.rawLog ?: return
        val isWest = _uiState.value.regionInfo?.isWest ?: true
        log.setEinherjar(true)
        val hex = Integer.toHexString(einherjar.logId)
        log.setLogId(hex)
        log.setName(einherjar.getLanguageName(isWest))
        log.setTextGreeting(einherjar.getLanguageGreeting(isWest))
        log.setTextChallenge(einherjar.getLanguageChallenge(isWest))
        log.setTextRecruit(einherjar.getLanguageRecruit(isWest))
        log.setAsset(einherjar.asset)
        log.setFlaw(einherjar.flaw)
        log.setBuild(0, einherjar.build)
        log.setBuild(1, einherjar.face)
        log.setBuild(2, einherjar.hair)
        log.setVoice(einherjar.voice)
        log.setGender(einherjar.isFemale)
        if (einherjar.hairColor != null) {
            log.setHairColorFx(einherjar.hairColor)
            unit.rawBlockEnd?.setHairColorFx(einherjar.hairColor)
        }
        if (einherjar.avatarClass > 0) {
            unit.rawBlock1?.setUnitClass(einherjar.avatarClass)
        }
        notifyModified()
    }

    fun setLogUnitMessages(unit: Unit, greeting: String, challenge: String, recruit: String, street: String) {
        val log = unit.rawLog ?: return
        log.setTextGreeting(greeting)
        log.setTextChallenge(challenge)
        log.setTextRecruit(recruit)
        log.setTextStreet(street)
        notifyModified()
    }

    fun setLogUnitCardProfile(unit: Unit, slot: Int, value: Int) {
        val log = unit.rawLog ?: return
        log.setProfileCard(value, slot)
        notifyModified()
    }

    fun setLogUnitDifficulty(unit: Unit, diff: Int) {
        val log = unit.rawLog ?: return
        log.setDifficulty(diff.coerceIn(0, 2))
        notifyModified()
    }

    fun setLogUnitGameModeFlag(unit: Unit, slot: Int, enabled: Boolean) {
        val log = unit.rawLog ?: return
        log.setGameModeFlag(slot, enabled)
        notifyModified()
    }

    // --- Harita Karşılaşmaları (Overworld Encounters) & Kışla (Barracks) ---

    fun randomizeEncounters(type: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        gmap.randomizeMaps(type)
        notifyModified(when (type) {
            0 -> "Tüm harita karşılaşmaları temizlendi."
            1 -> "Tüm açık bölümlere Risen orduları yerleştirildi."
            2 -> "Tüm açık bölümlere Tüccar Anna yerleştirildi."
            else -> "Harita karşılaşmaları rastgele dolduruldu."
        })
    }

    fun setMapEncounter(mapIndex: Int, slot: Int, encounterType: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val gmap = chapter.blockGmap ?: return
        if (mapIndex in 0 until gmap.maps.size) {
            val map = gmap.maps[mapIndex]
            map.setEncounter(slot, encounterType)
            notifyModified()
        }
    }

    fun setBarracksEvent(eventIndex: Int, type: Int, unit1: Int, unit2: Int = 65535, icon: Int = 0) {
        val chapter = _uiState.value.chapterFile ?: return
        val evst = chapter.blockEvst ?: return
        if (eventIndex in 0 until evst.eventList.size) {
            val ev = evst.eventList[eventIndex]
            ev.setEventType(type)
            ev.setUnit1(unit1)
            ev.setUnit2(unit2)
            ev.setEventIcon(icon)
            notifyModified()
        }
    }

    fun fillAllBarracksEvents() {
        val chapter = _uiState.value.chapterFile ?: return
        val evst = chapter.blockEvst ?: return
        val mainRoster = chapter.blockUnit?.unitList?.getOrNull(3) ?: emptyList()
        val playableUnits = mainRoster.map { it.unitId }
        val u1 = playableUnits.getOrNull(0) ?: 0 // Chrom
        val u2 = playableUnits.getOrNull(1) ?: 1 // Robin
        val u3 = playableUnits.getOrNull(2) ?: 2 // Lissa
        val u4 = playableUnits.getOrNull(3) ?: 3 // Frederick
        val u5 = playableUnits.getOrNull(4) ?: 4 // Sully

        evst.eventList.getOrNull(0)?.apply {
            setEventType(1) // Stat Boost
            setUnit1(u1)
            setUnit2(65535)
        }
        evst.eventList.getOrNull(1)?.apply {
            setEventType(2) // Exp Gain
            setUnit1(u2)
            setUnit2(65535)
        }
        evst.eventList.getOrNull(2)?.apply {
            setEventType(3) // Weapon Exp Gain
            setUnit1(u3)
            setUnit2(65535)
        }
        evst.eventList.getOrNull(3)?.apply {
            setEventType(4) // Random Item
            setUnit1(u4)
            setUnit2(65535)
        }
        evst.eventList.getOrNull(4)?.apply {
            setEventType(5) // Conversation
            setUnit1(u1)
            setUnit2(u2)
            setEventIcon(0)
        }
        notifyModified("Tüm kışla etkinlikleri dolduruldu!")
    }

    fun clearBarracksEvents() {
        val chapter = _uiState.value.chapterFile ?: return
        val evst = chapter.blockEvst ?: return
        for (ev in evst.eventList) {
            ev.setEventType(0)
            ev.setUnit1(65535)
            ev.setUnit2(65535)
            ev.setEventIcon(0)
        }
        notifyModified("Kışla etkinlikleri temizlendi.")
    }

    // --- Sokak Geçişi (StreetPass) & Kablosuz Ekipler (Wireless Block) ---

    fun setPlayerTeamName(name: String) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val team = du.playerTeam ?: return
        team.setName(name)
        notifyModified()
    }

    fun setPlayerTeamRenown(renown: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val team = du.playerTeam ?: return
        team.setRenown(renown.coerceIn(0, 999999))
        notifyModified()
    }

    fun autoFillPlayerTeamFromArmy() {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val team = du.playerTeam ?: return
        val isWest = chapter.isWest
        val mainRoster = chapter.blockUnit?.unitList?.getOrNull(3) ?: emptyList()
        val activeUnits = mainRoster.filter { it.rawBlock1 != null }
        if (activeUnits.isEmpty()) return

        team.unitList.clear()
        for (i in 0 until minOf(10, activeUnits.size)) {
            val unit = activeUnits[i]
            val unitDu = unit.toUnitDu(isWest)
            if (i == 0) {
                unitDu.setDuFlag(4, true) // Leader flag
            }
            team.unitList.add(unitDu)
        }
        if (team.unitList.isNotEmpty()) {
            team.setExtraData(team.unitList[0])
        }
        notifyModified("StreetPass takımı ordunuzun ilk ${team.unitList.size} birimiyle dolduruldu!")
    }

    fun setPlayerTeamUnit(slot: Int, unit: Unit) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val team = du.playerTeam ?: return
        val isWest = chapter.isWest
        val unitDu = unit.toUnitDu(isWest)
        if (slot == 0) {
            unitDu.setDuFlag(4, true)
        }
        if (slot < team.unitList.size) {
            team.unitList[slot] = unitDu
        } else {
            team.unitList.add(unitDu)
        }
        if (team.unitList.isNotEmpty()) {
            team.setExtraData(team.unitList[0])
        }
        notifyModified("${unit.unitName()} StreetPass takımına (Yuva ${slot + 1}) atandı.")
    }

    fun removePlayerTeamUnit(slot: Int) {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val team = du.playerTeam ?: return
        if (slot in 0 until team.unitList.size) {
            team.unitList.removeAt(slot)
            if (team.unitList.isNotEmpty()) {
                team.unitList[0].setDuFlag(4, true) // Ensure first unit is leader
                team.setExtraData(team.unitList[0])
            }
            notifyModified("StreetPass takımı yuvası boşaltıldı.")
        }
    }

    fun clearForeignWirelessTeams() {
        val chapter = _uiState.value.chapterFile ?: return
        val du = chapter.blockDu26 ?: return
        val count = du.teamList.size
        du.teamList.clear()
        // Also clear wireless encounter flags from maps
        chapter.blockGmap?.maps?.forEach { map ->
            if (map.isWireless(0)) map.setEncounter(0, 0)
            if (map.isWireless(1)) map.setEncounter(1, 0)
        }
        notifyModified("$count adet yabancı kablosuz ekip haritadan temizlendi.")
    }

    // ==========================================
    // Faz 9: Global (Sistem) Kaydı Eylemleri
    // ==========================================

    fun setGlobalRenown(value: Int) {
        val global = _uiState.value.globalFile ?: return
        global.glUserBlock.renown = value.coerceIn(0, 999_999)
        notifyModified("Şöhret güncellendi: ${global.glUserBlock.renown}")
    }

    fun setGlobalFlag(bit: Int, enabled: Boolean) {
        val global = _uiState.value.globalFile ?: return
        global.glUserBlock.setGlobalFlag(bit, enabled)
        notifyModified()
    }

    fun unlockAllSupportsAndGallery() {
        val global = _uiState.value.globalFile ?: return
        global.glUserBlock.fullSupportLog()
        notifyModified("Tüm Destek Günlüğü ve Birim Galerisi kilitleri açıldı!")
    }

    fun changeGlobalRegion(isWest: Boolean) {
        val global = _uiState.value.globalFile ?: return
        global.changeRegion(isWest)
        val regionName = if (isWest) "USA / EUR (Batı)" else "JPN (Japonya)"
        _uiState.update {
            it.copy(
                regionInfo = it.regionInfo?.copy(regionName = regionName, headerSize = if (isWest) 0xC0 else 0x80)
            )
        }
        notifyModified("Genel kayıt bölgesi değiştirildi: $regionName")
    }

    fun moveGlobalUnit(fromIndex: Int, toIndex: Int) {
        val global = _uiState.value.globalFile ?: return
        val list = global.glUnitBlock.unitList
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            notifyModified()
        }
    }

    fun deleteGlobalUnit(index: Int) {
        val global = _uiState.value.globalFile ?: return
        val list = global.glUnitBlock.unitList
        if (index in list.indices) {
            val removed = list.removeAt(index)
            notifyModified("${removed.name} günlükten silindi.")
        }
    }

    fun importUnitToGlobal(unit: Unit) {
        val global = _uiState.value.globalFile ?: return
        val list = global.glUnitBlock.unitList
        if (list.size >= 99) {
            _uiState.update { it.copy(errorMessage = "Avatar günlüğü dolu (Maksimum 99 birim)!") }
            return
        }
        val isWest = global.region()
        val unitDu = unit.toUnitDu(isWest)
        list.add(unitDu)
        notifyModified("${unitDu.name} günlüğe başarıyla eklendi! (${list.size}/99)")
    }

    fun importUnitBytesToGlobal(bytes: ByteArray) {
        val global = _uiState.value.globalFile ?: return
        val list = global.glUnitBlock.unitList
        if (list.size >= 99) {
            _uiState.update { it.copy(errorMessage = "Avatar günlüğü dolu (Maksimum 99 birim)!") }
            return
        }
        try {
            val unit = Unit(bytes)
            importUnitToGlobal(unit)
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Birim içe aktarılamadı: ${e.message}") }
        }
    }
}
