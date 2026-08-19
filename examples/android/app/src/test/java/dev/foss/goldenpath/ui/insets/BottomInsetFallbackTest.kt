package dev.foss.goldenpath.ui.insets

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomInsetFallbackTest {
    @Test
    fun threeButton_zeroInset_uses48dpMinimum() {
        val px = BottomInsetCalculator.effectiveBottomPaddingPx(
            measuredInsetPx = 0,
            navigationMode = NavigationMode.ThreeButton,
            density = 2f,
        )
        assertEquals(96, px)
    }

    @Test
    fun threeButton_largeInset_usesMeasured() {
        val px = BottomInsetCalculator.effectiveBottomPaddingPx(
            measuredInsetPx = 120,
            navigationMode = NavigationMode.ThreeButton,
            density = 2f,
        )
        assertEquals(120, px)
    }

    @Test
    fun gesture_zeroInset_staysZero() {
        val px = BottomInsetCalculator.effectiveBottomPaddingPx(
            measuredInsetPx = 0,
            navigationMode = NavigationMode.Gesture,
            density = 2f,
        )
        assertEquals(0, px)
    }

    @Test
    fun unknown_zeroInset_staysZero() {
        val px = BottomInsetCalculator.effectiveBottomPaddingPx(
            measuredInsetPx = 0,
            navigationMode = NavigationMode.Unknown,
            density = 3f,
        )
        assertEquals(0, px)
    }
}
