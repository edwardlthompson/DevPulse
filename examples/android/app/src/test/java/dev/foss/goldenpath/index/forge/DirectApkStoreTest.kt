package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class DirectApkStoreTest {
    @Test
    fun httpsApkPersistsAndLinkLocalHttpIsRejected() {
        assertNull(DirectApkCodec.normalize("org.app", "http://169.254.0.1/x.apk"))
        assertNull(DirectApkCodec.normalize("", "https://github.com/o/r/releases/download/v1/app.apk"))
        val href = "https://github.com/o/r/releases/download/v1/app.apk"
        assertEquals("org.app" to href, DirectApkCodec.normalize("org.app", href))
        val store = FileDirectApkStore(File(createTempDirectory("apk").toFile(), "d.tsv"))
        store.put("org.app", href)
        store.put("org.app", "http://169.254.0.1/x.apk")
        assertEquals(href, store.load()["org.app"])
    }
}
