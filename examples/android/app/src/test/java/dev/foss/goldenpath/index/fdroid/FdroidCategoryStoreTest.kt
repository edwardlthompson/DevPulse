package dev.foss.goldenpath.index.fdroid

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FdroidCategoryStoreTest {
    @Test
    fun replaceWriteRoundTrip() {
        val dir = createTempDirectory("fdroid-cat").toFile()
        val store = FileFdroidCategoryStore(File(dir, "cats.tsv"))
        store.putAll(
            listOf(
                FdroidAppRecord("org.maps", 1L, null, "official", category = "Navigation", relatedPackages = listOf("org.other")),
            ),
        )
        val loaded = store.load()
        assertEquals("Navigation", loaded.getValue("org.maps").category)
        assertEquals(listOf("org.other"), loaded.getValue("org.maps").related)
        store.save(emptyMap())
        assertTrue(store.load().isEmpty())
    }
}
