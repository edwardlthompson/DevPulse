package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Test

class FdroidAntiFeaturesTest {
    @Test
    fun parsesTrackingAndNonFreeNet() {
        assertEquals(
            listOf("Tracking", "NonFreeNet"),
            FdroidAntiFeatures.parse("""{"antiFeatures":["Tracking","NonFreeNet"]}"""),
        )
        assertEquals(emptyList<String>(), FdroidAntiFeatures.parse("""{"packageName":"org.x"}"""))
        val rec = FdroidIndexParser.parse(
            """{"apps":[{"packageName":"org.ver","antiFeatures":["Tracking"],"lastUpdated":1}],"packages":{"org.ver":[{"versionCode":1,"apkName":"org.ver_1.apk"}]}}""",
            "official",
        ).single()
        assertEquals(listOf("Tracking"), rec.antiFeatures)
    }
}
