package dev.foss.goldenpath.ui.insets

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import dev.foss.goldenpath.BuildConfig

val LocalNavigationMode = compositionLocalOf { NavigationMode.Unknown }

@Composable
fun NavigationModeProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val mode = remember(context) { context.readNavigationMode() }
    if (BuildConfig.DEBUG) {
        val density = LocalDensity.current
        val measured = WindowInsets.navigationBars.getBottom(density)
        Log.d("GoldenPath", "navMode=$mode navInsetBottom=${measured}px")
    }
    CompositionLocalProvider(LocalNavigationMode provides mode) {
        content()
    }
}

@Composable
fun rememberBottomInsetPadding(): PaddingValues {
    val mode = LocalNavigationMode.current
    val density = LocalDensity.current
    val insets = WindowInsets.navigationBars.union(WindowInsets.mandatorySystemGestures)
    val measured = insets.getBottom(density)
    val effectivePx = BottomInsetCalculator.effectiveBottomPaddingPx(
        measuredInsetPx = measured,
        navigationMode = mode,
        density = density.density,
    )
    return PaddingValues(bottom = with(density) { effectivePx.toDp() })
}

fun Modifier.bottomInsetPadding(): Modifier = composed {
    val bottom = rememberBottomInsetPadding().calculateBottomPadding()
    this.padding(bottom = bottom)
}

@Composable
fun navigationBarInsetBottomDp(): Int {
    val density = LocalDensity.current
    return WindowInsets.navigationBars.getBottom(density).let { px ->
        with(density) { px.toDp().value.toInt() }
    }
}

@Composable
fun navigationModeLabelRes(mode: NavigationMode): Int = when (mode) {
    NavigationMode.ThreeButton -> dev.foss.goldenpath.R.string.nav_mode_three_button
    NavigationMode.TwoButton -> dev.foss.goldenpath.R.string.nav_mode_two_button
    NavigationMode.Gesture -> dev.foss.goldenpath.R.string.nav_mode_gesture
    NavigationMode.Unknown -> dev.foss.goldenpath.R.string.nav_mode_unknown
}
