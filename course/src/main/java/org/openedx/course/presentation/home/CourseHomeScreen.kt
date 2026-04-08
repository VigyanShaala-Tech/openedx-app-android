package org.openedx.course.presentation.home

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.AndroidUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import org.openedx.core.Mock
import org.openedx.core.NoContentScreenType
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.CourseDatesBannerInfo
import org.openedx.core.ui.CircularProgress
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.NoContentScreen
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import org.openedx.course.presentation.container.CourseContentTab
import org.openedx.course.presentation.ui.CourseDatesBanner
import org.openedx.course.presentation.ui.CourseDatesBannerTablet
import org.openedx.course.presentation.ui.CourseMessage
import org.openedx.course.presentation.ui.ResumeCourseButton
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.foundation.extension.takeIfNotEmpty
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as coreR

@Composable
fun CourseHomeScreen(
    windowSize: WindowSize,
    viewModel: CourseHomeViewModel,
    fragmentManager: FragmentManager,
    homePagerState: PagerState,
    onResetDatesClick: () -> Unit,
    onNavigateToContent: (CourseContentTab) -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(null)
    val resumeBlockId by viewModel.resumeBlockId.collectAsState("")
    val context = LocalContext.current

    LaunchedEffect(resumeBlockId) {
        if (resumeBlockId.isNotEmpty()) {
            viewModel.openBlock(fragmentManager, resumeBlockId)
        }
    }

    CourseHomeUI(
        windowSize = windowSize,
        uiState = uiState,
        uiMessage = uiMessage,
        onSubSectionClick = { subSectionBlock ->
            // Log section/subsection click event
            viewModel.logSectionSubsectionClick(
                subSectionBlock.blockId,
                subSectionBlock.displayName
            )
            if (viewModel.isCourseDropdownNavigationEnabled) {
                viewModel.courseSubSectionUnit[subSectionBlock.id]?.let { unit ->
                    viewModel.courseRouter.navigateToCourseContainer(
                        fragmentManager,
                        courseId = viewModel.courseId,
                        unitId = unit.id,
                        mode = CourseViewMode.FULL
                    )
                }
            } else {
                viewModel.courseRouter.navigateToCourseSubsections(
                    fm = fragmentManager,
                    courseId = viewModel.courseId,
                    subSectionId = subSectionBlock.id,
                    mode = CourseViewMode.FULL
                )
            }
        },
        onResumeClick = { componentId ->
            viewModel.openBlock(
                fragmentManager,
                componentId
            )
        },
        onDownloadClick = { blocksIds ->
            viewModel.downloadBlocks(
                blocksIds = blocksIds,
                fragmentManager = fragmentManager,
            )
        },
        onResetDatesClick = {
            viewModel.resetCourseDatesBanner(
                onResetDates = {
                    onResetDatesClick()
                }
            )
        },
        onCertificateClick = {
            viewModel.viewCertificateTappedEvent()
            it.takeIfNotEmpty()
                ?.let { url -> AndroidUriHandler(context).openUri(url) }
        },
        onVideoClick = { videoBlock ->
            viewModel.courseRouter.navigateToCourseContainer(
                fragmentManager,
                courseId = viewModel.courseId,
                unitId = viewModel.getBlockParent(videoBlock.id)?.id ?: return@CourseHomeUI,
                mode = CourseViewMode.VIDEOS
            )
            viewModel.logVideoClick(videoBlock.id)
        },
        onAssignmentClick = { assignmentBlock ->
            viewModel.courseRouter.navigateToCourseContainer(
                fragmentManager,
                courseId = viewModel.courseId,
                unitId = viewModel.getBlockParent(assignmentBlock.id)?.id ?: return@CourseHomeUI,
                mode = CourseViewMode.FULL
            )
            viewModel.logAssignmentClick(assignmentBlock.id)
        },
        onNavigateToContent = onNavigateToContent,
        onNavigateToProgress = onNavigateToProgress,
        getBlockParent = viewModel::getBlockParent,
        onViewAllContentClick = viewModel::logViewAllContentClick,
        onViewAllVideosClick = viewModel::logViewAllVideosClick,
        onViewAllAssignmentsClick = viewModel::logViewAllAssignmentsClick,
        onViewProgressClick = viewModel::logViewProgressClick,
        onViewAllAnnouncementsClick = {
            viewModel.logViewAllAnnouncementsClick()
            onNavigateToContent(CourseContentTab.HANDOUTS)
        },
        onJoinClick = { session ->
            // Handle join click, e.g. open in browser or deep link
            AndroidUriHandler(context).openUri("https://zoom.us/j/${session.id}") // Placeholder
        },
        onViewAllLiveSessionsClick = {
            onNavigateToContent(CourseContentTab.LIVE_SESSIONS)
        }
    )
}

