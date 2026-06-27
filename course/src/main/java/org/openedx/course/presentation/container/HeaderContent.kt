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
    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = horizontalPadding, end = horizontalPadding, bottom = 24.dp, top = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = org ?: "",
                color = MaterialTheme.appColors.textSecondary,
                style = MaterialTheme.appTypography.labelMedium
            )
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(32.dp)
            ) {
                Box {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_triangle_alert),
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    // Red dot
                    if (haveNewNotification) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFD32F2F), CircleShape)
                                .border(1.2.dp, Color.White, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = courseTitle ?: "",
            color = Color(0xFF212121),
            style = MaterialTheme.appTypography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp
        )
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
