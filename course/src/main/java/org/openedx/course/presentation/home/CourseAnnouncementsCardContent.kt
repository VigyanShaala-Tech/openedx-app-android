package org.openedx.course.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.domain.model.AnnouncementModel
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R

@Composable
fun CourseAnnouncementsCardContent(
    announcements: List<AnnouncementModel>,
    onViewAllAnnouncementsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Announcement,
                contentDescription = null,
                tint = MaterialTheme.appColors.textDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.course_announcements),
                style = MaterialTheme.appTypography.titleMedium,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (announcements.isEmpty()) {
            Text(
                text = stringResource(org.openedx.core.R.string.core_no_announcements),
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            announcements.take(2).forEach { announcement ->
                AnnouncementItem(announcement = announcement)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ViewAllButton(
            text = "View All Announcements",
            onClick = onViewAllAnnouncementsClick
        )
    }
}

@Composable
private fun AnnouncementItem(announcement: AnnouncementModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.appColors.background,
        border = BorderStroke(1.dp, MaterialTheme.appColors.cardViewBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = announcement.date ?: "",
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Basic HTML stripping
            val plainText = androidx.core.text.HtmlCompat.fromHtml(
                announcement.content ?: "",
                androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
            ).toString().trim()

            // Try to split title and body if possible, or just show as in image
            val lines = plainText.lines().filter { it.isNotBlank() }
            val title = lines.getOrNull(0) ?: ""
            val body = lines.drop(1).joinToString(" ")

            Text(
                text = title,
                style = MaterialTheme.appTypography.bodyMedium,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (body.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
