package org.openedx.course.presentation.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import org.openedx.core.NoContentScreenType
import org.openedx.core.domain.model.CourseProgress
import org.openedx.core.domain.model.DashboardProgress
import org.openedx.core.ui.CircularProgress
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.NoContentScreen
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun CourseProgressScreen(
    windowSize: WindowSize,
    viewModel: CourseProgressViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState(null)

    when (val state = uiState) {
        is CourseProgressUIState.Loading -> CircularProgress()
        is CourseProgressUIState.Error -> NoContentScreen(NoContentScreenType.COURSE_PROGRESS)
        is CourseProgressUIState.Data -> CourseProgressContent(
            uiState = state,
            uiMessage = uiMessage,
            windowSize = windowSize,
        )
    }
}

@Composable
private fun CourseProgressContent(
    uiState: CourseProgressUIState.Data,
    uiMessage: UIMessage?,
    windowSize: WindowSize
) {
    val scaffoldState = rememberScaffoldState()
    val gradingPolicy = uiState.progress.gradingPolicy

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = screenWidth,
                color = MaterialTheme.appColors.background,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    uiState.dashboardProgress?.let { dashboard ->
                        item {
                            DashboardHeader()
                        }
                        item {
                            SummaryCardsGrid(dashboard.summaryCards)
                        }
                        if (dashboard.quizScores.isNotEmpty()) {
                            item {
                                QuizScoreChart(dashboard.quizScores)
                            }
                        }
                        item {
                            OverallPerformanceView(dashboard.overallPerformance)
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                    item {
                        CourseCompletionView(
                            progress = uiState.progress
                        )
                    }
                    if (gradingPolicy == null) return@LazyColumn
                    val assignmentPolicies = uiState.progress.getNotEmptyGradingPolicies()
                    if (!assignmentPolicies.isNullOrEmpty()) {
                        item {
                            OverallGradeView(
                                progress = uiState.progress,
                            )
                        }
                        item {
                            GradeDetailsHeaderView()
                        }
                        itemsIndexed(assignmentPolicies) { index, policy ->
                            AssignmentTypeRow(
                                uiState = uiState,
                                policy = policy,
                                color = if (gradingPolicy.assignmentColors.isNotEmpty()) {
                                    gradingPolicy.assignmentColors[index % gradingPolicy.assignmentColors.size]
                                } else {
                                    MaterialTheme.appColors.primary
                                }
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                        item {
                            GradeDetailsFooterView(
                                progress = uiState.progress
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                NoGradesView()
                            }
                        }
                    }
                }
            }

            HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)
        }
    }
}

@Composable
private fun NoGradesView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
            contentDescription = null,
            tint = MaterialTheme.appColors.divider
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.course_progress_no_assignments),
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GradeDetailsHeaderView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.course_progress_grade_details),
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.course_progress_assignment_type),
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textPrimaryVariant,
            )
            Text(
                text = stringResource(R.string.course_progress_current_max),
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textPrimaryVariant,
            )
        }
    }
}

@Composable
fun GradeDetailsFooterView(
    progress: CourseProgress,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.course_progress_current_overall),
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textDark,
        )
        Text(
            text = "${progress.getTotalWeightPercent().toInt()}%",
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OverallGradeView(
    progress: CourseProgress,
) {
    val gradingPolicy = progress.gradingPolicy
    if (gradingPolicy == null) return
    val notCompletedWeightedGradePercent = progress.getNotCompletedWeightedGradePercent()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.course_progress_overall_title),
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark,
        )
        Text(
            text = stringResource(R.string.course_progress_overall_description),
            style = MaterialTheme.appTypography.labelMedium,
            color = MaterialTheme.appColors.textDark,
        )
        CurrentOverallGradeText(progress = progress)
        Column {
            GradeProgressBar(
                progress = progress,
                gradingPolicy = gradingPolicy,
                notCompletedWeightedGradePercent = notCompletedWeightedGradePercent
            )
            RequiredGradeMarker(progress = progress)
        }

        Surface(
            color = MaterialTheme.appColors.cardViewBackground,
            shape = MaterialTheme.appShapes.cardShape,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.appColors.warning
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.warning,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        R.string.course_progress_required_grade_percent,
                        progress.requiredGradePercent.toString()
                    ),
                    style = MaterialTheme.appTypography.labelLarge,
                    color = MaterialTheme.appColors.textDark,
                )
            }
        }
    }
}

