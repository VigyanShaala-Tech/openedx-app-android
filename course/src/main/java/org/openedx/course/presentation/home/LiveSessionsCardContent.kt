package org.openedx.course.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.data.model.LiveClassModel
import org.openedx.core.data.model.OngoingSessionModel
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.utils.TimeUtils
import org.openedx.course.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveSessionsCardContent(
    modifier: Modifier = Modifier,
    isHomeScreen: Boolean = false,
    showTitle: Boolean = true,
    uiState: CourseHomeUIState.CourseData,
    initialTab: String = "today",
    onTabChanged: (String) -> Unit = {},
    onLoadMore: (String) -> Unit = {},
    onJoinClick: (LiveClassModel) -> Unit,
    onJoinOngoingClick: (OngoingSessionModel) -> Unit,
    onViewAllLiveSessionsClick: () -> Unit
) {
    val ongoingSession = uiState.ongoingSession

    if (isHomeScreen && ongoingSession != null && ongoingSession.isSessionOngoing) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.course_happening_now),
                    style = MaterialTheme.appTypography.titleSmall,
                    color = MaterialTheme.appColors.textDark,
                    fontWeight = FontWeight.Bold
                )
            }

            OngoingSessionItem(
                session = ongoingSession,
                onJoinClick = {
                    onJoinOngoingClick(ongoingSession)
                }
            )
        }
    } else {
        TabbedLiveSessions(
            uiState = uiState,
            initialTab = initialTab,
            onTabChanged = onTabChanged,
            onLoadMore = onLoadMore,
            onJoinClick = onJoinClick,
            onJoinOngoingClick = onJoinOngoingClick,
            onViewAllLiveSessionsClick = onViewAllLiveSessionsClick
        )
    }
}

@Composable
fun OngoingSessionItem(
    session: OngoingSessionModel,
    onJoinClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.liveSessionBackground)
            .border(1.dp, MaterialTheme.appColors.liveSessionBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onJoinClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.appColors.liveSessionIconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.liveSessionIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.meetingName ?: "",
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textDark,
                    fontWeight = FontWeight.Bold
                )
                val startedDate = parseLiveSessionDate(session.started)
                val formattedStartedText = if (startedDate != null) {
                    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    "${dateFormatter.format(startedDate)} • ${timeFormatter.format(startedDate)}"
                } else {
                    session.started ?: ""
                }
                Text(
                    text = formattedStartedText,
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
            Button(
                onClick = onJoinClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.appColors.liveSessionJoinButton),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = session.linkLabel ?: stringResource(R.string.course_live_sessions_join),
                    color = Color.White,
                    fontSize = 12.sp,
                    style = MaterialTheme.appTypography.labelMedium
                )
            }
        }
    }
}

@Composable
fun TabbedLiveSessions(
    uiState: CourseHomeUIState.CourseData,
    initialTab: String,
    onTabChanged: (String) -> Unit,
    onLoadMore: (String) -> Unit = {},
    onJoinClick: (LiveClassModel) -> Unit,
    onJoinOngoingClick: (OngoingSessionModel) -> Unit,
    onViewAllLiveSessionsClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    LaunchedEffect(selectedTab) {
        onTabChanged(selectedTab)
    }

    val sessions = when (selectedTab) {
        "today" -> uiState.liveClassesToday
        "upcoming" -> uiState.liveClassesUpcoming
        "past" -> uiState.liveClassesPast
        else -> emptyList()
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.course_container_content_tab_live_sessions),
            style = MaterialTheme.appTypography.titleMedium,
            color = MaterialTheme.appColors.textDark,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_today),
                count = if (uiState.ongoingSession?.isSessionOngoing == true) uiState.liveClassesToday.size + 1 else uiState.liveClassesToday.size,
                isSelected = selectedTab == "today",
                onClick = { selectedTab = "today" }
            )
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_upcoming),
                count = uiState.liveClassesUpcoming.size,
                isSelected = selectedTab == "upcoming",
                onClick = { selectedTab = "upcoming" }
            )
            LiveTabButton(
                text = stringResource(R.string.course_live_sessions_past),
                count = uiState.liveClassesPast.size,
                isSelected = selectedTab == "past",
                onClick = { selectedTab = "past" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == "today" && uiState.ongoingSession?.isSessionOngoing == true) {
            OngoingSessionItem(
                session = uiState.ongoingSession,
                onJoinClick = { onJoinOngoingClick(uiState.ongoingSession) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (sessions.isEmpty() && (selectedTab != "today" || uiState.ongoingSession?.isSessionOngoing != true)) {
            Text(
                text = stringResource(R.string.course_live_sessions_no_sessions),
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            sessions.forEach { session ->
                LiveSessionItem(
                    session = session,
                    isClickable = selectedTab == "today" || selectedTab == "past",
                    onJoinClick = { onJoinClick(session) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val hasMoreForSelectedTab = when (selectedTab) {
                "today" -> uiState.liveClassesTodayHasMore
                "upcoming" -> uiState.liveClassesUpcomingHasMore
                "past" -> uiState.liveClassesPastHasMore
                else -> false
            }

            if (hasMoreForSelectedTab) {
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.isLoadingMoreLiveSessions) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.appColors.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(
                        onClick = { onLoadMore(selectedTab) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Load More",
                            style = MaterialTheme.appTypography.labelLarge,
                            color = MaterialTheme.appColors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$text ($count)",
            style = MaterialTheme.appTypography.labelSmall,
            color = if (isSelected) MaterialTheme.appColors.primaryButtonText else MaterialTheme.appColors.textPrimary
        )
    }
}

private fun parseLiveSessionDate(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank()) return null

    val isoDate = TimeUtils.iso8601ToDate(dateStr)
    if (isoDate != null) return isoDate

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            if (pattern.contains("'Z'") || pattern.contains("XXX")) {
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val parsed = sdf.parse(dateStr)
            if (parsed != null) return parsed
        } catch (_: Exception) {}
    }
    return null
}

@Composable
fun LiveSessionItem(
    session: LiveClassModel,
    isClickable: Boolean = true,
    onJoinClick: () -> Unit
) {
    val parsedDate = parseLiveSessionDate(session.startTime)
    val formattedDateText = if (parsedDate != null) {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        "${dateFormatter.format(parsedDate)} • ${timeFormatter.format(parsedDate)}"
    } else {
        session.startTime ?: ""
    }

    val clickableModifier = if (isClickable) {
        Modifier.clickable(onClick = onJoinClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.appColors.cardViewBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = if (isClickable) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.topic ?: "",
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formattedDateText,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary
            )
        }
    }
}
