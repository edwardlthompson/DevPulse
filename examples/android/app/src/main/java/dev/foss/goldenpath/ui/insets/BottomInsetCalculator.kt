package dev.foss.goldenpath.ui.insets

/** Pure inset fallback math — unit-tested without Compose or device. */
object BottomInsetCalculator {
    const val THREE_BUTTON_MIN_DP = 48

    fun effectiveBottomPaddingPx(
        measuredInsetPx: Int,
        navigationMode: NavigationMode,
        density: Float,
    ): Int {
        val modeMinPx = when (navigationMode) {
            NavigationMode.ThreeButton -> (THREE_BUTTON_MIN_DP * density).toInt()
            NavigationMode.TwoButton -> (THREE_BUTTON_MIN_DP * density).toInt()
            NavigationMode.Gesture,
            NavigationMode.Unknown,
            -> 0
        }
        return maxOf(measuredInsetPx, modeMinPx)
    }
}
