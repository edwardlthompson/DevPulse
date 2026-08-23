package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageRoomTest {
    @Test
    fun enoughLeavesReserve() {
        assertTrue(StorageRoom.enough(free = StorageRoom.RESERVE + 2, need = 1))
        assertFalse(StorageRoom.enough(free = StorageRoom.RESERVE + 1, need = 1))
        assertFalse(StorageRoom.enough(free = StorageRoom.RESERVE, need = 0))
        assertFalse(StorageRoom.enough(free = 100, need = -1))
    }
}
