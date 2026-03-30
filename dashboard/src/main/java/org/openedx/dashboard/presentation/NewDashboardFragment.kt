package org.openedx.dashboard.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.auth.presentation.AuthRouter
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.dashboard.data.model.AchievementData
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseCardData
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.PaginationDto
import org.openedx.dashboard.data.model.RecommendationData
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.StatCardData
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.data.model.WishlistItemData
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as CoreR

class NewDashboardFragment : Fragment() {
    private val dashboardRouter: DashboardRouter by inject()
    private val authRouter: AuthRouter by inject()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()
                val viewModel: NewDashboardViewModel = koinViewModel { parametersOf(windowSize) }
                val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
                LaunchedEffect(lifecycleState) {
                    if (lifecycleState == Lifecycle.State.RESUMED) {
                        viewModel.refresh()
                    }
                }
                NewDashboardScreen(
                    viewModel = viewModel,
                    onWishlistViewAllClick = {
                        dashboardRouter.navigateToWishlist(requireActivity().supportFragmentManager)
                    },
                    onAchievementsViewAllClick = {
                        dashboardRouter.navigateToAchievements(requireActivity().supportFragmentManager)
                    },
                    onRecommendedViewAllClick = {
                        authRouter.navigateToLogistration(
                            requireActivity().supportFragmentManager,
                            null,
                            "RECOMMENDED"
                        )
                    },
                    onRecommendationClick = { courseId ->
                        dashboardRouter.navigateToCourseDetail(
                            requireActivity().supportFragmentManager,
                            courseId
                        )
                    },
                    onCourseClick = { id, title ->
                        dashboardRouter.navigateToCourseOutline(
                            fm = requireActivity().supportFragmentManager,
                            courseId = id,
                            courseTitle = title
                        )
                    },
                    onContinueViewAllClick = {
                        dashboardRouter.navigateToAllEnrolledCourses(
                            requireActivity().supportFragmentManager,
                            "IN_PROGRESS"
                        )
                    },
                    onCompletedViewAllClick = {
                        dashboardRouter.navigateToAllEnrolledCourses(
                            requireActivity().supportFragmentManager,
                            "COMPLETED"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun NewDashboardScreen(
    viewModel: NewDashboardViewModel,
    onWishlistViewAllClick: () -> Unit,
    onAchievementsViewAllClick: () -> Unit,
    onRecommendedViewAllClick: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onCourseClick: (String, String) -> Unit,
    onContinueViewAllClick: () -> Unit,
    onCompletedViewAllClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsState(NewDashboardState())
    NewDashboardScreenContent(
        uiState,
        viewModel.userFullName ?: viewModel.userName,
        onWishlistViewAllClick,
        onAchievementsViewAllClick,
        onRecommendedViewAllClick,
        onRecommendationClick,
        onCourseClick,
        onContinueViewAllClick,
        onCompletedViewAllClick,
        onRemoveWishlist = { id -> viewModel.removeFromWishlist(id) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewDashboardScreenContent(
    uiState: NewDashboardState,
    displayName: String,
    onWishlistViewAllClick: () -> Unit,
    onAchievementsViewAllClick: () -> Unit,
    onRecommendedViewAllClick: () -> Unit,
    onRecommendationClick: (String) -> Unit,
    onCourseClick: (String, String) -> Unit,
    onContinueViewAllClick: () -> Unit,
    onCompletedViewAllClick: () -> Unit,
    onRemoveWishlist: (String) -> Unit
) {
    val windowSize = rememberWindowSize()
    val contentPadding by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                compact = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            )
        )
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.appColors.primary)
        }
        return
    }

    val statCards = uiState.summary.map {
        val icon = when (it.icon) {
            "faBookOpen" -> Icons.Filled.ImportContacts
            "faCheckCircle" -> Icons.Filled.CheckCircle
            "faChartLine" -> Icons.Filled.Alarm
            "faAward" -> Icons.Filled.EmojiEvents
            else -> Icons.Filled.Book
        }
        StatCardData(icon, it.number.toString(), it.label)
    }

    val continueCourses = uiState.continueLearning.map { course ->
        CourseCardData(
            course.id,
            course.title,
            course.category ?: "",
            sanitizeUrl(course.course_image),
            course.progress
        )
    }
    val completedCourses = uiState.completed?.results?.map { course ->
        CourseCardData(
            course.id,
            course.title,
            course.category ?: "",
            sanitizeUrl(course.course_image),
            course.progress
        )
    } ?: emptyList()
    val wishlistItems = uiState.wishlist?.results?.map { it.copy(image = sanitizeUrl(it.image)) } ?: emptyList()
    
    val achievements = uiState.achievements

    val recommendations = uiState.recommended.map { rec ->
        RecommendationData(
            rec.id,
            rec.title,
            rec.category ?: "",
            (rec.rating ?: 0).toString(),
            rec.description ?: "",
            sanitizeUrl(rec.image)
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            item {
//                val carouselItems = remember {
//                    listOf(
//                        CarouselItem(
//                            imageUrl = SAMPLE_IMAGE_1,
//                            title = "Creating Job Opportunities in Science and Technology",
//                            subtitle = "Discover the latest job opportunities in Science and Technology and apply for the ones that match your skills and interests."
//                        ),
//                        CarouselItem(
//                            imageUrl = SAMPLE_IMAGE_2,
//                            title = "Learn from Industry Experts",
//                            subtitle = "Access world-class education from renowned instructors and gain practical knowledge that sets you apart."
//                        ),
//                        CarouselItem(
//                            imageUrl = SAMPLE_IMAGE_3,
//                            title = "Grow your career in STEM",
//                            subtitle = "Be part of an inspiring global community and make your next leap into higher education or a job."
//                        )
//                    )
//                }
//                DashboardCarousel(items = carouselItems)
//            }
//            item {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "Welcome back, $userName",
//                            style = MaterialTheme.appTypography.titleLarge,
//                            color = MaterialTheme.appColors.textDark
//                        )
//                    }
//                    Card(
//                        modifier = Modifier.size(36.dp),
//                        shape = CircleShape,
//                        elevation = 0.dp,
//                        backgroundColor = MaterialTheme.appColors.surface
//                    ) {
//                        Box(contentAlignment = Alignment.Center) {
//                            Icon(
//                                imageVector = Icons.Filled.EmojiEvents,
//                                contentDescription = null,
//                                tint = MaterialTheme.appColors.primary
//                            )
//                        }
//                    }
//                }
//            }

            if (statCards.isNotEmpty()) {
                item {
                    val cards = statCards.take(4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        cards.forEach { item ->
                            Card(
                                backgroundColor = MaterialTheme.appColors.background,
                                elevation = 4.dp,
                                shape = MaterialTheme.appShapes.cardShape,
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
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
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.appColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = item.value,
                                        style = MaterialTheme.appTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.appColors.textDark
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.appTypography.labelSmall.copy(lineHeight = 14.sp),
                                        color = MaterialTheme.appColors.textPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                        if (cards.size < 4) {
                            repeat(4 - cards.size) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_my_courses))
                Spacer(Modifier.height(8.dp))
                CoursesTabs(
                    continueCourses = continueCourses,
                    wishlistItems = wishlistItems,
                    completedCourses = completedCourses,
                    onWishlistViewAllClick = onWishlistViewAllClick,
                    onCourseClick = onCourseClick,
                    onContinueViewAllClick = onContinueViewAllClick,
                    onCompletedViewAllClick = onCompletedViewAllClick,
                    onRemoveWishlist = onRemoveWishlist
                )
            }

            if (achievements.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Achievements",
                        showViewAll = true,
                        onViewAllClick = onAchievementsViewAllClick
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(achievements) { a ->
                            Card(
                                backgroundColor = MaterialTheme.appColors.surface,
                                elevation = 0.dp,
                                shape = MaterialTheme.appShapes.cardShape,
                                modifier = Modifier.width(windowSize.windowSizeValue(expanded = 160.dp, compact = 110.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(a.img)
                                            .error(CoreR.drawable.core_ic_logo)
                                            .placeholder(CoreR.drawable.core_ic_logo)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp).clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = a.title,
                                        style = MaterialTheme.appTypography.labelSmall,
                                        color = MaterialTheme.appColors.textDark,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recommendations.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_recommended_for_you),
                        showViewAll = true,
                        onViewAllClick = onRecommendedViewAllClick
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recommendations.forEach { r ->
                            RecommendationItem(r) { onRecommendationClick(r.id) }
                        }
                    }
                }
            }
            }
        }
        if (uiState.refreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.appColors.primary
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showViewAll: Boolean = false,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark
        )
        if (showViewAll) {
            Row(
                modifier = Modifier.clickable { onViewAllClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_view_all),
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.primary
                )
            }
        }
    }
}

@Composable
private fun CoursesTabs(
    continueCourses: List<CourseCardData>,
    wishlistItems: List<WishlistItemData>,
    completedCourses: List<CourseCardData>,
    onWishlistViewAllClick: () -> Unit,
    onCourseClick: (String, String) -> Unit,
    onContinueViewAllClick: () -> Unit,
    onCompletedViewAllClick: () -> Unit,
    onRemoveWishlist: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_continue_learning),
        androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_wishlist),
        androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_completed)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.appColors.surface, MaterialTheme.appShapes.textFieldShape)
            .clip(MaterialTheme.appShapes.textFieldShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.appShapes.textFieldShape)
                    .background(
                        if (selected) MaterialTheme.appColors.primary else Color.Transparent
                    )
                    .clickable { selectedTab = index }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = if (selected) MaterialTheme.appColors.primaryButtonText else Color.Black,
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    when (selectedTab) {
        0 -> {
            if (continueCourses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    continueCourses.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { c ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CourseCard(c) { onCourseClick(c.id, c.title) }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = onContinueViewAllClick)
            } else {
                EmptyTabContent(androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_no_courses_in_progress))
            }
        }

