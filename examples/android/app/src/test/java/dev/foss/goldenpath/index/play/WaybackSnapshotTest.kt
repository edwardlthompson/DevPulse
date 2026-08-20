package dev.foss.goldenpath.index.play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WaybackSnapshotTest {
    @Test
    fun prefersIdSnapshotAndIgnoresUnavailable() {
        val json = """{"archived_snapshots":{"closest":{"available":true,"url":"http://web.archive.org/web/20240601120000/https://play.google.com/store/apps/details?id=app.gone","status":"200"}}}"""
        assertEquals(
            "https://web.archive.org/web/20240601120000id_/https://play.google.com/store/apps/details?id=app.gone",
            WaybackSnapshot.snapshotUrl(json),
        )
        assertNull(WaybackSnapshot.snapshotUrl("""{"archived_snapshots":{}}"""))
    }
}
