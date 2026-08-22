package dev.foss.goldenpath.index.forge

import dev.foss.goldenpath.index.fdroid.FdroidAppRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FdroidLeftoverHintsTest {
    @Test
    fun gitlabAndCodebergOnly() {
        val records = listOf(
            FdroidAppRecord("org.gitlab.app", 2L, "https://gitlab.com/acme/app", "official", "1.0"),
            FdroidAppRecord("org.codeberg.app", 3L, "https://codeberg.org/loop/app", "izzy", "2.0"),
            FdroidAppRecord("org.github.app", 1L, "https://github.com/acme/app", "official"),
        )
        val hints = FdroidLeftoverHints.hints(
            records,
            setOf("org.gitlab.app", "org.codeberg.app", "org.github.app"),
        )
        assertEquals(ForgeHost.GitLab, hints.getValue("org.gitlab.app").host)
        assertEquals("acme/app", hints.getValue("org.gitlab.app").ownerRepo)
        assertEquals(2L, hints.getValue("org.gitlab.app").ms)
        assertEquals(ForgeHost.Codeberg, hints.getValue("org.codeberg.app").host)
        assertNull(hints["org.github.app"])
    }

    @Test
    fun pastedGitlabFillsGap() {
        val merged = FdroidLeftoverHints.merge(
            emptyList(),
            setOf("org.pasted.app"),
            mapOf("org.pasted.app" to "https://gitlab.com/user/repo"),
        )
        assertEquals("user/repo", merged.getValue("org.pasted.app").ownerRepo)
    }
}
