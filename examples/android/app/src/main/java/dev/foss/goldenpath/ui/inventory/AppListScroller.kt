package dev.foss.goldenpath.ui.inventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.foss.goldenpath.inventory.InstalledApp
import dev.foss.goldenpath.ui.refresh.highRefreshScroll
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppListScroller(
    apps: List<InstalledApp>,
    listState: LazyListState,
    selected: Set<String>,
    onToggleSelect: (String) -> Unit,
    onOpen: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }
    var scrubIndex by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val keypoints = remember(apps) { AppYearScrubber.findYearKeypoints(apps) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val thumbHeightDp = 48.dp
        val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
        val availableTrackHeightPx = (maxHeightPx - thumbHeightPx).coerceAtLeast(1f)

        val scrollbarFraction by remember(apps.size) {
            derivedStateOf {
                if (isDragging) {
                    scrubFraction
                } else {
                    val total = apps.size
                    val firstVisible = listState.firstVisibleItemIndex
                    val visibleInfo = listState.layoutInfo.visibleItemsInfo
                    val visibleCount = visibleInfo.size.coerceAtLeast(1)
                    if (total <= visibleCount) {
                        0f
                    } else {
                        (firstVisible.toFloat() / (total - visibleCount)).coerceIn(0f, 1f)
                    }
                }
            }
        }

        val activeIndex by remember(apps.size) {
            derivedStateOf {
                if (isDragging) {
                    scrubIndex
                } else {
                    listState.firstVisibleItemIndex.coerceIn(0, (apps.size - 1).coerceAtLeast(0))
                }
            }
        }

        val currentYearText = remember(activeIndex, apps) {
            AppYearScrubber.yearForIndex(apps, activeIndex)
        }

        // Main App List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp)
                .highRefreshScroll(),
            state = listState,
        ) {
            items(apps, key = { it.packageName }) { app ->
                InventoryRow(
                    app = app,
                    selected = app.packageName in selected,
                    onToggleSelect = { onToggleSelect(app.packageName) },
                    onOpen = { onOpen(app) },
                )
            }
        }

        // Scrubbable Scrollbar Rail & Thumb
        if (apps.size > 1) {
            val thumbYOffsetPx = (scrollbarFraction * availableTrackHeightPx).coerceIn(0f, availableTrackHeightPx)
            val thumbYOffsetDp = with(density) { thumbYOffsetPx.toDp() }

            // Year Callout Bubble
            AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        val bubbleY = (thumbYOffsetPx - 16 * density.density)
                            .coerceIn(0f, maxHeightPx - 64 * density.density)
                        IntOffset(x = -with(density) { 36.dp.roundToPx() }, y = bubbleY.roundToInt())
                    },
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = currentYearText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Interactive Scrollbar Track
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(28.dp)
                    .pointerInput(apps.size, availableTrackHeightPx) {
                        detectTapGestures(
                            onPress = { offset ->
                                isDragging = true
                                val fraction = (offset.y / maxHeightPx).coerceIn(0f, 1f)
                                scrubFraction = fraction
                                val target = AppYearScrubber.targetIndexForFraction(fraction, apps.size)
                                scrubIndex = target
                                coroutineScope.launch { listState.scrollToItem(target) }
                                tryAwaitRelease()
                                isDragging = false
                            },
                        )
                    }
                    .pointerInput(apps.size, availableTrackHeightPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val fraction = (offset.y / maxHeightPx).coerceIn(0f, 1f)
                                scrubFraction = fraction
                                val target = AppYearScrubber.targetIndexForFraction(fraction, apps.size)
                                scrubIndex = target
                                coroutineScope.launch { listState.scrollToItem(target) }
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val fraction = (change.position.y / maxHeightPx).coerceIn(0f, 1f)
                                scrubFraction = fraction
                                val target = AppYearScrubber.targetIndexForFraction(fraction, apps.size)
                                scrubIndex = target
                                coroutineScope.launch { listState.scrollToItem(target) }
                            },
                        )
                    },
            ) {
                // Subtle Track Background
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                )

                // Year Ticks along the track
                if (keypoints.size in 2..15) {
                    keypoints.forEach { kp ->
                        val tickY = with(density) { (kp.fraction * maxHeightPx).toDp() }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(y = tickY)
                                .size(width = 6.dp, height = 2.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                }

                // Draggable Thumb
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = thumbYOffsetDp)
                        .width(if (isDragging) 8.dp else 5.dp)
                        .height(thumbHeightDp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isDragging) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            },
                        ),
                )
            }
        }
    }
}
