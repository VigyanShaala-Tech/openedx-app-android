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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
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
import java.util.Calendar
import java.util.Locale

@Composable
fun LiveSessionsCardContent(
    modifier: Modifier = Modifier,
    isHomeScreen: Boolean = false,
    showTitle: Boolean = true,
    uiState: CourseHomeUIState.CourseData,
    onJoinClick: (LiveClassModel) -> Unit,
    onJoinOngoingClick: (String) -> Unit,
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
                onJoinClick = { ongoingSession.link?.let { onJoinOngoingClick(it) } }
            )
        }
    } else {
        TabbedLiveSessions(uiState, onJoinClick, onJoinOngoingClick, onViewAllLiveSessionsClick)
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = session.started ?: "",
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
    onJoinClick: (LiveClassModel) -> Unit,
    onJoinOngoingClick: (String) -> Unit,
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
                onJoinClick = { uiState.ongoingSession.link?.let { onJoinOngoingClick(it) } }
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
            sessions.take(5).forEach { session ->
                LiveSessionItem(
                    session = session,
                    isClickable = selectedTab == "today",
                    onJoinClick = { onJoinClick(session) }
                )
                Spacer(modifier = Modifier.height(8.dp))
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

@Composable
fun LiveSessionItem(
    session: LiveClassModel,
    isClickable: Boolean = true,
    onJoinClick: () -> Unit
) {
    val date = TimeUtils.iso8601ToDate(session.startTime) ?: Calendar.getInstance().time
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

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
                text = "${dateFormatter.format(date)} • ${timeFormatter.format(date)}",
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary
            )
        }
    }
}
