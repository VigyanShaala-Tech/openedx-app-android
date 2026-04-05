package org.openedx.course.presentation.contenttab

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.CircularProgress
import org.openedx.core.ui.WebContentScreen
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.course.presentation.assignments.CourseContentAssignmentScreen
import org.openedx.course.presentation.container.CourseContentTab
import org.openedx.course.presentation.handouts.HandoutsType
import org.openedx.course.presentation.handouts.HandoutsUIState
import org.openedx.course.presentation.handouts.HandoutsViewModel
import org.openedx.course.presentation.home.CourseHomeViewModel
import org.openedx.course.presentation.home.LiveSessionsCardContent
import org.openedx.course.presentation.outline.CourseContentAllScreen
import org.openedx.course.presentation.videos.CourseContentVideoScreen
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun ContentTabScreen(
    viewModel: ContentTabViewModel,
    windowSize: WindowSize,
    fragmentManager: FragmentManager,
    courseId: String,
    courseName: String,
    pagerState: PagerState,
    onTabSelected: (CourseContentTab) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    ContentTabUI(
        windowSize = windowSize,
        pagerState = pagerState,
        onTabSelected = onTabSelected,
        onTabClicked = {
            viewModel.logTabClickEvent(it)
        },
        content = { page ->
            if (page < CourseContentTab.entries.size) {
                when (CourseContentTab.entries[page]) {
                    CourseContentTab.ALL -> CourseContentAllScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = {
                            parametersOf(
                                courseId,
                                courseName
                            )
                        }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome
                    )

                    CourseContentTab.VIDEOS -> CourseContentVideoScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = {
                            parametersOf(
                                courseId,
                                courseName
                            )
                        }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome
                    )

                    CourseContentTab.ASSIGNMENTS -> CourseContentAssignmentScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = { parametersOf(courseId) }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome
                    )

                    CourseContentTab.LIVE_SESSIONS -> {
                        val homeViewModel: CourseHomeViewModel = koinViewModel(
                            parameters = { parametersOf(courseId, courseName) }
                        )
                        val uiState = homeViewModel.uiState.collectAsState().value
                        if (uiState is org.openedx.course.presentation.home.CourseHomeUIState.CourseData) {
                            LiveSessionsCardContent(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = 16.dp),
                                uiState = uiState,
                                onJoinClick = { session ->
                                    // Handle join click
                                },
                                onViewAllLiveSessionsClick = {}
                            )
                        } else {
                            CircularProgress()
                        }
                    }

                    CourseContentTab.HANDOUTS -> {
                        val handoutsViewModel: HandoutsViewModel = koinViewModel(
                            parameters = { parametersOf(courseId, HandoutsType.Handouts.name) }
                        )
                        HandoutsContent(handoutsViewModel, windowSize)
                    }
                }
            }
        }
    )
}

@Composable
private fun ContentTabUI(
    windowSize: WindowSize,
    pagerState: PagerState,
    onTabSelected: (CourseContentTab) -> Unit = {},
    onTabClicked: (CourseContentTab) -> Unit = {},
    content: @Composable (Int) -> Unit
) {
    val tabsWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                compact = Modifier.fillMaxWidth()
            )
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < CourseContentTab.entries.size) {
            val selectedTab = CourseContentTab.entries[pagerState.currentPage]
            onTabSelected(selectedTab)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .then(tabsWidth)
                    .height(IntrinsicSize.Min)
                    .clip(MaterialTheme.appShapes.buttonShape)
                    .border(
                        1.dp,
                        MaterialTheme.appColors.primary,
                        MaterialTheme.appShapes.buttonShape
                    )
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CourseContentTab.entries.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected)
                                    MaterialTheme.appColors.primary
                                else
                                    MaterialTheme.appColors.background
                            )
                            .clickable {
                                scope.launch {
                                    pagerState.scrollToPage(index)
                                }
                                onTabClicked(tab)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(tab.labelResId),
                            color = if (isSelected)
                                MaterialTheme.appColors.primaryButtonText
                            else
                                MaterialTheme.appColors.primary,
                            style = MaterialTheme.typography.button.copy(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            maxLines = 1
                        )
                    }

                    if (index != CourseContentTab.entries.lastIndex) {
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = MaterialTheme.appColors.primary
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                beyondViewportPageCount = CourseContentTab.entries.size,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                content(page)
            }
        }
    }
}

@Composable
private fun HandoutsContent(
    viewModel: HandoutsViewModel,
    windowSize: WindowSize
) {
    val uiState by viewModel.uiState.collectAsState()
    HandoutsUI(
        uiState = uiState,
        windowSize = windowSize,
        apiHostUrl = viewModel.apiHostUrl,
        onInjectDarkMode = { content, colorBackgroundValue, colorTextValue ->
            viewModel.injectDarkMode(
                content,
                colorBackgroundValue,
                colorTextValue
            )
        }
    )
}

@Composable
private fun HandoutsUI(
    uiState: HandoutsUIState,
    windowSize: WindowSize,
    apiHostUrl: String,
    onInjectDarkMode: (String, ULong, ULong) -> String
) {
    val colorBackgroundValue = MaterialTheme.appColors.background.value
    val colorTextValue = MaterialTheme.appColors.textPrimary.value
    when (uiState) {
        is HandoutsUIState.Loading -> {
            CircularProgress()
        }

        is HandoutsUIState.HTMLContent -> {
            WebContentScreen(
                windowSize = windowSize,
                apiHostUrl = apiHostUrl,
                title = "",
                onBackClick = {},
                htmlBody = onInjectDarkMode(
                    uiState.htmlContent,
                    colorBackgroundValue,
                    colorTextValue
                )
            )
        }

        HandoutsUIState.Error -> {
            // Handle error or empty state
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContentTabScreenPreview() {
    OpenEdXTheme {
        ContentTabUI(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            pagerState = rememberPagerState(initialPage = 3) { CourseContentTab.entries.size },
            content = { page ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Page $page Content")
                }
            }
        )
    }
}
