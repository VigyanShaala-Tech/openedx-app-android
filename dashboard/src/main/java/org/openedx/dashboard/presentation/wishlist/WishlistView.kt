package org.openedx.dashboard.presentation.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as CoreR

@Composable
fun WishlistView(
    fragmentManager: FragmentManager
) {
    val viewModel: WishlistViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(null)

    WishlistView(
        state = uiState,
        uiMessage = uiMessage,
        onBack = { fragmentManager.popBackStack() },
        onRefresh = { viewModel.refresh() },
        onRemove = { courseId -> viewModel.remove(courseId) }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WishlistView(
    state: WishlistUIState,
    uiMessage: org.openedx.foundation.presentation.UIMessage?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: (String) -> Unit
) {
    val windowSize = rememberWindowSize()
    val scaffoldState = rememberScaffoldState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.refreshing,
        onRefresh = { onRefresh() }
    )

    val contentPadding by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = PaddingValues(vertical = 24.dp),
                compact = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            )
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = MaterialTheme.appColors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsInset()
                    .displayCutoutForLandscape()
                    .padding(
                        horizontal = contentPadding.calculateLeftPadding(
                            layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr
                        ),
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackBtn(tint = MaterialTheme.appColors.textDark) { onBack() }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "My Wishlist",
                    style = MaterialTheme.appTypography.titleLarge,
                    color = MaterialTheme.appColors.textDark
                )
            }
        }
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    state.loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.appColors.primary
                        )
                    }
                    state.items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Your wishlist is empty",
                                style = MaterialTheme.appTypography.titleMedium,
                                color = MaterialTheme.appColors.textPrimary
                            )
                        }
                    }
                    else -> {
                        val windowSize2 = rememberWindowSize()
                        val columns = if (windowSize2.isTablet) 3 else 2
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.Fixed(columns),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = contentPadding
                        ) {
                            items(state.items) { item ->
                                WishlistGridItem(item, onRemove)
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    state.refreshing,
                    pullRefreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun WishlistGridItem(item: CourseItemDto, onRemove: (String) -> Unit) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(MaterialTheme.appShapes.cardShape),
                    contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.course_image)
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                )
                androidx.compose.material.IconButton(
                    onClick = { onRemove(item.id) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    androidx.compose.material.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textPrimary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.appTypography.titleSmall,
                color = MaterialTheme.appColors.textDark,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "4.5",
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textDark
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = (item.level ?: "").trim(),
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimary
                )
            }
        }
    }
}

@Preview
@Composable
private fun WishlistPreview() {
    OpenEdXTheme {
        WishlistView(
            state = WishlistUIState(
                loading = false,
                items = listOf(
                    CourseItemDto("1", "Android Development", "", 0, "Mobile", "Beginner"),
                    CourseItemDto("2", "Kotlin Fundamentals", "", 0, "Programming", "Intermediate")
                )
            ),
            uiMessage = null,
            onBack = {},
            onRefresh = {},
            onRemove = {}
        )
    }
}