@Composable
private fun CourseCompletionView(
    progress: CourseProgress
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.course_progress_completion_title),
                style = MaterialTheme.appTypography.titleMedium,
                color = MaterialTheme.appColors.textDark,
            )
            Text(
                text = stringResource(R.string.course_progress_completion_description),
                style = MaterialTheme.appTypography.labelMedium,
                color = MaterialTheme.appColors.textDark,
            )
        }
        CourseCompletionCircularProgress(
            progress = progress.completion,
            progressPercent = progress.completionPercent,
            completedText = stringResource(R.string.course_completed)
        )
    }
}

@Composable
private fun AssignmentTypeRow(
    uiState: CourseProgressUIState.Data,
    policy: CourseProgress.GradingPolicy.AssignmentPolicy,
    color: Color
) {
    val assignments = uiState.progress.getAssignmentSections(policy.type)
    val earned = uiState.progress.getCompletedAssignmentCount(policy, uiState.courseStructure)
    val possible = assignments.size
    Column(
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = policy.type ?: "",
            style = MaterialTheme.appTypography.labelLarge,
            color = MaterialTheme.appColors.textPrimary,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(7.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.course_progress_earned_possible_assignment_problems,
                        earned,
                        possible
                    ),
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textDark,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("${(policy.weight * 100).toInt()}%")
                        }
                        append(" ")
                        append(stringResource(R.string.course_progress_of_grade))
                    },
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textDark,
                )
            }
            Text(
                stringResource(
                    R.string.course_progress_current_and_max_weighted_graded_percent,
                    uiState.progress.getAssignmentWeightedGradedPercent(policy).toInt(),
                    (policy.weight * 100).toInt()
                ),
                style = MaterialTheme.appTypography.bodyLarge,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.appColors.textDark,
            )
        }
    }
}

@Composable
fun CourseCompletionCircularProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    progressPercent: Int,
    completedText: String?
) {
    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(80.dp),
            progress = 1f,
            color = MaterialTheme.appColors.progressBarBackgroundColor,
            strokeWidth = 4.dp,
        )
        CircularProgressIndicator(
            modifier = Modifier
                .size(80.dp),
            progress = progress,
            color = Color(0xFF4CAF50), // Green as in image
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.appTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textDark,
            )
            Text(
                text = completedText ?: "",
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun GradeProgressBar(
    progress: CourseProgress,
    gradingPolicy: CourseProgress.GradingPolicy,
    notCompletedWeightedGradePercent: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.appColors.gradeProgressBarBorder,
                shape = CircleShape
            )
    ) {
        gradingPolicy.assignmentPolicies.forEachIndexed { index, assignmentPolicy ->
            val assignmentColors = gradingPolicy.assignmentColors
            val color = if (assignmentColors.isNotEmpty()) {
                assignmentColors[
                    gradingPolicy.assignmentPolicies.indexOf(
                        assignmentPolicy
                    ) % assignmentColors.size
                ]
            } else {
                MaterialTheme.appColors.primary
            }
            val weightedPercent =
                progress.getAssignmentWeightedGradedPercent(assignmentPolicy)
            if (weightedPercent > 0f) {
                Box(
                    modifier = Modifier
                        .weight(weightedPercent)
                        .background(color)
                        .fillMaxHeight()
                )

                // Add black separator between assignment policies (except after the last one)
                if (index < gradingPolicy.assignmentPolicies.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .background(Color.Black)
                            .fillMaxHeight()
                    )
                }
            }
        }
        if (notCompletedWeightedGradePercent > 0f) {
            Box(
                modifier = Modifier
                    .weight(notCompletedWeightedGradePercent)
                    .background(MaterialTheme.appColors.gradeProgressBarBackground)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun RequiredGradeMarker(
    progress: CourseProgress
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(progress.requiredGrade),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier.offset(x = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_course_marker),
                tint = MaterialTheme.appColors.warning,
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .offset(y = 2.dp)
                    .clearAndSetSemantics { },
                text = "${progress.requiredGradePercent}%",
                style = MaterialTheme.appTypography.labelMedium,
                color = MaterialTheme.appColors.textDark,
            )
        }
    }
}

