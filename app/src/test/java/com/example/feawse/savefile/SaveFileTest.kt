package com.example.feawse.savefile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveFileTest {

    @Test
    fun testUncompressedPassThrough() {
        // When bytes are not marked with PMOC, autoDecompress should return original bytes
        val dummySave = ByteArray(0x200) { it.toByte() }
        val result = SaveFile.autoDecompress(dummySave)
        assertEquals(dummySave.size, result.size)
        assertEquals(dummySave[0], result[0])
        assertEquals(dummySave[100], result[100])
    }

    @Test
    fun testIsChapterDetection() {
        val dummyData = ByteArray(0x200)
        // No EDNI/PMOC header -> not recognized as chapter save
        assertFalse(SaveFile.isChapter(dummyData))

        // Set US uncompressed header "EDNI" (0x45, 0x44, 0x4E, 0x49) at offset 0xC0
        val edni = byteArrayOf(0x45, 0x44, 0x4E, 0x49)
        System.arraycopy(edni, 0, dummyData, 0xC0, 4)
        assertTrue(SaveFile.isChapter(dummyData))
    }
}
