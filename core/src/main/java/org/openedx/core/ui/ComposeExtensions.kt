package org.openedx.core.ui

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.core.R
import org.openedx.core.presentation.global.InsetHolder

const val KEYBOARD_VISIBILITY_THRESHOLD = 0.15f

inline val isPreview: Boolean
    @ReadOnlyComposable
    @Composable
    get() {
        return LocalInspectionMode.current
    }

@Stable
inline val Int.px: Float
    @Composable
    get() {
        return this.toFloat().px
    }

@Stable
inline val Float.px: Float
    @Composable
    get() {
        val density = LocalDensity.current.density
        return this * density
    }

fun LazyListState.shouldLoadMore(rememberedIndex: MutableState<Int>, threshold: Int): Boolean {
    val firstVisibleIndex = this.firstVisibleItemIndex
    if (rememberedIndex.value != firstVisibleIndex) {
        rememberedIndex.value = firstVisibleIndex
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        return lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - threshold
    }
    return false
}

fun LazyGridState.shouldLoadMore(rememberedIndex: MutableState<Int>, threshold: Int): Boolean {
    val firstVisibleIndex = this.firstVisibleItemIndex
    if (rememberedIndex.value != firstVisibleIndex) {
        rememberedIndex.value = firstVisibleIndex
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        return lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - threshold
    }
    return false
}

fun Modifier.statusBarsInset(): Modifier = composed {
    val topInset = (LocalContext.current as? InsetHolder)?.topInset ?: 0
    return@composed this
        .padding(top = with(LocalDensity.current) { topInset.toDp() })
}

fun Modifier.navigationBarsInset(): Modifier = composed {
    val bottomInset = (LocalContext.current as? InsetHolder)?.bottomInset ?: 0
    return@composed this
        .padding(bottom = with(LocalDensity.current) { bottomInset.toDp() })
}

fun Modifier.displayCutoutForLandscape(): Modifier = composed {
    val cutoutInset = (LocalContext.current as? InsetHolder)?.cutoutInset ?: 0
    val cutoutInsetDp = with(LocalDensity.current) { cutoutInset.toDp() }
    return@composed if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this.padding(horizontal = cutoutInsetDp)
    } else {
        this
    }
}

inline fun Modifier.noRippleClickable(crossinline onClick: () -> Unit): Modifier = composed {
    this then Modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

fun Modifier.roundBorderWithoutBottom(borderWidth: Dp, cornerRadius: Dp): Modifier = composed(
    factory = {
        var path: Path
        this.then(
            Modifier.drawWithCache {
                val height = this.size.height
                val width = this.size.width
                onDrawWithContent {
                    drawContent()
                    path = Path().apply {
                        moveTo(width.times(0f), height.times(1f))
                        lineTo(width.times(0f), height.times(0f))
                        lineTo(width.times(1f), height.times(0f))
                        lineTo(width.times(1f), height.times(1f))
                    }
                    drawPath(
                        path = path,
                        color = Color.LightGray,
                        style = Stroke(
                            width = borderWidth.toPx(),
                            pathEffect = PathEffect.cornerPathEffect(cornerRadius.toPx())
                        )
                    )
                }
            }
        )
    }
)

@Composable
fun <T : Any> rememberSaveableMap(init: () -> MutableMap<String, T?>): MutableMap<String, T?> {
    return rememberSaveable(
        saver = mapSaver(
            save = {
                it.toMap()
            },
            restore = {
                it.toMutableMap() as MutableMap<String, T?>
            }
        )
    ) {
        init()
    }
}

@Composable
fun isImeVisibleState(threshold: Int = 0): State<Boolean> {
    val imeInsets = WindowInsets.ime
    val imeBottom = imeInsets.getBottom(LocalDensity.current)
    val isOpen = remember(imeBottom) { mutableStateOf(false) }

    LaunchedEffect(imeBottom) {
        isOpen.value = imeBottom > threshold
    }

    return isOpen
}

fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}

fun Modifier.settingsHeaderBackground(): Modifier = composed {
    return@composed this
        .paint(
            painter = painterResource(id = R.drawable.core_green_gradient_rect),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter
        )
}

