package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Test

class FdroidIndexBytesTest {
    @Test
    fun findsAsciiNeedleWithoutDecodingHaystack() {
        val hay = """{"apps":[{"packageName":"org.keep"}]}""".toByteArray()
        val at = FdroidIndexBytes.indexOf(hay, "\"packageName\"")
        val nameAt = FdroidIndexBytes.indexOf(hay, "org.keep")
        assertEquals(true, at >= 0)
        assertEquals("org.keep", FdroidIndexBytes.utf8(hay, nameAt, nameAt + 8))
    }

    @Test
    fun objectSliceKeepsLeadingSourceCodeOffNextApp() {
        val hay = """{"apps":[{"sourceCode":"https://github.com/peterhearty/WipeFiles","packageName":"uk.org.platitudes.wipefiles"},{"sourceCode":"https://github.com/Foxek/WiseTimer","packageName":"com.foxek.simpletimer"}]}""".toByteArray()
        val wipeAt = FdroidIndexBytes.indexOf(hay, "\"packageName\"")
        val slice = FdroidIndexBytes.objectSlice(hay, wipeAt)
        assertEquals(true, slice.contains("peterhearty/WipeFiles"))
        assertEquals(false, slice.contains("WiseTimer"))
        assertEquals(false, slice.contains("com.foxek.simpletimer"))
    }
}
