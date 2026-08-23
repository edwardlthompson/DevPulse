package dev.foss.goldenpath.index.play

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlaySourceHintsTest {
    @Before
    fun reset() {
        PlaySourceHints.clear()
    }

    @Test
    fun githubWebsiteBecomesReleaseHint() {
        PlaySourceHints.note("org.schabi.newpipe", "https://github.com/TeamNewPipe/NewPipe")
        assertEquals(
            "https://github.com/TeamNewPipe/NewPipe/releases",
            PlaySourceHints.snapshot()["org.schabi.newpipe"],
        )
    }

    @Test
    fun blankOrNonHttpsIsIgnored() {
        PlaySourceHints.note("app.x", "  ")
        PlaySourceHints.note("app.y", "http://example.com")
        assertEquals(emptyMap<String, String>(), PlaySourceHints.snapshot())
    }
}
