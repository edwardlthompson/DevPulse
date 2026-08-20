package dev.foss.goldenpath.inventory

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueryAllPackagesGateTest {
    @Test
    fun explainsOnAndroid11AndAbove() {
        assertFalse(QueryAllPackagesGate.mustExplain(29))
        assertTrue(QueryAllPackagesGate.mustExplain(30))
        assertTrue(QueryAllPackagesGate.mustExplain(36))
    }

    @Test
    fun blocksScanUntilAcknowledgedOnAndroid11() {
        assertFalse(QueryAllPackagesGate.canScan(acknowledged = false, sdkInt = 30))
        assertTrue(QueryAllPackagesGate.canScan(acknowledged = true, sdkInt = 30))
    }

    @Test
    fun allowsScanWithoutAckBelowAndroid11() {
        assertTrue(QueryAllPackagesGate.canScan(acknowledged = false, sdkInt = 29))
    }
}
