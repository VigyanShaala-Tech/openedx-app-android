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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.PaginationDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.presentation.CarouselItem
import org.openedx.dashboard.presentation.DashboardCarousel
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as CoreR
import org.koin.android.ext.android.inject
import org.openedx.dashboard.presentation.DashboardRouter

class NewDashboardFragment : Fragment() {
    private val dashboardRouter: DashboardRouter by inject()
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
                NewDashboardScreen(
                    viewModel = viewModel,
                    onWishlistViewAllClick = {
                        dashboardRouter.navigateToWishlist(requireActivity().supportFragmentManager)
                    }
                )
            }
        }
    }
}

private data class StatCardData(val icon: androidx.compose.ui.graphics.vector.ImageVector, val value: String, val label: String)
private data class CourseCardData(val title: String, val tag: String, val imageUrl: String, val progress: Int)
private data class WishlistItemData(val title: String, val meta: String, val rating: String, val reviews: String, val instructor: String, val imageUrl: String)
private data class AchievementData(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class RecommendationData(val title: String, val category: String, val rating: String, val description: String, val imageUrl: String)

@Composable
private fun NewDashboardScreen(viewModel: NewDashboardViewModel, onWishlistViewAllClick: () -> Unit) {
    val uiState by viewModel.state.collectAsState(NewDashboardState())
    NewDashboardScreenContent(uiState, viewModel.userName, onWishlistViewAllClick)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewDashboardScreenContent(uiState: NewDashboardState, userName: String, onWishlistViewAllClick: () -> Unit) {
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
            "faBookOpen" -> Icons.Filled.Book
            "faCheckCircle" -> Icons.Filled.CheckCircle
            "faChartLine" -> Icons.Filled.Alarm
            "faAward" -> Icons.Filled.EmojiEvents
            else -> Icons.Filled.Book
        }
        StatCardData(icon, it.number.toString(), it.label)
    }

    val continueCourses = uiState.continueLearning.map { course ->
        CourseCardData(course.title, course.category ?: "", sanitizeUrl(course.course_image), course.progress)
    }
    val completedCourses = uiState.completed?.results?.map { course ->
        CourseCardData(course.title, course.category ?: "", sanitizeUrl(course.course_image), course.progress)
    } ?: emptyList()
    val wishlistItems = uiState.wishlist?.results?.map { course ->
        WishlistItemData(
            course.title,
            (course.level ?: "").trim(),
            "0",
            "0 reviews",
            "",
            sanitizeUrl(course.course_image)
        )
    } ?: emptyList()
    val achievements = uiState.achievements.map { a ->
        AchievementData(a.title, Icons.Filled.EmojiEvents)
    }
    val recommendations = uiState.recommended.map { rec ->
        RecommendationData(
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
//                                    .height(110.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
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
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.appColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = item.value,
                                        style = MaterialTheme.appTypography.titleMedium,
                                        color = MaterialTheme.appColors.textDark
                                    )
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.appTypography.labelSmall,
                                        color = MaterialTheme.appColors.textPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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
                SectionHeader(title = "My Courses")
                CoursesTabs(
                    continueCourses = continueCourses,
                    wishlistItems = wishlistItems,
                    completedCourses = completedCourses,
                    onWishlistViewAllClick = onWishlistViewAllClick
                )
            }

            if (achievements.isNotEmpty()) {
                item {
                    SectionHeader(title = "Achievements", showViewAll = true)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(achievements) { a ->
                            Card(
                                backgroundColor = MaterialTheme.appColors.surface,
                                elevation = 0.dp,
                                shape = MaterialTheme.appShapes.cardShape
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                        Icon(
                                            imageVector = a.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.appColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = a.title,
                                        style = MaterialTheme.appTypography.bodySmall,
                                        color = MaterialTheme.appColors.textDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recommendations.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recommended for You", showViewAll = true)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recommendations.forEach { r ->
                            RecommendationItem(r)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, showViewAll: Boolean = false) {
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
                modifier = Modifier.clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Continue Learning", "Wishlist", "Completed")

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
                        if (selected) MaterialTheme.appColors.primary else MaterialTheme.appColors.tabUnselectedBtnBackground
                    )
                    .clickable { selectedTab = index }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.appTypography.bodySmall,
                    color = if (selected) MaterialTheme.appColors.primaryButtonText else MaterialTheme.appColors.tabUnselectedBtnContent
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
                                    CourseCard(c)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = {})
            } else {
                EmptyTabContent("No courses in progress")
            }
        }
        1 -> {
            if (wishlistItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    wishlistItems.forEach { WishlistItem(it) }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = onWishlistViewAllClick)
            } else {
                EmptyTabContent("Your wishlist is empty")
            }
        }
        else -> {
            if (completedCourses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    completedCourses.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowItems.forEach { c ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CourseCard(c)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                ViewAllLink(onClick = {})
            } else {
                EmptyTabContent("No completed courses yet")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "View All",
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.primary
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.appColors.primary
        )
    }
}

@Composable
private fun CourseCard(c: CourseCardData) {
    Card(
        backgroundColor = MaterialTheme.appColors.surface,
        elevation = 0.dp,
        shape = MaterialTheme.appShapes.cardShape
    ) {
        Column {
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
                        .background(MaterialTheme.appColors.primary, MaterialTheme.appShapes.textFieldShape)
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
                LinearProgressIndicator(
                    progress = c.progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.appColors.progressBarColor,
                    backgroundColor = MaterialTheme.appColors.progressBarBackgroundColor
                )
            }
        }
    }
}

