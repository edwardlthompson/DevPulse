package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FdroidWhatsNewTest {
    @Test
    fun readsLocalizedWhatsNew() {
        val chunk = """{"packageName":"app.one","localized":{"en-US":{"whatsNew":"Fixed crash\nNew icon"}}}"""
        assertEquals("Fixed crash\nNew icon", FdroidWhatsNew.parse(chunk))
    }

    @Test
    fun missingOrBlankIsNone() {
        assertNull(FdroidWhatsNew.parse("""{"packageName":"app.one"}"""))
        assertNull(FdroidWhatsNew.parse("""{"whatsNew":"   "}"""))
    }
}
