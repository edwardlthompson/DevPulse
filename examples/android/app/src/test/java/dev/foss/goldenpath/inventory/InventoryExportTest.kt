package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class InventoryExportTest {
    private val listed = sampleApp(
        packageName = "org.example.app",
        label = "Example",
        latestListings = listOf(
            UpdateLink(
                source = RemoteReleasedSource.Forge,
                url = "https://github.com/example/app/releases",
                versionName = "2.0",
                releasedAtMs = 1_700_000_000_000L,
                listed = true,
                known = true,
            ),
        ),
    )

    @Test
    fun htmlLinksListedForgeAndEscapesLabel() {
        val html = InventoryExport.render(
            listOf(listed.copy(label = "<script>alert(1)</script>")),
            InventoryExportFormat.Html,
        )
        assertTrue(html.contains("""<a href="https://github.com/example/app/releases">"""))
        assertTrue(html.contains("&lt;script&gt;"))
        assertFalse(html.contains("<script>alert"))
    }

    @Test
    fun csvIncludesPackageAndUrl() {
        val csv = InventoryExport.render(listOf(listed), InventoryExportFormat.Csv)
        assertTrue(csv.contains("org.example.app"))
        assertTrue(csv.contains("https://github.com/example/app/releases"))
    }

    @Test
    fun xmlIsWellFormed() {
        val xml = InventoryExport.render(listOf(listed), InventoryExportFormat.Xml)
        val parsed = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
        assertTrue(parsed.documentElement.tagName == "inventory")
        assertTrue(xml.contains("https://github.com/example/app/releases"))
    }

    @Test
    fun emptyListWritesValidHeaders() {
        val html = InventoryExport.render(emptyList(), InventoryExportFormat.Html)
        val csv = InventoryExport.render(emptyList(), InventoryExportFormat.Csv)
        val xml = InventoryExport.render(emptyList(), InventoryExportFormat.Xml)
        assertTrue(html.contains("<tbody></tbody>") || html.contains("<tbody></tbody>".lowercase()))
        assertTrue(html.contains("<thead>"))
        assertTrue(csv.startsWith("label,package,"))
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
    }
}