@Composable
private fun WishlistItem(w: WishlistItemData) {
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
                        .data(w.imageUrl)
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
                    text = w.meta,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textPrimary
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
                        text = w.rating,
                        style = MaterialTheme.appTypography.bodySmall,
                        color = MaterialTheme.appColors.textDark
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "(${w.reviews})",
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
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.appColors.tabUnselectedBtnContent,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
private fun RecommendationItem(r: RecommendationData) {
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
                        .data(r.imageUrl)
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.appColors.primary, MaterialTheme.appShapes.textFieldShape)
                            .clip(MaterialTheme.appShapes.textFieldShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = r.category,
                            style = MaterialTheme.appTypography.labelSmall,
                            color = MaterialTheme.appColors.primaryButtonText
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.primary
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

private const val SAMPLE_IMAGE_1 = "https://images.unsplash.com/photo-1509395176047-4a66953fd231?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_2 = "https://images.unsplash.com/photo-1517142089942-ba376ce32a2e?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_3 = "https://images.unsplash.com/photo-1527694224012-bea5e2b3e979?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_4 = "https://images.unsplash.com/photo-1518779578993-ec3579fee39f?q=80&w=1080&auto=format&fit=crop"

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
                    CourseItemDto("1", "Introduction to Computer Science", SAMPLE_IMAGE_1, 45, "Computer Science", "Beginner"),
                    CourseItemDto("2", "Data Science: Machine Learning", SAMPLE_IMAGE_2, 70, "Data Science", "Intermediate")
                ),
                achievements = listOf(
                    AchievementDto(1, "Fast Learner", null),
                    AchievementDto(2, "Top Performer", null)
                ),
                recommended = listOf(
                    RecommendationDto("r1", "Advanced Python", "Learn advanced python concepts", "Programming", "10 weeks", "Advanced", SAMPLE_IMAGE_4, 4, 120, "John Doe")
                ),
                wishlist = PaginatedDto(
                    results = listOf(
                        CourseItemDto("3", "Mobile App Development", SAMPLE_IMAGE_3, 0, "Mobile", "Beginner")
                    ),
                    pagination = PaginationDto(null, null, 1, 1)
                ),
                completed = PaginatedDto(
                    results = listOf(
                        CourseItemDto("1", "Introduction to Computer Science", SAMPLE_IMAGE_1, 100, "Computer Science", "Beginner")
                    ),
                    pagination = PaginationDto(null, null, 1, 1)
                )
            ),
            userName = "Priya",
            onWishlistViewAllClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewDashboardScreenLoadingPreview() {
    OpenEdXTheme {
        NewDashboardScreenContent(
            uiState = NewDashboardState(loading = true),
            userName = "Priya",
            onWishlistViewAllClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NewDashboardScreenEmptyPreview() {
    OpenEdXTheme {
        NewDashboardScreenContent(
            uiState = NewDashboardState(loading = false),
            userName = "Priya",
            onWishlistViewAllClick = {}
        )
    }
}
