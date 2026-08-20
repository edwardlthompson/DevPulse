package dev.foss.goldenpath.ui.refresh

import androidx.compose.ui.Modifier
import androidx.compose.ui.FrameRateCategory
import androidx.compose.ui.preferredFrameRate

fun Modifier.highRefreshScroll(): Modifier = preferredFrameRate(FrameRateCategory.High)