@Composable
fun CurrentOverallGradeText(
    progress: CourseProgress
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.appColors.textDark,
                    fontSize = MaterialTheme.appTypography.labelMedium.fontSize,
                    fontFamily = MaterialTheme.appTypography.labelMedium.fontFamily,
                    fontWeight = MaterialTheme.appTypography.labelMedium.fontWeight
                )
            ) {
                append(stringResource(R.string.course_progress_current_overall) + " ")
            }
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.appColors.primary,
                    fontSize = MaterialTheme.appTypography.labelMedium.fontSize,
                    fontFamily = MaterialTheme.appTypography.labelMedium.fontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append("${progress.getTotalWeightPercent().toInt()}%")
            }
        },
        style = MaterialTheme.appTypography.labelMedium,
    )
}

@Composable
private fun DashboardHeader() {
    Column {
        Text(
            text = "Your progress",
            style = MaterialTheme.appTypography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textDark,
                fontSize = 24.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Track live sessions, quizzes, assignments and watch time at a glance.",
            style = MaterialTheme.appTypography.bodyMedium,
            color = MaterialTheme.appColors.textSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCardsGrid(cards: List<DashboardProgress.SummaryCard>) {
    val rows = cards.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowCards.forEach { card ->
                    SummaryCardView(
                        modifier = Modifier.weight(1f),
                        card = card
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryCardView(modifier: Modifier = Modifier, card: DashboardProgress.SummaryCard) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.appColors.background,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = try { Color(card.color.toColorInt()).copy(alpha = 0.1f) } catch (_: Exception) { Color.Gray.copy(alpha = 0.1f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (card.icon) {
                            "video" -> Icons.Default.Videocam
                            "clipboardCheck" -> Icons.Default.AssignmentTurnedIn
                            "fileText" -> Icons.Default.Description
                            "clock" -> Icons.Default.AccessTime
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = try { Color(card.color.toColorInt()) } catch (_: Exception) { Color.Gray },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column {
                Text(
                    text = card.value,
                    style = MaterialTheme.appTypography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.appColors.textDark
                    )
                )
                Text(
                    text = card.name,
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun QuizScoreChart(quizScores: List<DashboardProgress.QuizScoreData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.appColors.background,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.textDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quiz score",
                    style = MaterialTheme.appTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.appColors.textDark
                )
            }
            Text(
                text = "Score across quizzes",
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 24.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 40.dp.toPx()
                    val spacing = (width - (barWidth * quizScores.size)) / (quizScores.size + 1)
                    
                    for (i in 0..4) {
                        val y = height - (height * i / 4)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    quizScores.forEachIndexed { index, quiz ->
                        val x = spacing + (index * (barWidth + spacing))
                        val barHeight = if (quiz.max > 0) (height * (quiz.score / quiz.max)).toFloat() else 0f
                        
                        drawRect(
                            color = Color(0xFF2C4869),
                            topLeft = androidx.compose.ui.geometry.Offset(x, height - barHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                quizScores.forEach { quiz ->
                    Text(
                        text = quiz.name,
                        style = MaterialTheme.appTypography.bodySmall,
                        color = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun OverallPerformanceView(performance: DashboardProgress.OverallPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.appColors.background,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.textDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Overall performance",
                    style = MaterialTheme.appTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.appColors.textDark
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val center = size.center
                    val maxRadius = size.minDimension / 2
                    val strokeWidth = 15.dp.toPx()
                    
                    performance.radialData.forEachIndexed { index, data ->
                        val radius = maxRadius - (index * (strokeWidth + 10.dp.toPx()))
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            radius = radius,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = when (index) {
                                0 -> Color(0xFF69AB4A)
                                1 -> Color(0xFF2C4869)
                                2 -> Color(0xFFFFCC29)
                                else -> Color.Gray
                            },
                            startAngle = -90f,
                            sweepAngle = (data.value * 3.6).toFloat(),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            performance.progressBars.forEachIndexed { index, bar ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bar.name,
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textSecondary
                        )
                        Text(
                            text = "${bar.value.toInt()}%",
                            style = MaterialTheme.appTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.appColors.textDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = (bar.value / 100).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = when (index) {
                            0 -> Color(0xFF69AB4A)
                            1 -> Color(0xFF2C4869)
                            2 -> Color(0xFFFFCC29)
                            else -> MaterialTheme.appColors.primary
                        },
                        backgroundColor = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
            
            if (performance.encouragementMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_course_marker),
                            contentDescription = null,
                            tint = Color(0xFF69AB4A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = performance.encouragementMessage,
                            style = MaterialTheme.appTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF69AB4A)
                        )
                    }
                }
            }
        }
    }
}
