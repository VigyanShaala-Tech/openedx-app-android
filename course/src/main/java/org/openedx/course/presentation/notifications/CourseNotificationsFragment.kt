package org.openedx.course.presentation.notifications

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.course.R
import org.openedx.course.data.model.CourseNotificationItem
import org.openedx.core.R as coreR

class CourseNotificationsFragment : Fragment() {

    private val viewModel by viewModel<CourseNotificationsViewModel> {
        parametersOf(requireArguments().getString(ARG_COURSE_ID, ""))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                CourseNotificationsScreen(
                    courseTitle = requireArguments().getString(ARG_COURSE_TITLE, ""),
                    viewModel = viewModel,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_COURSE_TITLE = "courseTitle"

        fun newInstance(courseId: String, courseTitle: String): CourseNotificationsFragment {
            val fragment = CourseNotificationsFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId,
                ARG_COURSE_TITLE to courseTitle
            )
            return fragment
        }
    }
}

@Composable
fun CourseNotificationsScreen(
    courseTitle: String,
    viewModel: CourseNotificationsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                        text = courseTitle,
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
            when (uiState) {
                is CourseNotificationsUIState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CourseNotificationsUIState.Success -> {
                    val notifications = (uiState as CourseNotificationsUIState.Success).notifications
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No notifications found",
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            style = MaterialTheme.appTypography.bodyMedium,
                            color = MaterialTheme.appColors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(notifications) { notification ->
                                CourseNotificationItem(notification)
                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.appColors.divider
                                )
                            }
                        }
                    }
                }
                is CourseNotificationsUIState.Error -> {
                    Text(
                        text = (uiState as CourseNotificationsUIState.Error).message,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.appTypography.bodyMedium,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CourseNotificationsDialog(
    courseId: String,
    onDismiss: () -> Unit
) {
    val viewModel: CourseNotificationsViewModel = koinViewModel(parameters = { parametersOf(courseId) })
    val uiState by viewModel.uiState.collectAsState()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                backgroundColor = MaterialTheme.appColors.background,
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.items_requiring_attention),
                            style = MaterialTheme.appTypography.titleMedium,
                            color = MaterialTheme.appColors.textDark,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = MaterialTheme.appColors.textDark
                            )
                        }
                    }

                    Divider(color = MaterialTheme.appColors.divider)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (uiState) {
                            is CourseNotificationsUIState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            is CourseNotificationsUIState.Success -> {
                                val notifications = (uiState as CourseNotificationsUIState.Success).notifications
                                if (notifications.isEmpty()) {
                                    Text(
                                        text = "No notifications found",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Center),
                                        style = MaterialTheme.appTypography.bodyMedium,
                                        color = MaterialTheme.appColors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        items(notifications) { notification ->
                                            CourseNotificationItem(notification)
                                            Divider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                color = MaterialTheme.appColors.divider
                                            )
                                        }
                                    }
                                }
                            }
                            is CourseNotificationsUIState.Error -> {
                                Text(
                                    text = (uiState as CourseNotificationsUIState.Error).message,
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.appTypography.bodyMedium,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(8.dp)
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
fun CourseNotificationItem(notification: CourseNotificationItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        val icon = if (notification.type == "badge") {
            coreR.drawable.core_ic_certificate
        } else {
            coreR.drawable.core_ic_assignment
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.appColors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.appColors.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.appTypography.titleSmall,
                color = MaterialTheme.appColors.textDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.description,
                style = MaterialTheme.appTypography.bodySmall,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.createdAt,
                style = MaterialTheme.appTypography.labelSmall,
                color = MaterialTheme.appColors.textSecondary
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}