        1 -> {
            if (wishlistItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    wishlistItems.forEach { WishlistItem(it, onRemove = onRemoveWishlist) }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = onWishlistViewAllClick)
            } else {
                EmptyTabContent(androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_wishlist_empty))
            }
        }

        else -> {
            if (completedCourses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    completedCourses.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { c ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CourseCard(c) { onCourseClick(c.id, c.title) }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = onCompletedViewAllClick)
            } else {
                EmptyTabContent(androidx.compose.ui.res.stringResource(org.openedx.dashboard.R.string.dashboard_no_completed_courses))
            }
        }
    }
}

@Composable
private fun EmptyTabContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.textPrimary
        )
    }
}

@Composable
private fun ViewAllLink(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.appColors.primary.copy(alpha = 0.12f)) // light grey background
            .clickable { onClick() }
            .padding(vertical = 12.dp) // height of button
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "View All",
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.primary
            )

            Spacer(modifier = Modifier.width(4.dp)) // small gap

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier.size(18.dp) // slightly smaller icon
            )
        }
    }
}

@Composable
private fun CourseCard(c: CourseCardData, onClick: () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Column(
            modifier = Modifier.clickable { onClick() }
        ) {
            Box {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(MaterialTheme.appShapes.cardShape),
                    contentScale = ContentScale.Crop,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(c.imageUrl)
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                )
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(
                            MaterialTheme.appColors.primary,
                            MaterialTheme.appShapes.textFieldShape
                        )
                        .clip(MaterialTheme.appShapes.textFieldShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = c.tag,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.primaryButtonText
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = c.title,
                    style = MaterialTheme.appTypography.titleSmall,
                    color = MaterialTheme.appColors.textDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Percentage text
                    Text(
                        text = "${c.progress}%",
                        style = MaterialTheme.appTypography.bodySmall,
                        color = Color(0xFF7A7A7A), // subtle grey
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LinearProgressIndicator(
                        progress = c.progress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.appColors.primary,
                        backgroundColor = MaterialTheme.appColors.progressBarBackgroundColor
                    )
                }
            }
        }
    }
}

