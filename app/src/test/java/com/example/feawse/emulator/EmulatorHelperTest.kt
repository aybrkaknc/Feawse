package com.example.feawse.emulator

import com.example.feawse.util.EmulatorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmulatorHelperTest {

    @Test
    fun testGetSlotInfo() {
        val slot0 = EmulatorHelper.getSlotInfo("chapter0")
        assertEquals(1, slot0.slotNumber)
        assertEquals("Kayıt Yuvası 1 (Slot 1)", slot0.displayName)
        assertFalse(slot0.isBattleSave)
        assertFalse(slot0.isGlobal)

        val slot1 = EmulatorHelper.getSlotInfo("chapter1")
        assertEquals(2, slot1.slotNumber)

        val slot2 = EmulatorHelper.getSlotInfo("chapter2")
        assertEquals(3, slot2.slotNumber)

        val slot3 = EmulatorHelper.getSlotInfo("chapter3")
        assertEquals(4, slot3.slotNumber)
        assertTrue(slot3.isBattleSave)

        val global = EmulatorHelper.getSlotInfo("global")
        assertEquals(0, global.slotNumber)
        assertTrue(global.isGlobal)

        val custom = EmulatorHelper.getSlotInfo("my_save.bin")
        assertEquals(-1, custom.slotNumber)
        assertEquals("my_save.bin", custom.description)
    }

    @Test
    fun testDetectRegion() {
        // Test USA/EUR uncompressed header with "EDNI" at 0xC0
        val usaBytes = ByteArray(0xD0)
        val edni = byteArrayOf(0x45, 0x44, 0x4E, 0x49)
        System.arraycopy(edni, 0, usaBytes, 0xC0, 4)

        val usaResult = EmulatorHelper.detectRegion(usaBytes)
        assertEquals("USA / EUR (Batı)", usaResult.regionName)
        assertFalse(usaResult.isCompressed)
        assertEquals(0xC0, usaResult.headerSize)

        // Test JPN compressed header with "PMOC" at 0x80
        val jpnBytes = ByteArray(0xA0)
        val pmoc = byteArrayOf(0x50, 0x4D, 0x4F, 0x43)
        System.arraycopy(pmoc, 0, jpnBytes, 0x80, 4)

        val jpnResult = EmulatorHelper.detectRegion(jpnBytes)
        assertEquals("JPN (Japonya)", jpnResult.regionName)
        assertTrue(jpnResult.isCompressed)
        assertEquals(0x80, jpnResult.headerSize)
    }

    @Test
    fun testTitleIdsAndGuide() {
        assertEquals("00040000000A0500", EmulatorHelper.TITLE_ID_USA)
        assertEquals("000400000009F100", EmulatorHelper.TITLE_ID_EUR)
        assertEquals("000400000007A900", EmulatorHelper.TITLE_ID_JPN)

        assertTrue(EmulatorHelper.EMULATOR_GUIDE_TEXT.contains("Azahar Plus"))
        assertTrue(EmulatorHelper.EMULATOR_GUIDE_TEXT.contains("Lime3DS"))
        assertTrue(EmulatorHelper.EMULATOR_GUIDE_TEXT.contains("00040000000a0500"))
        assertTrue(EmulatorHelper.EMULATOR_GUIDE_TEXT.contains("chapter0"))
    }
}
