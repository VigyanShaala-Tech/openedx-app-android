package org.openedx.dashboard.presentation.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Book
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.dashboard.data.model.AchievementStatDto
import org.openedx.dashboard.data.model.BadgeProgressDto
import org.openedx.dashboard.data.model.EarnedBadgeDto
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import androidx.compose.ui.res.stringResource
import org.openedx.dashboard.R
import org.openedx.core.R as CoreR

@Composable
fun AchievementsView(
    fragmentManager: FragmentManager
) {
    val viewModel: AchievementsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(null)

    AchievementsView(
        state = uiState,
        uiMessage = uiMessage,
        onBack = { fragmentManager.popBackStack() },
        onRefresh = { viewModel.refresh() }
    )
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun AchievementsView(
    state: AchievementsUIState,
    uiMessage: org.openedx.foundation.presentation.UIMessage?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
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
                    text = stringResource(R.string.dashboard_my_achievements),
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
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = contentPadding
                        ) {
                            if (state.stats.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        state.stats.take(3).forEach { s ->
                                            val icon = when (s.icon) {
                                                "faAward" -> Icons.Filled.EmojiEvents
                                                "faTrophy" -> Icons.Filled.EmojiEvents
                                                "faCheckCircle" -> Icons.Filled.CheckCircle
                                                else -> Icons.Filled.Alarm
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                StatCard(icon, s.number.toString(), s.label)
                                            }
                                        }
                                    }
                                }
                            }
                            if (state.earnedBadges.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.dashboard_earned_badges),
                                        style = MaterialTheme.appTypography.titleMedium,
                                        color = MaterialTheme.appColors.textDark
                                    )
                                }
                                item {
                                    val columns = if (windowSize.isTablet) 3 else 2
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        state.earnedBadges.chunked(columns).forEach { row ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                row.forEach { b ->
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        EarnedBadgeCard(b)
                                                    }
                                                }
                                                if (row.size < columns) {
                                                    repeat(columns - row.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (state.badgesInProgress.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.dashboard_badges_in_progress),
                                        style = MaterialTheme.appTypography.titleMedium,
                                        color = MaterialTheme.appColors.textDark
                                    )
                                }
                                items(state.badgesInProgress.size) { index ->
                                    BadgeProgressItem(state.badgesInProgress[index])
                                }
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
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Card(
        backgroundColor = MaterialTheme.appColors.background,
        elevation = 4.dp,
        shape = MaterialTheme.appShapes.cardShape,
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.appColors.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.appTypography.titleMedium,
                    color = MaterialTheme.appColors.textDark
                )
                Text(
                    text = label,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = Int.MAX_VALUE
                )
            }
        }
    }
}

@Composable
private fun EarnedBadgeCard(badge: EarnedBadgeDto) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.appShapes.cardShape),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(sanitizeUrl(badge.icon_url))
                    .error(CoreR.drawable.core_no_image_course)
                    .placeholder(CoreR.drawable.core_no_image_course)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = badge.title,
                style = MaterialTheme.appTypography.titleSmall,
                color = MaterialTheme.appColors.textDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = badge.description,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BadgeProgressItem(item: BadgeProgressDto) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.appColors.primary.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        item.title.contains("Hour", true) -> Icons.Filled.Alarm
                        item.title.contains("Research", true) -> Icons.Filled.Book
                        item.title.contains("Community", true) -> Icons.Filled.EmojiEvents
                        item.title.contains("Course", true) -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.EmojiEvents
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.appTypography.titleSmall,
                        color = MaterialTheme.appColors.textDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    val p = (item.progress ?: 0).coerceIn(0, 100)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = p / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50)),
                            color = MaterialTheme.appColors.primary,
                            backgroundColor = MaterialTheme.appColors.progressBarBackgroundColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$p%",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = Color(0xFF7A7A7A)
                        )
                    }
                }
            }
        }
    }
}

private fun sanitizeUrl(url: String?): String {
    return url?.replace("`", "")?.trim() ?: ""
}
