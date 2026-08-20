package dev.foss.goldenpath.index.play

import org.junit.Assert.assertTrue
import org.junit.Test

class PlayFetchPolicyTest {
    @Test
    fun usesMobileChromeUserAgent() {
        assertTrue(PlayFetchPolicy.USER_AGENT.contains("Chrome/"))
        assertTrue(PlayFetchPolicy.USER_AGENT.contains("Mobile"))
        assertTrue(PlayFetchPolicy.ACCEPT_LANGUAGE.startsWith("en"))
    }
}