@Composable
private fun WishlistItem(w: WishlistItemData, onRemove: (String) -> Unit) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Box {
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
                        .data(w.image)
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = w.title,
                        style = MaterialTheme.appTypography.titleSmall,
                        color = MaterialTheme.appColors.textDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${w.duration}-${w.category}",
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.rateStars
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = w.rating?.toString() ?: "",
                            style = MaterialTheme.appTypography.bodySmall,
                            color = MaterialTheme.appColors.textDark
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "(${w.reviews ?: 0})",
                            style = MaterialTheme.appTypography.labelSmall,
                            color = MaterialTheme.appColors.textPrimary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = w.instructor,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = MaterialTheme.appColors.textPrimary,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onRemove(w.id) }
            )
        }
    }
}

@Composable
private fun RecommendationItem(r: RecommendationData, onClick: () -> Unit) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.appShapes.cardShape),
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(r.imageUrl)
                    .error(CoreR.drawable.core_no_image_course)
                    .placeholder(CoreR.drawable.core_no_image_course)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
            )
//            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier
                .weight(1f)
                .padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.appColors.primary.copy(alpha = 0.12f),
                                MaterialTheme.appShapes.textFieldShape
                            )
                            .clip(MaterialTheme.appShapes.textFieldShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = r.category,
                            style = MaterialTheme.appTypography.labelSmall,
                            color = MaterialTheme.appColors.primary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.rateStars
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = r.rating,
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textDark
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = r.title,
                    style = MaterialTheme.appTypography.titleSmall,
                    color = MaterialTheme.appColors.textDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = r.description,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private const val SAMPLE_IMAGE_1 =
    "https://images.unsplash.com/photo-1509395176047-4a66953fd231?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_2 =
    "https://images.unsplash.com/photo-1517142089942-ba376ce32a2e?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_3 =
    "https://images.unsplash.com/photo-1527694224012-bea5e2b3e979?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_4 =
    "https://images.unsplash.com/photo-1518779578993-ec3579fee39f?q=80&w=1080&auto=format&fit=crop"

private fun sanitizeUrl(url: String?): String {
    return url?.replace("`", "")?.trim() ?: ""
}

@Preview(showBackground = true)
@Composable
private fun NewDashboardScreenPreview() {
    OpenEdXTheme {
        NewDashboardScreenContent(
            uiState = NewDashboardState(
                loading = false,
                summary = listOf(
                    SummaryCardDto(1, "faBookOpen", 3, "Courses in Progress"),
                    SummaryCardDto(2, "faCheckCircle", 5, "Completed Courses"),
                    SummaryCardDto(3, "faChartLine", 12, "Learning Days Streak"),
                    SummaryCardDto(4, "faAward", 2, "Achievements")
                ),
                continueLearning = listOf(
                    CourseItemDto(
                        "1",
                        "Introduction to Computer Science",
                        SAMPLE_IMAGE_1,
                        45,
                        "Computer Science",
                        "Beginner"
                    ),
                    CourseItemDto(
                        "2",
                        "Data Science: Machine Learning",
                        SAMPLE_IMAGE_2,
                        70,
                        "Data Science",
                        "Intermediate"
                    )
                ),
                achievements = listOf(
                    AchievementDto(1, "Fast Learner", null),
                    AchievementDto(2, "Top Performer", null)
                ),
                recommended = listOf(
                    RecommendationDto(
                        "r1",
                        "Advanced Python",
                        "Learn advanced python concepts",
                        "Programming",
                        "10 weeks",
                        "Advanced",
                        SAMPLE_IMAGE_4,
                        4,
                        120,
                        "John Doe"
                    )
                ),
                wishlist = PaginatedDto(
                    results = listOf(
                        org.openedx.dashboard.data.model.WishlistItemData(
                            id = "3",
                            title = "Mobile App Development",
                            description = "Learn to build Android apps",
                            image = SAMPLE_IMAGE_3,
                            duration = "40 Hours",
                            progress = "0",
                            category = "Mobile",
                            level = "Beginner",
                            rating = 4.0f,
                            reviews = 125,
                            instructor = "Jane Doe"
                        )
                    ),
                    pagination = PaginationDto(null, null, 1, 1)
                ),
                completed = PaginatedDto(
                    results = listOf(
                        CourseItemDto(
                            "1",
                            "Introduction to Computer Science",
                            SAMPLE_IMAGE_1,
                            100,
                            "Computer Science",
                            "Beginner"
                        )
                    ),
                    pagination = PaginationDto(null, null, 1, 1)
                )
            ),
            displayName = "Priya",
            onWishlistViewAllClick = {},
            onAchievementsViewAllClick = {},
            onRecommendedViewAllClick = {},
            onRecommendationClick = {},
            onCourseClick = { _, _ -> },
            onContinueViewAllClick = {},
            onCompletedViewAllClick = {},
            onRemoveWishlist = {}
        )
    }
}

//@Preview(showBackground = true)
@Composable
private fun NewDashboardScreenLoadingPreview() {
    OpenEdXTheme {
        NewDashboardScreenContent(
            uiState = NewDashboardState(loading = true),
            displayName = "Priya",
            onWishlistViewAllClick = {},
            onAchievementsViewAllClick = {},
            onRecommendedViewAllClick = {},
            onRecommendationClick = {},
            onCourseClick = { _, _ -> },
            onContinueViewAllClick = {},
            onCompletedViewAllClick = {},
            onRemoveWishlist = {}
        )
    }
}

//@Preview(showBackground = true)
@Composable
private fun NewDashboardScreenEmptyPreview() {
    OpenEdXTheme {
        NewDashboardScreenContent(
            uiState = NewDashboardState(loading = false),
            displayName = "Priya",
            onWishlistViewAllClick = {},
            onAchievementsViewAllClick = {},
            onRecommendedViewAllClick = {},
            onRecommendationClick = {},
            onCourseClick = { _, _ -> },
            onContinueViewAllClick = {},
            onCompletedViewAllClick = {},
            onRemoveWishlist = {}
        )
    }
}
