package org.openedx.course.presentation.container

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.course.R
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.core.R as coreR

@Composable
internal fun ExpandedHeaderContent(
    modifier: Modifier = Modifier,
    org: String?,
    courseTitle: String?,
    haveNewNotification: Boolean = false,
    onNotificationClick: () -> Unit = {}
) {
    val windowSize = rememberWindowSize()
    val horizontalPadding = if (!windowSize.isTablet) {
        24.dp
    } else {
        98.dp
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.appColors.background)
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
    ) {
        Text(
            text = courseTitle ?: "",
            color = MaterialTheme.appColors.textDark,
            style = MaterialTheme.appTypography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp)
        )

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .offset(y = (-4).dp)
        ) {
            Box {
                Icon(
                    painter = painterResource(id = R.drawable.ic_course_notification_bell),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                // Red dot
                if (haveNewNotification) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.appColors.warningRed, CircleShape)
                            .border(1.2.dp, MaterialTheme.appColors.background, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun CollapsedHeaderContent(
    modifier: Modifier = Modifier,
    courseTitle: String?
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp),
        text = courseTitle ?: "",
        color = MaterialTheme.appColors.textDark,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.appTypography.titleSmall,
        maxLines = 1
    )
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun ExpandedHeaderContentPreview() {
    OpenEdXTheme {
        ExpandedHeaderContent(
            modifier = Modifier.fillMaxWidth(),
            org = "organization",
            courseTitle = "Course title"
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun CollapsedHeaderContentPreview() {
    OpenEdXTheme {
        CollapsedHeaderContent(
            modifier = Modifier.fillMaxWidth(),
            courseTitle = "Course title"
        )
    }
}