fun Modifier.crop(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    fun Dp.toPxInt(): Int = this.toPx().toInt()

    layout(
        placeable.width - (horizontal * 2).toPxInt(),
        placeable.height - (vertical * 2).toPxInt()
    ) {
        placeable.placeRelative(-horizontal.toPx().toInt(), -vertical.toPx().toInt())
    }
}

fun Modifier.horizontalScrollbar(
    state: LazyListState,
    color: Color,
    thickness: Dp = 2.dp
): Modifier = composed {
    val density = LocalDensity.current
    val thicknessPx = with(density) { thickness.toPx() }

    this.drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isEmpty()) return@drawWithContent

        val totalItemsCount = layoutInfo.totalItemsCount
        val viewportSize = layoutInfo.viewportSize.width.toFloat()

        val averageItemSize = visibleItemsInfo.map { it.size }.average().toFloat()
        val estimatedTotalWidth =
            totalItemsCount * averageItemSize + (totalItemsCount - 1) * layoutInfo.mainAxisItemSpacing

        if (estimatedTotalWidth > viewportSize) {
            val scrollbarAreaWidth = viewportSize - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
            val scrollbarWidth = (viewportSize / estimatedTotalWidth) * scrollbarAreaWidth
            val scrollOffset =
                state.firstVisibleItemIndex * (averageItemSize + layoutInfo.mainAxisItemSpacing) + state.firstVisibleItemScrollOffset
            val scrollbarOffset = layoutInfo.beforeContentPadding + (scrollOffset / estimatedTotalWidth) * scrollbarAreaWidth

            // Draw track
            drawRect(
                color = color.copy(alpha = 0.1f),
                topLeft = Offset(layoutInfo.beforeContentPadding.toFloat(), size.height - thicknessPx),
                size = Size(scrollbarAreaWidth, thicknessPx)
            )

            // Draw thumb
            drawRect(
                color = color,
                topLeft = Offset(scrollbarOffset.coerceIn(layoutInfo.beforeContentPadding.toFloat(), viewportSize - layoutInfo.afterContentPadding - scrollbarWidth), size.height - thicknessPx),
                size = Size(scrollbarWidth, thicknessPx),
                alpha = 0.6f
            )
        }
    }
}

fun Modifier.horizontalScrollbar(
    state: ScrollState,
    color: Color,
    thickness: Dp = 3.dp
): Modifier = composed {
    val density = LocalDensity.current
    val thicknessPx = with(density) { thickness.toPx() }

    this.drawWithContent {
        drawContent()

        val maxValue = state.maxValue
        if (maxValue > 0) {
            val viewportWidth = size.width
            val totalWidth = viewportWidth + maxValue
            val scrollbarWidth = ((viewportWidth / totalWidth) * viewportWidth).coerceAtLeast(24.dp.toPx())
            val scrollbarOffset = (state.value.toFloat() / maxValue.toFloat()) * (viewportWidth - scrollbarWidth)

            // Draw track
            drawRect(
                color = color.copy(alpha = 0.2f),
                topLeft = Offset(0f, size.height - thicknessPx),
                size = Size(viewportWidth, thicknessPx)
            )

            // Draw thumb
            drawRect(
                color = color,
                topLeft = Offset(scrollbarOffset.coerceIn(0f, viewportWidth - scrollbarWidth), size.height - thicknessPx),
                size = Size(scrollbarWidth, thicknessPx),
                alpha = 0.8f
            )
        }
    }
}

@Composable
fun HorizontalScrollbar(
    scrollState: ScrollState,
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 3.dp
) {
    if (scrollState.maxValue > 0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(thickness)
                .drawWithContent {
                    val maxValue = scrollState.maxValue
                    val viewportWidth = size.width
                    val totalWidth = viewportWidth + maxValue
                    val scrollbarWidth = ((viewportWidth / totalWidth) * viewportWidth).coerceAtLeast(24.dp.toPx())
                    val scrollbarOffset = (scrollState.value.toFloat() / maxValue.toFloat()) * (viewportWidth - scrollbarWidth)

                    // Draw track
                    drawRect(
                        color = color.copy(alpha = 0.15f),
                        topLeft = Offset(0f, 0f),
                        size = Size(viewportWidth, size.height)
                    )

                    // Draw thumb
                    drawRect(
                        color = color,
                        topLeft = Offset(scrollbarOffset.coerceIn(0f, viewportWidth - scrollbarWidth), 0f),
                        size = Size(scrollbarWidth, size.height),
                        alpha = 0.8f
                    )
                }
        )
    }
}

