package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUpdateOwnershipTest {
    @Test
    fun skipsConfirmOnlyForOwnedUpdateOnApi31() {
        assertTrue(
            SessionUpdateOwnership.requestNoUserAction(31, true, "app.devpulse", "app.devpulse"),
        )
        assertFalse(
            SessionUpdateOwnership.requestNoUserAction(30, true, "app.devpulse", "app.devpulse"),
        )
        assertFalse(
            SessionUpdateOwnership.requestNoUserAction(31, false, "app.devpulse", "app.devpulse"),
        )
        assertFalse(
            SessionUpdateOwnership.requestNoUserAction(31, true, "com.android.vending", "app.devpulse"),
        )
        assertFalse(
            SessionUpdateOwnership.requestNoUserAction(31, true, null, "app.devpulse"),
        )
        assertFalse(
            SessionUpdateOwnership.requestNoUserAction(31, true, "app.devpulse", ""),
        )
    }
}
