package org.openedx.auth.presentation.logistration

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.auth.R
import org.openedx.core.ApiConstants
import org.openedx.core.domain.model.Media
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.SearchBar
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.shouldLoadMore
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.compose.LogistrationLogoView
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryUIState
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.foundation.utils.UrlUtils
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
                    origin = arguments?.getString(ARG_ORIGIN),
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
                            viewModel.navigateToSignIn(requireActivity().supportFragmentManager)
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
                            viewModel.navigateToSignUp(requireActivity().supportFragmentManager)
                        }
                    },
                    onSearchClick = { querySearch ->
                        viewModel.navigateToDiscovery(
                            requireActivity().supportFragmentManager,
                            querySearch
                        )
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
                        viewModel.navigateToCourseDetail(
                            requireActivity().supportFragmentManager,
                            course.id
                        )
                    },
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onBackClick = {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_ORIGIN = "origin"
        fun newInstance(courseId: String?): LogistrationFragment {
            val fragment = LogistrationFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId
            )
            return fragment
        }

        fun newInstance(courseId: String?, origin: String?): LogistrationFragment {
            val fragment = LogistrationFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId,
                ARG_ORIGIN to origin
            )
            return fragment
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
private fun LogistrationScreen(
    origin: String? = null,
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
    onBackClick: () -> Unit,
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
            if (!origin.equals("RECOMMENDED", true)) {
                Surface(
                    elevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OpenEdXOutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            text = stringResource(id = CoreR.string.core_sign_in),
                            onClick = onSignInClick,
                            borderColor = Color.Transparent, 
                            textColor = Color(0xFF37474F)
                        )
                        if (isRegistrationEnabled) {
                            Spacer(Modifier.width(16.dp))
                            OpenEdXButton(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp),
                                text = stringResource(id = CoreR.string.core_register),
                                onClick = onRegisterClick,
                                backgroundColor = Color(0xFF8BC34A) 
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        val contentWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(max = 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val contentPaddings by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = PaddingValues(top = 24.dp, bottom = 24.dp),
                    compact = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsInset()
                .displayCutoutForLandscape()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.TopCenter)
                    .then(contentWidth),
                contentPadding = contentPaddings,
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!origin.equals("RECOMMENDED", true)) {
                            LogistrationLogoView()
                            Spacer(Modifier.height(8.dp))
                            val carouselItems = listOf(
                                LogistrationCarouselItem(
                                    imageResId = R.drawable.onboarding_1,
                                    title = "Grow your career",
                                    titleColored = "START HERE",
                                    subtitle = "If you are pursuing a Science and Tech. degree, connect with experts learn from the best and do Hands-On research projects and take your next leap with as part of a global community."
                                ),
                                LogistrationCarouselItem(
                                    imageResId = R.drawable.onboarding_2,
                                    title = "Learn from Global",
                                    titleColored = "Instructors and Mentors",
                                    subtitle = "Our mentors across bring global industrial and academic experience across STEM disciplines that will set you apart"
                                ),
                                LogistrationCarouselItem(
                                    imageResId = R.drawable.onboarding_3,
                                    title = "Creating Job Opportunities in",
                                    titleColored = "Science and Technology",
                                    subtitle = "Discover the latest job opportunities in Science and Technology and apply for the ones that match your skills and interests."
                                )
                            )
                            LogistrationCarousel(items = carouselItems)
                        } else {
                            Toolbar(
                                label = "Explore Our Courses",
                                canShowBackBtn = false,
                                onBackClick = onBackClick
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Discover a comprehensive collection of STEM courses designed to help you build skills and advance your career.",
                                style = MaterialTheme.appTypography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = MaterialTheme.appColors.textDark
                            )
                        }

                        val focusManager = LocalFocusManager.current
                        var isSearchFocused by remember { mutableStateOf(false) }
                        Column {
                            Spacer(Modifier.height(24.dp))
                            SearchBar(
                                modifier = Modifier
                                    .testTag("tf_discovery_search")
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .onFocusChanged { isSearchFocused = it.isFocused }
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSearchFocused) 2.dp else 1.dp,
                                        color = if (isSearchFocused) MaterialTheme.appColors.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(Color(0xFFF1F4F6)),
                                label = "Search courses...",
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
                            Spacer(Modifier.height(16.dp))
                            LogistrationFilters(
                                onFiltersChanged = onFiltersChanged
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        if (state is DiscoveryUIState.Courses) {
                            Text(
                                text = "Showing ${state.courses.size} of ${state.courses.size} courses",
                                style = MaterialTheme.appTypography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF8BC34A),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
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
                            LogistrationCourseItem(
                                apiHostUrl = apiHostUrl,
                                course = course,
                                onClick = {
                                    onItemClick(course)
                                }
                            )
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

@Composable
private fun LogistrationCourseItem(
    apiHostUrl: String,
    course: Course,
    onClick: (Course) -> Unit
) {
    Card(
        backgroundColor = Color.White,
        elevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(course) }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(course.media?.courseImage?.uri?.toImageLink(apiHostUrl) ?: "")
                    .error(CoreR.drawable.core_no_image_course)
                    .placeholder(CoreR.drawable.core_no_image_course)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name.orEmpty(),
                    style = MaterialTheme.appTypography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color(0xFF263238),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (course.level?.isNotEmpty() == true) {
                        CourseTag(text = course.level.orEmpty())
                    }
                    if (course.category?.isNotEmpty() == true) {
                        CourseTag(text = course.category.orEmpty())
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = course.instructorName.orEmpty(),
                    style = MaterialTheme.appTypography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF78909C) 
                )
            }
        }
    }
}

@Composable
fun CourseTag(text: String) {
    Box(
        modifier = Modifier
            .background(
                Color(0xFF8BC34A).copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.appTypography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = Color(0xFF8BC34A)
        )
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
            origin = null,
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
            onBackClick = {}
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
            origin = null,
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
            onBackClick = {}
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
    name = "Vigyanshaala Rural STEM Champion - Workshops",
    number = "number",
    org = "EdX",
    pacing = "pacing",
    shortDescription = "shortDescription",
    start = "start",
    end = "end",
    startDisplay = "startDisplay",
    startType = "startType",
    overview = "",
    isEnrolled = false,
    rating = "4.5",
    noOfReviews = "100",
    enrollments = "0",
    isWishlisted = false,
    level = "Beginner",
    category = "STEM",
    instructorName = "Dr. Priya Sharma",
)