@Composable
private fun CourseHomeUI(
    windowSize: WindowSize,
    uiState: CourseHomeUIState,
    uiMessage: UIMessage?,
    onSubSectionClick: (Block) -> Unit,
    onResumeClick: (String) -> Unit,
    onDownloadClick: (blockIds: List<String>) -> Unit,
    onResetDatesClick: () -> Unit,
    onCertificateClick: (String) -> Unit,
    onVideoClick: (Block) -> Unit,
    onAssignmentClick: (Block) -> Unit,
    onNavigateToContent: (CourseContentTab) -> Unit,
    onNavigateToProgress: () -> Unit,
    getBlockParent: (blockId: String) -> Block?,
    onViewAllContentClick: () -> Unit,
    onViewAllVideosClick: () -> Unit,
    onViewAllAssignmentsClick: () -> Unit,
    onViewProgressClick: () -> Unit,
    onViewAllAnnouncementsClick: () -> Unit,
    onJoinClick: (org.openedx.core.data.model.LiveClassModel) -> Unit,
    onViewAllLiveSessionsClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = screenWidth,
                color = MaterialTheme.appColors.background
            ) {
                when (uiState) {
                    is CourseHomeUIState.CourseData -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.datesBannerInfo.isBannerAvailableForDashboard()) {
                                if (windowSize.isTablet) {
                                    CourseDatesBannerTablet(
                                        banner = uiState.datesBannerInfo,
                                        resetDates = onResetDatesClick,
                                    )
                                } else {
                                    CourseDatesBanner(
                                        banner = uiState.datesBannerInfo,
                                        resetDates = onResetDatesClick,
                                    )
                                }
                            }

                            val certificate = uiState.courseStructure.certificate
                            if (certificate?.isCertificateEarned() == true) {
                                CourseMessage(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = painterResource(R.drawable.course_ic_certificate),
                                    message = stringResource(
                                        R.string.course_you_earned_certificate,
                                        uiState.courseStructure.name
                                    ),
                                    action = stringResource(R.string.course_view_certificate),
                                    onActionClick = {
                                        onCertificateClick(
                                            certificate.certificateURL ?: ""
                                        )
                                    }
                                )
                            }

                            if (uiState.resumeComponent != null) {
                                ResumeButton(
                                    displayName = uiState.resumeUnitTitle,
                                    onClick = { onResumeClick(uiState.resumeComponent.id) }
                                )
                            }

                            // Live Sessions Card
                            DashboardCard {
                                LiveSessionsCardContent(
                                    uiState = uiState,
                                    onJoinClick = onJoinClick,
                                    onViewAllLiveSessionsClick = onViewAllLiveSessionsClick
                                )
                            }

                            // Course Completion Card
                            DashboardCard {
                                CourseCompletionHomePagerCardContent(
                                    uiState = uiState,
                                    onViewAllContentClick = {
                                        onViewAllContentClick()
                                        onNavigateToContent(CourseContentTab.ALL)
                                    },
                                    onDownloadClick = onDownloadClick,
                                    onSubSectionClick = onSubSectionClick
                                )
                            }

                            // Announcements Card
                            DashboardCard {
                                CourseAnnouncementsCardContent(
                                    announcements = uiState.announcements,
                                    onViewAllAnnouncementsClick = onViewAllAnnouncementsClick
                                )
                            }

                            // Videos Card
                            DashboardCard {
                                VideosHomePagerCardContent(
                                    uiState = uiState,
                                    onVideoClick = onVideoClick,
                                    onViewAllVideosClick = {
                                        onViewAllVideosClick()
                                        onNavigateToContent(CourseContentTab.VIDEOS)
                                    }
                                )
                            }

                            // Assignments Card
                            DashboardCard {
                                AssignmentsHomePagerCardContent(
                                    uiState = uiState,
                                    onAssignmentClick = onAssignmentClick,
                                    getBlockParent = getBlockParent,
                                    onViewAllAssignmentsClick = {
                                        onViewAllAssignmentsClick()
                                        onNavigateToContent(CourseContentTab.ASSIGNMENTS)
                                    }
                                )
                            }

                            // Grades Card
                            DashboardCard {
                                GradesHomePagerCardContent(
                                    uiState = uiState,
                                    onViewProgressClick = {
                                        onViewProgressClick()
                                        onNavigateToProgress()
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    CourseHomeUIState.Error -> {
                        NoContentScreen(noContentScreenType = NoContentScreenType.COURSE_OUTLINE)
                    }

                    CourseHomeUIState.Loading -> {
                        CircularProgress()
                    }

                    CourseHomeUIState.Waiting -> {}
                }
            }
        }
    }
}

