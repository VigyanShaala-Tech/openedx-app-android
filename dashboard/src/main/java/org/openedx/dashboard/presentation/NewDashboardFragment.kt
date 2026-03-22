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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
 
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.Fragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as CoreR

class NewDashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                NewDashboardScreen()
            }
        }
    }
}

private data class StatCardData(val icon: androidx.compose.ui.graphics.vector.ImageVector, val value: String, val label: String)
private data class CourseCardData(val title: String, val tag: String, val imageUrl: String, val progress: Int)
private data class WishlistItemData(val title: String, val meta: String, val rating: String, val reviews: String, val instructor: String, val imageUrl: String)
private data class AchievementData(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class RecommendationData(val title: String, val category: String, val rating: String, val description: String, val imageUrl: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewDashboardScreen() {
    val windowSize = rememberWindowSize()
    val contentPadding by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                compact = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            )
        )
    }

    val statCards = listOf(
        StatCardData(Icons.Filled.Book, "10", "Enrolled Courses"),
        StatCardData(Icons.Filled.CheckCircle, "3", "Completed Courses"),
        StatCardData(Icons.Filled.Alarm, "7", "In Progress Courses"),
        StatCardData(Icons.Filled.EmojiEvents, "3", "Badges Earned"),
    )

    val continueCourses = listOf(
        CourseCardData("STEM Research & Project Lab", "Research", SAMPLE_IMAGE_1, 45),
        CourseCardData("Data Science for Beginners", "Data", SAMPLE_IMAGE_2, 30),
        CourseCardData("Web Design Fundamentals", "Design", SAMPLE_IMAGE_3, 55),
        CourseCardData("Machine Learning Foundations", "AI/ML", SAMPLE_IMAGE_4, 30),
    )
    val completedCourses = listOf(
        CourseCardData("Robotics Foundation", "Robotics", SAMPLE_IMAGE_5, 100),
        CourseCardData("Mobile App Dev", "Dev", SAMPLE_IMAGE_6, 100),
        CourseCardData("Python Learning Journey", "Python", SAMPLE_IMAGE_7, 100),
        CourseCardData("Career Skills & Portfolio", "Career", SAMPLE_IMAGE_8, 100),
    )
    val wishlistItems = listOf(
        WishlistItemData("Quantum Computing Basics", "18 hours · Advanced", "4.5", "450 reviews", "Dr. Raj Patel", SAMPLE_IMAGE_9),
        WishlistItemData("Electromagnetic Wonders", "12 hours · Intermediate", "4.3", "320 reviews", "Prof. Meera Gupta", SAMPLE_IMAGE_10),
        WishlistItemData("Space Studies & Astronomy", "24 hours · Beginner", "4.8", "680 reviews", "Dr. Ananya Sharma", SAMPLE_IMAGE_11),
        WishlistItemData("Advanced Renewable Energy", "20 hours · Advanced", "4.6", "510 reviews", "Prof. Vikram Singh", SAMPLE_IMAGE_1),
    )
    val achievements = listOf(
        AchievementData("First Course", Icons.Filled.Book),
        AchievementData("7 Day Streak", Icons.Filled.CheckCircle),
        AchievementData("Quiz Master", Icons.Filled.EmojiEvents),
        AchievementData("Top Learner", Icons.Filled.Star),
    )
    val recommendations = listOf(
        RecommendationData("Advanced Renewable Energy", "Energy", "4.7", "Deep dive into solar, wind, and sustainable energy...", SAMPLE_IMAGE_1),
        RecommendationData("DNA Healthcare", "Health", "4.5", "Learn how DNA analysis is transforming modern...", SAMPLE_IMAGE_12),
        RecommendationData("Environmental Data Patterns", "Environment", "4.7", "Analyze environmental data for climate change...", SAMPLE_IMAGE_13),
    )

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back, Priya",
                            style = MaterialTheme.appTypography.titleLarge,
                            color = MaterialTheme.appColors.textDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.appColors.surface, MaterialTheme.appShapes.cardShape)
                            .clip(MaterialTheme.appShapes.cardShape)
                            .clickable { }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(statCards.size) { index ->
                        val item = statCards[index]
                        Card(
                            backgroundColor = MaterialTheme.appColors.surface,
                            elevation = 0.dp,
                            shape = MaterialTheme.appShapes.cardShape
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = item.value,
                                    style = MaterialTheme.appTypography.titleLarge,
                                    color = MaterialTheme.appColors.textDark
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.appTypography.bodySmall,
                                    color = MaterialTheme.appColors.textPrimary
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
                    completedCourses = completedCourses
                )
            }

            item {
                SectionHeader(title = "Achievements", showViewAll = true)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(achievements.size) { index ->
                        val a = achievements[index]
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
                                Icon(
                                    imageVector = a.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.primary
                                )
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
            ViewAllLink()
        }
        1 -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                wishlistItems.forEach { WishlistItem(it) }
            }
            Spacer(Modifier.height(12.dp))
            ViewAllLink()
        }
        else -> {
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
            ViewAllLink()
        }
    }
}

@Composable
private fun ViewAllLink() {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
private const val SAMPLE_IMAGE_5 = "https://images.unsplash.com/photo-1508921340878-b1de6f7d16c8?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_6 = "https://images.unsplash.com/photo-1512496015851-a90fb38ba4f4?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_7 = "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_8 = "https://images.unsplash.com/photo-1497294815431-197d1f1c9f07?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_9 = "https://images.unsplash.com/photo-1485827404703-89b55f3a8ba0?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_10 = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_11 = "https://images.unsplash.com/photo-1526401485004-2ca616c53df9?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_12 = "https://images.unsplash.com/photo-1581093588401-9cf9f3ebd57a?q=80&w=1080&auto=format&fit=crop"
private const val SAMPLE_IMAGE_13 = "https://images.unsplash.com/photo-1469474968028-56623f02e42e?q=80&w=1080&auto=format&fit=crop"