@Composable
fun HorizontalLazyListScrollbar(
    state: LazyListState,
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 3.dp
) {
    val layoutInfo = state.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    if (visibleItemsInfo.isNotEmpty()) {
        val totalItemsCount = layoutInfo.totalItemsCount
        val viewportSize = layoutInfo.viewportSize.width.toFloat()
        val averageItemSize = visibleItemsInfo.map { it.size }.average().toFloat()
        val estimatedTotalWidth =
            totalItemsCount * averageItemSize + (totalItemsCount - 1) * layoutInfo.mainAxisItemSpacing

        if (estimatedTotalWidth > viewportSize) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(thickness)
                    .drawWithContent {
                        val scrollbarAreaWidth = viewportSize - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
                        val scrollbarWidth = ((viewportSize / estimatedTotalWidth) * scrollbarAreaWidth).coerceAtLeast(24.dp.toPx())
                        val scrollOffset =
                            state.firstVisibleItemIndex * (averageItemSize + layoutInfo.mainAxisItemSpacing) + state.firstVisibleItemScrollOffset
                        val scrollbarOffset = layoutInfo.beforeContentPadding + (scrollOffset / estimatedTotalWidth) * scrollbarAreaWidth

                        // Draw track
                        drawRect(
                            color = color.copy(alpha = 0.15f),
                            topLeft = Offset(layoutInfo.beforeContentPadding.toFloat(), 0f),
                            size = Size(scrollbarAreaWidth, size.height)
                        )

                        // Draw thumb
                        drawRect(
                            color = color,
                            topLeft = Offset(
                                scrollbarOffset.coerceIn(
                                    layoutInfo.beforeContentPadding.toFloat(),
                                    viewportSize - layoutInfo.afterContentPadding - scrollbarWidth
                                ),
                                0f
                            ),
                            size = Size(scrollbarWidth, size.height),
                            alpha = 0.8f
                        )
                    }
            )
        }
    }
}

fun Modifier.leftVerticalScrollbar(
    state: LazyListState,
    color: Color,
    thickness: Dp = 4.dp,
    padding: Dp = 4.dp
): Modifier = composed {
    val density = LocalDensity.current
    val thicknessPx = with(density) { thickness.toPx() }
    val paddingPx = with(density) { padding.toPx() }

    this.drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isEmpty()) return@drawWithContent

        val totalItemsCount = layoutInfo.totalItemsCount
        val viewportHeight = layoutInfo.viewportSize.height.toFloat()

        val averageItemSize = visibleItemsInfo.map { it.size }.average().toFloat()
        val estimatedTotalHeight =
            totalItemsCount * averageItemSize + (totalItemsCount - 1) * layoutInfo.mainAxisItemSpacing

        if (estimatedTotalHeight > viewportHeight) {
            val scrollbarAreaHeight = viewportHeight - layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
            val scrollbarHeight = ((viewportHeight / estimatedTotalHeight) * scrollbarAreaHeight).coerceAtLeast(24.dp.toPx())
            val scrollOffset =
                state.firstVisibleItemIndex * (averageItemSize + layoutInfo.mainAxisItemSpacing) + state.firstVisibleItemScrollOffset
            val scrollbarOffset = layoutInfo.beforeContentPadding + (scrollOffset / estimatedTotalHeight) * scrollbarAreaHeight

            // Draw track on left side
            drawRect(
                color = color.copy(alpha = 0.15f),
                topLeft = Offset(paddingPx, layoutInfo.beforeContentPadding.toFloat()),
                size = Size(thicknessPx, scrollbarAreaHeight)
            )

            // Draw thumb on left side
            drawRect(
                color = color,
                topLeft = Offset(
                    paddingPx,
                    scrollbarOffset.coerceIn(
                        layoutInfo.beforeContentPadding.toFloat(),
                        viewportHeight - layoutInfo.afterContentPadding - scrollbarHeight
                    )
                ),
                size = Size(thicknessPx, scrollbarHeight),
                alpha = 0.7f
            )
        }
    }
}
