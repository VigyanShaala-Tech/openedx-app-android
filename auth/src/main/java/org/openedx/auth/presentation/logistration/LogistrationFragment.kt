package org.openedx.auth.presentation.logistration

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.auth.R
import org.openedx.core.ApiConstants
import org.openedx.core.domain.model.Media
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.SearchBar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.shouldLoadMore
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.compose.LogistrationLogoView
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryUIState
import org.openedx.discovery.presentation.ui.DiscoveryCourseItem
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.foundation.utils.UrlUtils
import org.openedx.auth.presentation.logistration.LogistrationCarousel
import org.openedx.auth.presentation.logistration.LogistrationCarouselItem
import org.openedx.auth.presentation.logistration.LogistrationFilters
import org.openedx.core.R as CoreR

class LogistrationFragment : Fragment() {

    private val viewModel: LogistrationViewModel by viewModel {
        parametersOf(arguments?.getString(ARG_COURSE_ID, "") ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState()
                val uiMessage by viewModel.uiMessage.observeAsState()
                val canLoadMore by viewModel.canLoadMore.observeAsState(false)
                val refreshing by viewModel.isUpdating.observeAsState(false)

                LogistrationScreen(
                    windowSize = windowSize,
                    state = uiState!!,
                    uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl,
                    canLoadMore = canLoadMore,
                    refreshing = refreshing,
                    hasInternetConnection = viewModel.hasInternetConnection,
                    onSearchSubmit = { query ->
                        viewModel.searchCatalogCourses(searchTerm = query)
                    },
                    onFiltersChanged = { selected ->
                        viewModel.searchCatalogCourses(selected = selected)
                    },
                    onSignInClick = {
                        if (viewModel.isBrowserLoginEnabled) {
                            viewModel.signInBrowser(requireActivity())
                        } else {
                            viewModel.navigateToSignIn(parentFragmentManager)
                        }
                    },
                    onRegisterClick = {
                        if (viewModel.isBrowserRegistrationEnabled) {
                            UrlUtils.openInBrowser(
                                activity = context,
                                apiHostUrl = viewModel.apiHostUrl,
                                url = ApiConstants.URL_REGISTER_BROWSER,
                            )
                        } else {
                            viewModel.navigateToSignUp(parentFragmentManager)
                        }
                    },
                    onSearchClick = { querySearch ->
                        viewModel.navigateToDiscovery(parentFragmentManager, querySearch)
                    },
                    paginationCallback = {
                        viewModel.fetchMore()
                    },
                    onSwipeRefresh = {
                        viewModel.updateData()
                    },
                    onReloadClick = {
                        viewModel.getCoursesList()
                    },
                    onItemClick = { course ->
                        viewModel.courseDetailClicked(course.id, course.name)
                        viewModel.courseDetailClickedEvent(course.id, course.name)
                        viewModel.navigateToCourseDetail(parentFragmentManager, course.id)
                        // Note: router might need to be DiscoveryRouter or AuthRouter might need to handle this
                        // For now we navigate to discovery search with the course name as a workaround or 
                        // if we want to navigate to detail, we need the right router.
                    },
                    isRegistrationEnabled = viewModel.isRegistrationEnabled
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        fun newInstance(courseId: String?): LogistrationFragment {
            val fragment = LogistrationFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId
            )
            return fragment
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun LogistrationScreen(
    windowSize: WindowSize,
    state: DiscoveryUIState,
    uiMessage: UIMessage?,
    apiHostUrl: String,
    canLoadMore: Boolean,
    refreshing: Boolean,
    hasInternetConnection: Boolean,
    onSearchSubmit: (String) -> Unit,
    onFiltersChanged: (Map<String, String>) -> Unit,
    onSearchClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
    paginationCallback: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onReloadClick: () -> Unit,
    onItemClick: (org.openedx.discovery.domain.model.Course) -> Unit,
    isRegistrationEnabled: Boolean,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberLazyListState()
    val firstVisibleIndex = remember {
        mutableIntStateOf(scrollState.firstVisibleItemIndex)
    }
    val pullRefreshState =
        rememberPullRefreshState(refreshing = refreshing, onRefresh = { onSwipeRefresh() })

    var isInternetConnectionShown by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = MaterialTheme.appColors.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 32.dp,
                    )
                    .navigationBarsPadding()
            ) {
                AuthButtonsPanel(
                    onRegisterClick = onRegisterClick,
                    onSignInClick = onSignInClick,
                    showRegisterButton = isRegistrationEnabled
                )
            }
        }
    ) {
        val contentWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(max= 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val contentPaddings by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = PaddingValues(
                        top = 32.dp,
                        bottom = 40.dp
                    ),
                    compact = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .displayCutoutForLandscape()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopCenter)
                    .then(contentWidth),
                contentPadding = contentPaddings,
                state = scrollState
            ) {
                item {
                    Column {
                        LogistrationLogoView()
                        val carouselItems = listOf(
                            LogistrationCarouselItem(
                                imageResId = R.drawable.onboarding_1,
                                title = "Creating Job Opportunities in Science and Technology",
                                subtitle = "Discover the latest job opportunities in Science and Technology and apply for the ones that match your skills and interests."
                            ),
                            LogistrationCarouselItem(
                                imageResId = R.drawable.onboarding_2,
                                title = "Learn from Industry Experts",
                                subtitle = "Access world-class education from renowned instructors and gain practical knowledge that sets you apart."
                            ),
                            LogistrationCarouselItem(
                                imageResId = R.drawable.onboarding_3,
                                title = "Grow your career in STEM",
                                subtitle = "Be part of an inspiring global community and make your next leap into higher education or a job."
                            )
                        )
                        LogistrationCarousel(items = carouselItems)
//                        Text(
//                            text = stringResource(id = R.string.pre_auth_title),
//                            style = MaterialTheme.appTypography.headlineSmall,
//                            modifier = Modifier
//                                .testTag("txt_screen_title")
//                                .padding(bottom = 40.dp)
//                        )
                        val focusManager = LocalFocusManager.current
                        Column(Modifier.padding(bottom = 8.dp)) {
                            Text(
                                modifier = Modifier
                                    .testTag("txt_search_label")
                                    .padding(bottom = 10.dp),
                                style = MaterialTheme.appTypography.titleMedium,
                                text = stringResource(id = R.string.pre_auth_search_title),
                            )
                            SearchBar(
                                modifier = Modifier
                                    .testTag("tf_discovery_search")
                                    .fillMaxWidth()
                                    .height(48.dp),
                                label = stringResource(id = R.string.pre_auth_search_hint),
                                requestFocus = false,
                                searchValue = textFieldValue,
                                clearOnSubmit = true,
                                keyboardActions = {
                                    focusManager.clearFocus()
                                    onSearchSubmit(textFieldValue.text)
                                },
                                onValueChanged = { text ->
                                    textFieldValue = text
                                },
                                onClearValue = {
                                    textFieldValue = TextFieldValue("")
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                            LogistrationFilters(
                                onFiltersChanged = onFiltersChanged
                            )
                        }

//                        Text(
//                            modifier = Modifier
//                                .testTag("txt_explore_all_courses")
//                                .padding(bottom = 32.dp)
//                                .noRippleClickable {
//                                    onSearchClick("")
//                                },
//                            text = stringResource(id = R.string.pre_auth_explore_all_courses),
//                            color = MaterialTheme.appColors.primary,
//                            style = MaterialTheme.appTypography.labelLarge,
//                            textDecoration = TextDecoration.Underline
//                        )
                    }
                }

                when (state) {
                    is DiscoveryUIState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                            }
                        }
                    }

                    is DiscoveryUIState.Courses -> {
                        items(state.courses) { course ->
                            DiscoveryCourseItem(
                                apiHostUrl = apiHostUrl,
                                course = course,
                                windowSize = windowSize,
                                onClick = {
                                    onItemClick(course)
                                }
                            )
                            Divider()
                        }
                        item {
                            if (canLoadMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                                }
                            }
                        }
                    }
                }
            }

            if (scrollState.shouldLoadMore(firstVisibleIndex, 4)) {
                paginationCallback()
            }

            PullRefreshIndicator(
                refreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                if (!isInternetConnectionShown && !hasInternetConnection) {
                    OfflineModeDialog(
                        Modifier
                            .fillMaxWidth(),
                        onDismissCLick = {
                            isInternetConnectionShown = true
                        },
                        onReloadClick = {
                            isInternetConnectionShown = true
                            onReloadClick()
                        }
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_9_Light", device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_9_Night", device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogistrationPreview() {
    OpenEdXTheme {
        LogistrationScreen(
            onSearchSubmit = {},
            onFiltersChanged = { _ -> },
            onSearchClick = {},
            onSignInClick = {},
            onRegisterClick = {},
            isRegistrationEnabled = true,
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            state = DiscoveryUIState.Courses(
                listOf(
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                )
            ),
            uiMessage = null,
            apiHostUrl = "",
            paginationCallback = {},
            onSwipeRefresh = {},
            onItemClick = {},
            onReloadClick = {},
            canLoadMore = false,
            refreshing = false,
            hasInternetConnection = true,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "NEXUS_9_Light", device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_9_Night", device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogistrationRegistrationDisabledPreview() {
    OpenEdXTheme {
        LogistrationScreen(
            onSearchSubmit = {},
            onFiltersChanged = { _ -> },
            onSearchClick = {},
            onSignInClick = {},
            onRegisterClick = {},
            isRegistrationEnabled = false,
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            state = DiscoveryUIState.Courses(
                listOf(
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                )
            ),
            uiMessage = null,
            apiHostUrl = "",
            paginationCallback = {},
            onSwipeRefresh = {},
            onItemClick = {},
            onReloadClick = {},
            canLoadMore = false,
            refreshing = false,
            hasInternetConnection = true,
        )
    }
}

private val mockCourse = Course(
    id = "id",
    blocksUrl = "blocksUrl",
    courseId = "courseId",
    effort = "effort",
    enrollmentStart = null,
    enrollmentEnd = null,
    hidden = false,
    invitationOnly = false,
    media = Media(),
    mobileAvailable = true,
    name = "Test course",
    number = "number",
    org = "EdX",
    pacing = "pacing",
    shortDescription = "shortDescription",
    start = "start",
    end = "end",
    startDisplay = "startDisplay",
    startType = "startType",
    overview = "",
    isEnrolled = false
)
