package dev.foss.goldenpath.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceToggleGateTest {
    private val ready = SourceGateState(githubTokenPresent = true, customIndexUrl = "https://f-droid.org/repo/index-v1.jar")
    private val empty = SourceGateState(githubTokenPresent = false, customIndexUrl = "")

    @Test
    fun playAndCatalogTogglesNeverNeedSetup() {
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.Play, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.Aurora, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.Forge, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.Aptoide, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.ApkMirror, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.ApkPure, empty))
    }

    @Test
    fun starredAndSearchNeedSavedToken() {
        assertFalse(SourceToggleGate.allowOn(SourceToggleId.Starred, empty))
        assertFalse(SourceToggleGate.allowOn(SourceToggleId.SearchUnknowns, empty))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.Starred, ready))
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.SearchUnknowns, ready))
    }

    @Test
    fun customIndexNeedsValidatedUrl() {
        assertFalse(SourceToggleGate.allowOn(SourceToggleId.CustomFdroid, empty))
        assertFalse(
            SourceToggleGate.allowOn(
                SourceToggleId.CustomFdroid,
                SourceGateState(false, "http://127.0.0.1/index-v1.jar"),
            ),
        )
        assertTrue(SourceToggleGate.allowOn(SourceToggleId.CustomFdroid, ready))
    }

    @Test
    fun leftoverAndPasteValidateLocally() {
        assertFalse(SourceFieldValidate.leftoverToken("  "))
        assertFalse(SourceFieldValidate.leftoverToken("short"))
        assertTrue(SourceFieldValidate.leftoverToken("glpat-abc12345"))
        assertFalse(SourceFieldValidate.paste("org.app", "https://gitlab.com/org/app"))
        assertTrue(SourceFieldValidate.paste("org.app", "https://github.com/Acme/App"))
    }
}