@Composable
fun ResumeButton(
    displayName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        backgroundColor = Color(0xFF7CB342), // Green color from screenshot
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.appTypography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.appTypography.labelLarge,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ItemsRequiringAttentionDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.appColors.cardViewBackground,
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.appColors.cardViewBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Items Requiring Attention",
                        style = MaterialTheme.appTypography.titleMedium,
                        color = MaterialTheme.appColors.textDark,
                        fontWeight = FontWeight.Bold
                    )

                    // Item 1: Last Chance
                    AttentionItem(
                        icon = painterResource(id = coreR.drawable.core_ic_warning),
                        iconTint = Color(0xFFD32F2F),
                        title = "Last Chance!",
                        subtitle = "Basic Assessment Tools due tomorrow",
                        backgroundColor = Color(0xFFFFEBEE),
                        borderColor = Color(0xFFFFCDD2),
                        titleColor = Color(0xFFD32F2F)
                    )

                    // Item 2: Assignment Deadline
                    AttentionItem(
                        icon = painterResource(id = coreR.drawable.ic_core_watch_later),
                        iconTint = Color.DarkGray,
                        title = "Assignment Deadline",
                        subtitle = "Intermediate Assessment — Due Mar 20"
                    )

                    // Item 3: Pending Assignment
                    AttentionItem(
                        icon = painterResource(id = R.drawable.course_ic_announcements),
                        iconTint = Color(0xFF4CAF50),
                        title = "Pending Assignment",
                        subtitle = "Advanced Assessment — Due Mar 28",
                        iconBoxColor = Color(0xFFE8F5E9)
                    )

                    // Item 4: Course End Date
                    AttentionItem(
                        icon = painterResource(id = coreR.drawable.core_ic_calendar),
                        iconTint = Color(0xFF4CAF50),
                        title = "Course End Date",
                        subtitle = "Complete all modules by Apr 15",
                        iconBoxColor = Color(0xFFE8F5E9)
                    )
                }
            }
        }
    }
}

@Composable
fun AttentionItem(
    icon: Painter,
    iconTint: Color,
    title: String,
    subtitle: String,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.appColors.cardViewBorder.copy(alpha = 0.5f),
    titleColor: Color = MaterialTheme.appColors.textDark,
    iconBoxColor: Color = Color.Transparent
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBoxColor, CircleShape)
                    .then(
                        if (iconBoxColor == Color.Transparent) {
                            Modifier.border(
                                1.dp,
                                MaterialTheme.appColors.cardViewBorder.copy(alpha = 0.5f),
                                CircleShape
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.appTypography.titleSmall,
                    color = titleColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.appColors.cardViewBackground,
        border = BorderStroke(
            1.dp,
            MaterialTheme.appColors.cardViewBorder
        ),
        shape = MaterialTheme.appShapes.cardShape,
        elevation = 0.dp,
    ) {
        content()
    }
}

@Composable
fun ViewAllButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            tint = MaterialTheme.appColors.textAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textAccent
        )
    }
}

@Composable
fun CaughtUpMessage(
    modifier: Modifier = Modifier,
    message: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(coreR.drawable.core_ic_check),
            contentDescription = null,
            tint = MaterialTheme.appColors.successGreen
        )
        Text(
            modifier = modifier
                .fillMaxWidth(),
            text = message,
            color = MaterialTheme.appColors.textPrimary,
            style = MaterialTheme.appTypography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CourseHomeScreenPreview() {
    OpenEdXTheme {
        CourseHomeUI(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseHomeUIState.CourseData(
                courseStructure = Mock.mockCourseStructure,
                courseProgress = null, // No course progress for preview
                next = null, // No next section for preview
                downloadedState = mapOf(),
                resumeComponent = Mock.mockChapterBlock,
                resumeUnitTitle = "Resumed Unit",
                courseSubSections = mapOf(),
                subSectionsDownloadsCount = mapOf(),
                datesBannerInfo = CourseDatesBannerInfo(
                    missedDeadlines = false,
                    missedGatedContent = false,
                    verifiedUpgradeLink = "",
                    contentTypeGatingEnabled = false,
                    hasEnded = false
                ),
                useRelativeDates = true,
                courseVideos = mapOf(),
                courseAssignments = emptyList(),
                videoPreview = null,
                videoProgress = 0f
            ),
            uiMessage = null,
            onSubSectionClick = {},
            onResumeClick = {},
            onDownloadClick = {},
            onResetDatesClick = {},
            onCertificateClick = {},
            onVideoClick = {},
            onAssignmentClick = {},
            onNavigateToContent = { _ -> },
            onNavigateToProgress = {},
            getBlockParent = { null },
            onViewAllContentClick = {},
            onViewAllVideosClick = {},
            onViewAllAssignmentsClick = {},
            onViewProgressClick = {},
            onViewAllAnnouncementsClick = {},
            onJoinClick = {},
            onViewAllLiveSessionsClick = {}
        )
    }
}
