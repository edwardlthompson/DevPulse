package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FdroidPackageParserTest {
    @Test
    fun suggestedVersionFromApi() {
        val json = """{"packageName":"org.fdroid.fdroid","suggestedVersionCode":1023052,"packages":[{"versionName":"2.0-rc0","versionCode":2000040},{"versionName":"1.23.2","versionCode":1023052}]}"""
        val rec = FdroidPackageParser.parse("org.fdroid.fdroid", json, "official")
        assertEquals("1.23.2", rec?.suggestedVersionName)
    }

    @Test
    fun blankJsonIsNull() {
        assertNull(FdroidPackageParser.parse("org.x", "", "official"))
        assertNull(FdroidPackageParser.parse("org.x", "{}", "official"))
    }
}
