package dev.foss.goldenpath.inventory

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPersistenceTest {
    @Test
    fun userFilesContainsCoreTsvFiles() {
        assertTrue("pasted_repos.tsv" in SettingsPersistence.userFiles)
        assertTrue("ignored_updates.tsv" in SettingsPersistence.userFiles)
        assertTrue("direct_apks.tsv" in SettingsPersistence.userFiles)
    }

    @Test
    fun jsonExportAndStructureValid() {
        val root = JsonObject()
        val settings = JsonObject().apply {
            addProperty("welcomeSeen", true)
            addProperty("queryAllPackagesAck", true)
            addProperty("auroraPlay", true)
            addProperty("apkPureLookup", true)
        }
        val files = JsonObject().apply {
            addProperty("pasted_repos.tsv", "pkg\towner/repo\n")
        }
        root.add("settings", settings)
        root.add("files", files)

        val retrievedSettings = root.getAsJsonObject("settings")
        assertTrue(retrievedSettings.get("welcomeSeen").asBoolean)
        assertTrue(retrievedSettings.get("auroraPlay").asBoolean)
        assertEquals("pkg\towner/repo\n", root.getAsJsonObject("files").get("pasted_repos.tsv").asString)
    }
}
