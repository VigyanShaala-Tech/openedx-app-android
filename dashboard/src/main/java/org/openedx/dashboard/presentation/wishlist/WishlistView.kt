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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
        onRefresh = { viewModel.refresh() }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WishlistView(
    state: WishlistUIState,
    uiMessage: org.openedx.foundation.presentation.UIMessage?,
    onBack: () -> Unit,
    onRefresh: () -> Unit
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
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutForLandscape()
                .padding(paddingValues),
        ) {
            BackBtn(
                modifier = Modifier.padding(horizontal = contentPadding.calculateLeftPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr)),
                tint = MaterialTheme.appColors.textDark
            ) { onBack() }

            Text(
                modifier = Modifier
                    .padding(horizontal = contentPadding.calculateLeftPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr))
                    .padding(vertical = 8.dp),
                text = "Wishlist",
                style = MaterialTheme.appTypography.headlineBold,
                color = MaterialTheme.appColors.textDark
            )

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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = contentPadding,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items.size) { index ->
                                WishlistItem(state.items[index])
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
private fun WishlistItem(item: CourseItemDto) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(64.dp)
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
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.appTypography.titleSmall,
                    color = MaterialTheme.appColors.textDark,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = (item.level ?: "").trim(),
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimary
                )
            }
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier.clickable { }
            )
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
            onRefresh = {}
        )
    }
}
