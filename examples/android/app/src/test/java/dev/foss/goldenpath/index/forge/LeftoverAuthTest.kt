package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LeftoverAuthTest {
    @Test
    fun gitlabAndCodebergHeaders() {
        assertEquals("PRIVATE-TOKEN" to "glpat-x", LeftoverAuth.header(ForgeHost.GitLab, " glpat-x "))
        assertEquals("Authorization" to "token cb", LeftoverAuth.header(ForgeHost.Codeberg, "cb"))
        assertNull(LeftoverAuth.header(ForgeHost.GitLab, "  "))
        assertNull(LeftoverAuth.header(ForgeHost.GitHub, "ghp"))
    }
}
