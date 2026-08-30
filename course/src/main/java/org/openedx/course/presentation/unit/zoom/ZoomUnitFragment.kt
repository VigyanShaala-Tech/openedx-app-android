package org.openedx.course.presentation.unit.zoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import us.zoom.sdkhelper.ZoomMeetingHelper

class ZoomUnitFragment : Fragment() {

    private val viewModel by viewModel<ZoomUnitViewModel> {
        parametersOf(
            requireArguments().getString(ARG_BLOCK_ID, ""),
            requireArguments().getString(ARG_COURSE_ID, "")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            ZoomUnitView(viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (ZoomMeetingHelper.getInstance().isMeetingOngoing()) {
                    // When back is pressed in our fragment while meeting is active,
                    // we show the mini window (PIP) and go back in our app.
                    ZoomMeetingHelper.getInstance().showMiniMeetingWindow()
                }
                isEnabled = false
                val parent = parentFragment
                if (parent is org.openedx.course.presentation.unit.container.CourseUnitContainerFragment) {
                    parent.handleBackNavigation()
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                isEnabled = true
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (ZoomMeetingHelper.getInstance().isMeetingOngoing()) {
            ZoomMeetingHelper.getInstance().showMiniMeetingWindow()
        }
    }

    companion object {
        private const val ARG_BLOCK_ID = "blockId"
        private const val ARG_COURSE_ID = "courseId"

        fun newInstance(
            blockId: String,
            courseId: String
        ): ZoomUnitFragment {
            val fragment = ZoomUnitFragment()
            fragment.arguments = bundleOf(
                ARG_BLOCK_ID to blockId,
                ARG_COURSE_ID to courseId
            )
            return fragment
        }
    }
}

@Composable
fun ZoomUnitView(viewModel: ZoomUnitViewModel) {
    val block by viewModel.block.collectAsState()
    val context = LocalContext.current
    val userName = viewModel.userName
    val userEmail = viewModel.userId

    var isMeetingOngoing by remember { mutableStateOf(ZoomMeetingHelper.getInstance().isMeetingOngoing()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            isMeetingOngoing = ZoomMeetingHelper.getInstance().isMeetingOngoing()
            delay(2000)
        }
    }

    OpenEdXTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            block?.let { blockData ->
                Text(
                    text = blockData.displayName,
                    style = MaterialTheme.appTypography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isMeetingOngoing) {
                    MeetingInProgressCard(
                        onReturnClick = {
                            ZoomMeetingHelper.getInstance().showMiniMeetingWindow()
                        },
                        onLeaveClick = {
                            ZoomMeetingHelper.getInstance().leaveMeeting(false)
                        }
                    )
                } else {
                    val meetingInfo = blockData.meetingInfo
                    if (meetingInfo != null) {
                        if (meetingInfo.startTime.isNotEmpty()) {
                            Text(
                                text = stringResource(id = org.openedx.core.R.string.core_label_starting, meetingInfo.startTime),
                                style = MaterialTheme.appTypography.labelLarge,
                                color = MaterialTheme.appColors.textDark
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = meetingInfo.isSessionOngoing,
                            onClick = {
                                ZoomMeetingHelper.getInstance().joinMeeting(
                                    context,
                                    meetingInfo.meetingId,
                                    meetingInfo.passcode,
                                    userName,
                                    userEmail
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.appColors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Join Meeting",
                                style = MaterialTheme.appTypography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!meetingInfo.isSessionOngoing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Meeting is not live yet",
                                style = MaterialTheme.appTypography.bodySmall,
                                color = MaterialTheme.appColors.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MeetingInProgressCard(
    onReturnClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.appColors.cardViewBackground,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Meeting is in progress",
                style = MaterialTheme.appTypography.titleSmall,
                color = MaterialTheme.appColors.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLeaveClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.appColors.error)
                ) {
                    Text("Leave")
                }
                Button(
                    onClick = onReturnClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.appColors.primary, contentColor = Color.White)
                ) {
                    Text("Return")
                }
            }
        }
    }
}
