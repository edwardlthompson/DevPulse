package dev.foss.goldenpath.index.forge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GithubAppOptTest {
    @Test
    fun overlongAndInvalidRegexAreIgnored() {
        assertNull(GithubAppOptCodec.regexOrNull("a".repeat(GithubAppOptCodec.MAX_REGEX + 1)))
        assertNull(GithubAppOptCodec.regexOrNull("("))
        assertNull(GithubAppOptCodec.regexOrNull(""))
        assertEquals("arm64", GithubAppOptCodec.regexOrNull("arm64")?.pattern)
    }

    @Test
    fun filenameIgnoresHostPath() {
        assertEquals(
            "app-arm64.apk",
            GithubAppOptCodec.filename("https://github.com/o/r/releases/download/v1/app-arm64.apk?sig=1"),
        )
    }
}
