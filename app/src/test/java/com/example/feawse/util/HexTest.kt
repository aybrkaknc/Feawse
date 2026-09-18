package com.example.feawse.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HexTest {

    @Test
    fun testByte2Operations() {
        val bytes = ByteArray(4)
        // Little endian 0x1234 -> byte 0: 0x34, byte 1: 0x12
        Hex.setByte2(bytes, 0, 0x1234)
        assertEquals(0x34.toByte(), bytes[0])
        assertEquals(0x12.toByte(), bytes[1])

        val retrieved = Hex.getByte2(bytes, 0)
        assertEquals(0x1234, retrieved)
    }

    @Test
    fun testByte4Operations() {
        val bytes = ByteArray(8)
        // Little endian 0x12345678
        Hex.setByte4(bytes, 2, 0x12345678)
        val retrieved = Hex.getByte4(bytes, 2)
        assertEquals(0x12345678, retrieved)

        val byteArray4 = Hex.intToByteArray4(0x0A0B0C0D)
        assertEquals(0x0D.toByte(), byteArray4[0])
        assertEquals(0x0C.toByte(), byteArray4[1])
        assertEquals(0x0B.toByte(), byteArray4[2])
        assertEquals(0x0A.toByte(), byteArray4[3])
    }

    @Test
    fun testStringToByteAndToHex() {
        val parsed = Hex.toByte("45 44 4E 49")
        assertEquals(4, parsed.size)
        assertEquals('E'.code.toByte(), parsed[0])
        assertEquals('D'.code.toByte(), parsed[1])
        assertEquals('N'.code.toByte(), parsed[2])
        assertEquals('I'.code.toByte(), parsed[3])

        val hexStr = Hex.byteArrayToHexString(parsed)
        assertEquals("45444e49", hexStr)
    }

    @Test
    fun testBitFlagOperations() {
        val bytes = ByteArray(2)
        Hex.setBitFlag(bytes, 0, 3, true)
        assertEquals(true, Hex.hasBitFlag(bytes, 0, 3))
        assertEquals(false, Hex.hasBitFlag(bytes, 0, 2))

        Hex.setBitFlag(bytes, 0, 3, false)
        assertEquals(false, Hex.hasBitFlag(bytes, 0, 3))
    }
}
