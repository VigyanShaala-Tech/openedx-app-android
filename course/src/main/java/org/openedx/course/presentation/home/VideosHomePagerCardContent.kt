package org.openedx.course.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.openedx.core.domain.model.Block
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import org.openedx.course.presentation.contenttab.CourseContentVideoEmptyState
import org.openedx.course.presentation.ui.CourseVideoItem

@Composable
fun VideosHomePagerCardContent(
    uiState: CourseHomeUIState.CourseData,
    onVideoClick: (Block) -> Unit,
    onViewAllVideosClick: () -> Unit
) {
    val allVideos = uiState.courseVideos.values.flatten()
    if (allVideos.isEmpty()) {
        CourseContentVideoEmptyState(
            onReturnToCourseClick = {},
            showReturnButton = false
        )
        return
    }

    val completedVideos = allVideos.count { it.isCompleted() }
    val totalVideos = allVideos.size
    val firstIncompleteVideo = allVideos.find { !it.isCompleted() }
    val videoProgress = uiState.videoProgress ?: if (firstIncompleteVideo?.isCompleted() ?: false) {
        1f
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Videos",
            style = MaterialTheme.appTypography.titleLarge,
            color = MaterialTheme.appColors.textDark,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = null,
                tint = MaterialTheme.appColors.textDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$completedVideos/$totalVideos",
                style = MaterialTheme.appTypography.displaySmall,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Videos",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                )
                Text(
                    text = "completed",
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.appColors.cardViewBorder.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Watching section
        if (firstIncompleteVideo != null) {
            Text(
                text = "Next Video",
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Video card using CourseVideoItem
            CourseVideoItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                videoBlock = firstIncompleteVideo,
                preview = uiState.videoPreview,
                progress = videoProgress,
                onClick = {
                    onVideoClick(firstIncompleteVideo)
                },
                titleStyle = MaterialTheme.appTypography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    background = Color.Black.copy(alpha = 0.4f),
                ),
                contentModifier = Modifier.padding(0.dp),
                progressModifier = Modifier.height(0.dp),
                playButtonSize = 48.dp,
                borderColor = Color.Transparent
            )
        }
else {
            CaughtUpMessage(
                message = stringResource(R.string.course_videos_caught_up)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // View All Videos button
        ViewAllButton(
            text = stringResource(R.string.course_view_all_videos),
            onClick = onViewAllVideosClick
        )
    }
}
