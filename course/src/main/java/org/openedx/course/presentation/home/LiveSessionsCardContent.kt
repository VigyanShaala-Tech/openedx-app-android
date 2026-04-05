package org.openedx.course.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.data.model.LiveClassModel
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.TimeUtils
import org.openedx.course.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun LiveSessionsCardContent(
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    uiState: CourseHomeUIState.CourseData,
    onJoinClick: (LiveClassModel) -> Unit,
    onViewAllLiveSessionsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("today") }

    val sessions = when (selectedTab) {
        "today" -> uiState.liveClassesToday
        "upcoming" -> uiState.liveClassesUpcoming
        "past" -> uiState.liveClassesPast
        else -> emptyList()
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showTitle) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.course_container_content_tab_live_sessions),
                style = MaterialTheme.appTypography.titleMedium,
                color = MaterialTheme.appColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_today),
                count = uiState.liveClassesToday.size,
                isSelected = selectedTab == "today",
                onClick = { selectedTab = "today" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_upcoming),
                count = uiState.liveClassesUpcoming.size,
                isSelected = selectedTab == "upcoming",
                onClick = { selectedTab = "upcoming" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_past),
                count = uiState.liveClassesPast.size,
                isSelected = selectedTab == "past",
                onClick = { selectedTab = "past" }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No live sessions found",
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            sessions.take(10).forEach { session ->
                LiveSessionItem(
                    session = session,
                    type = selectedTab,
                    onJoinClick = { onJoinClick(session) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (sessions.size > 10) {
            ViewAllButton(
                text = stringResource(R.string.course_view_all_videos), // Reuse for now
                onClick = onViewAllLiveSessionsClick
            )
        }
    }
}

@Composable
fun LiveTabButton(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.cardViewBackground
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$text ($count)",
            style = MaterialTheme.appTypography.labelMedium.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            color = if (isSelected) MaterialTheme.appColors.primaryButtonText else MaterialTheme.appColors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LiveSessionItem(
    session: LiveClassModel,
    type: String,
    onJoinClick: () -> Unit
) {
    val isToday = type == "today"
    val isPast = type == "past"

    val date = TimeUtils.iso8601ToDate(session.startTime) ?: Calendar.getInstance().time
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.HOUR, session.durationHours)
    calendar.add(Calendar.MINUTE, session.durationMinutes)
    val endDate = calendar.time

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.background)
            .then(
                if (isToday) Modifier.background(Color(0xFFFFEAEA)) else Modifier
            )
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.appColors.cardViewBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isToday) Icons.Default.RadioButtonChecked else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = if (isToday) Color.Red else MaterialTheme.appColors.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.topic,
                        style = MaterialTheme.appTypography.titleSmall,
                        color = MaterialTheme.appColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (session.isOwner) "Hosted by you" else "Dr. Sharma",
                        style = MaterialTheme.appTypography.labelSmall,
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
                
                if (isToday) {
                    Button(
                        onClick = onJoinClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.course_live_sessions_join),
                            color = Color.White,
                            fontSize = 12.sp,
                            style = MaterialTheme.appTypography.labelMedium.copy(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                } else if (isPast) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.appColors.cardViewBackground)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.course_live_sessions_completed),
                            style = MaterialTheme.appTypography.labelSmall,
                            color = MaterialTheme.appColors.textSecondary
                        )
                    }
                } else {
                     Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Upcoming",
                            style = MaterialTheme.appTypography.labelSmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.appColors.textSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateFormatter.format(date),
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.appColors.textSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${timeFormatter.format(date)} - ${timeFormatter.format(endDate)}",
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    }
}
