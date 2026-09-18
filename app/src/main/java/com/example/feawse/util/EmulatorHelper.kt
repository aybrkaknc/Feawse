package com.example.feawse.util

import com.example.feawse.savefile.SaveFile
import java.util.Arrays

object EmulatorHelper {

    const val TITLE_ID_USA = "00040000000A0500"
    const val TITLE_ID_EUR = "000400000009F100"
    const val TITLE_ID_JPN = "000400000007A900"

    data class SlotInfo(
        val slotNumber: Int,
        val displayName: String,
        val description: String,
        val isBattleSave: Boolean = false,
        val isGlobal: Boolean = false
    )

    fun getSlotInfo(filename: String): SlotInfo {
        val lower = filename.lowercase()
        return when {
            lower.contains("chapter0") -> SlotInfo(1, "Kayıt Yuvası 1 (Slot 1)", "Normal Oyun Kaydı — Slot 1")
            lower.contains("chapter1") -> SlotInfo(2, "Kayıt Yuvası 2 (Slot 2)", "Normal Oyun Kaydı — Slot 2")
            lower.contains("chapter2") -> SlotInfo(3, "Kayıt Yuvası 3 (Slot 3)", "Normal Oyun Kaydı — Slot 3")
            lower.contains("chapter3") -> SlotInfo(4, "Savaş Kaydı (Battle Save)", "Harita içi askıya alınan kayıt", isBattleSave = true)
            lower.contains("global") -> SlotInfo(0, "Genel Veri (Global Save)", "Şöhret, SpotPass ve Galeri kayıtları", isGlobal = true)
            else -> SlotInfo(-1, "Özel Kayıt Dosyası", filename)
        }
    }

    data class RegionDetection(
        val regionName: String,
        val isCompressed: Boolean,
        val headerSize: Int,
        val isGlobal: Boolean = false
    ) {
        val isWest: Boolean get() = headerSize != 0x80
    }

    fun isGlobalSave(bytes: ByteArray, filename: String = ""): Boolean {
        if (filename.lowercase().contains("global")) return true
        val edni = Hex.toByte("45 44 4E 49")
        val pmoc = Hex.toByte("50 4D 4F 43")
        val at0 = if (bytes.size >= 4) Hex.getByte4Array(bytes, 0x0) else ByteArray(4)
        val us = if (bytes.size > 0xC4) Hex.getByte4Array(bytes, 0xC0) else ByteArray(4)
        val jp = if (bytes.size > 0x84) Hex.getByte4Array(bytes, 0x80) else ByteArray(4)

        val isChapterUs = Arrays.equals(us, edni) || Arrays.equals(us, pmoc)
        val isChapterJp = Arrays.equals(jp, edni) || Arrays.equals(jp, pmoc)

        val isAt0 = Arrays.equals(at0, edni) || Arrays.equals(at0, pmoc)
        return isAt0 && !isChapterUs && !isChapterJp
    }

    fun detectRegion(bytes: ByteArray, filename: String = ""): RegionDetection {
        val edni = Hex.toByte("45 44 4E 49") // EDNI
        val pmoc = Hex.toByte("50 4D 4F 43") // PMOC

        val global = if (bytes.size >= 4) Hex.getByte4Array(bytes, 0x0) else ByteArray(4)
        val us = if (bytes.size > 0xC4) Hex.getByte4Array(bytes, 0xC0) else ByteArray(4)
        val jp = if (bytes.size > 0x84) Hex.getByte4Array(bytes, 0x80) else ByteArray(4)

        val isCompressed = Arrays.equals(global, pmoc) || Arrays.equals(us, pmoc) || Arrays.equals(jp, pmoc)

        if (isGlobalSave(bytes, filename)) {
            return RegionDetection("Global (Sistem Kaydı)", isCompressed, 0x0, isGlobal = true)
        }

        return when {
            Arrays.equals(us, edni) || Arrays.equals(us, pmoc) -> {
                RegionDetection("USA / EUR (Batı)", isCompressed, 0xC0, isGlobal = false)
            }
            Arrays.equals(jp, edni) || Arrays.equals(jp, pmoc) -> {
                RegionDetection("JPN (Japonya)", isCompressed, 0x80, isGlobal = false)
            }
            else -> {
                RegionDetection("Bilinmeyen Bölge", isCompressed, 0, isGlobal = false)
            }
        }
    }

    /**
     * Emulator guidance and typical save file directories on Android.
     */
    val EMULATOR_GUIDE_TEXT = """
        🎮 3DS Emülatörleri Kayıt Dosyası Yolları:
        
        🔹 Azahar Plus / Citra Android:
        Dahili Depolama / Android / data / org.citra.citra_emu / files / sdmc / Nintendo 3DS / ...
        veya
        Dahili Depolama / Azahar Plus / sdmc / Nintendo 3DS / [0000...] / [0000...] / title / 00040000 / [TITLE_ID] / data / 00000001 /
        
        🔹 Lime3DS:
        Dahili Depolama / Android / data / io.github.lime3ds / files / sdmc / ...
        
        🔹 Mandarine 3DS:
        Dahili Depolama / Mandarine / sdmc / ...
        
        📁 FE: Awakening Title ID'leri:
        • ABD (USA): 00040000000a0500 (veya klasör 000a0500)
        • Avrupa (EUR): 000400000009f100 (veya klasör 0009f100)
        • Japonya (JPN): 000400000007a900 (veya klasör 0007a900)
        
        💾 Dosya Adları:
        • chapter0 = Kayıt Yuvası 1
        • chapter1 = Kayıt Yuvası 2
        • chapter2 = Kayıt Yuvası 3
        • chapter3 = Savaş Kaydı (Bookmark)
        • global   = Ekstra veriler (Renown/StreetPass)
        
        ⚠️ ÖNEMLİ: Dosyanızı açmadan önce uygulamanın otomatik yedek aldığından emin olun, orijinal dosyanızı güvende tutun!
    """.trimIndent()
}
