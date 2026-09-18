package com.example.feawse.compression

import org.junit.Assert.assertEquals
import org.junit.Test

class DataViewTest {

    @Test
    fun testGetUint16() {
        val buffer = byteArrayOf(0x34, 0x12)
        val value = DataView.getUint16(buffer, 0)
        assertEquals(0x1234L, value)
    }

    @Test
    fun testGetUint32() {
        val buffer = byteArrayOf(0x78.toByte(), 0x56.toByte(), 0x34.toByte(), 0x12.toByte())
        val value = DataView.getUint32(buffer, 0)
        assertEquals(0x12345678L, value)
    }

    @Test
    fun testByteAsULong() {
        assertEquals(0xFFL, DataView.byteAsULong((-1).toByte()))
        assertEquals(0x00L, DataView.byteAsULong(0.toByte()))
        assertEquals(0x80L, DataView.byteAsULong(0x80.toByte()))
    }
}
