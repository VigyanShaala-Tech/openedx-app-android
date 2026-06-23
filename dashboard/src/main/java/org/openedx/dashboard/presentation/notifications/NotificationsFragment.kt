package org.openedx.dashboard.presentation.notifications

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.domain.model.NotificationModel
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.Toolbar
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.dashboard.R

class NotificationsFragment : Fragment() {

    private val viewModel by viewModel<NotificationsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val notifications by viewModel.notifications.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                NotificationsScreen(
                    notifications = notifications,
                    isLoading = isLoading,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationsScreen(
    notifications: List<NotificationModel>,
    isLoading: Boolean,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background),
        topBar = {
            Column(modifier = Modifier.statusBarsInset()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    BackBtn(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onBackClick = onBackClick
                    )
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 56.dp),
                        text = stringResource(id = R.string.dashboard_notifications),
                        style = MaterialTheme.appTypography.titleMedium,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.appColors.background)
        ) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.appColors.primary
                )
            } else if (notifications.isEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "No notifications",
                    style = MaterialTheme.appTypography.bodyMedium,
                    color = MaterialTheme.appColors.textSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification = notification)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: NotificationModel) {
    val icon = when (notification.type.lowercase()) {
        "course" -> Icons.Default.MenuBook
        "certificate" -> Icons.Default.Badge
        "discussion" -> Icons.Default.ChatBubbleOutline
        "assignment" -> Icons.Default.CalendarToday
        "badge" -> Icons.Default.Stars
        else -> Icons.Default.Notifications
    }

    val backgroundColor = if (notification.isRead) {
        MaterialTheme.appColors.background
    } else {
        Color(0xFFF7F9F2) // Light green tint for unread as in image
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F4F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF90A4AE),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.appTypography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.appColors.textPrimary
                    )
                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    style = MaterialTheme.appTypography.bodySmall,
                    color = MaterialTheme.appColors.textSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.createdAt, // Ideally formatted
                    style = MaterialTheme.appTypography.labelSmall,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun NotificationItemPreview() {
    OpenEdXTheme {
        NotificationItem(
            notification = NotificationModel(
                id = 1,
                title = "New Course Available",
                description = "Advanced Machine Learning is now available for enrollment.",
                type = "course",
                isRead = false,
                createdAt = "2 hours ago"
            )
        )
    }
}
