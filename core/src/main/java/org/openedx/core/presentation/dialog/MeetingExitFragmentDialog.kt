package org.openedx.core.presentation.dialog

import android.app.PictureInPictureParams
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import org.openedx.core.R
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.OpenEdXOutlinedButton
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography

class MeetingExitFragmentDialog : DialogFragment() {

    var listener: MeetingExitDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                MeetingExitDialogScreen(
                    onConfirm = {
                        dismiss()
                        listener?.onConfirm()
                    },
//                    onMinimize = {
//                        dismiss()
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            val paramsBuilder = PictureInPictureParams.Builder()
//                                .setAspectRatio(Rational(16, 9))
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                                paramsBuilder.setAutoEnterEnabled(true)
//                                paramsBuilder.setSeamlessResizeEnabled(true)
//                            }
//                            try {
//                                activity?.enterPictureInPictureMode(paramsBuilder.build())
//                            } catch (e: Exception) {
//                                // Ignore
//                            }
//                        }
//                    },
                    onCancel = {
                        dismiss()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        listener = null
        super.onDestroy()
    }

    companion object {
        fun newInstance(): MeetingExitFragmentDialog {
            return MeetingExitFragmentDialog()
        }
    }
}

interface MeetingExitDialogListener {
    fun onConfirm()
//    fun onMinimize()
}

@Composable
private fun MeetingExitDialogScreen(
    onConfirm: () -> Unit,
//    onMinimize: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.95f)
            .clip(MaterialTheme.appShapes.courseImageShape),
        backgroundColor = MaterialTheme.appColors.background,
        shape = MaterialTheme.appShapes.courseImageShape
    ) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.course_leave_meeting_title),
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.course_leave_meeting_message),
                color = MaterialTheme.appColors.textFieldText,
                style = MaterialTheme.appTypography.titleSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                OpenEdXButton(
//                    text = stringResource(id = R.string.core_minimize),
//                    onClick = onMinimize
//                )
//                Spacer(Modifier.height(16.dp))
//            }
            OpenEdXButton(
                text = stringResource(id = R.string.course_leave_meeting_yes),
                onClick = onConfirm,
                backgroundColor = MaterialTheme.appColors.error,
                textColor = Color.White
            )
            Spacer(Modifier.height(16.dp))
            OpenEdXOutlinedButton(
                borderColor = MaterialTheme.appColors.primaryButtonBackground,
                textColor = MaterialTheme.appColors.primaryButtonBackground,
                text = stringResource(id = R.string.course_leave_meeting_no),
                onClick = onCancel
            )
        }
    }
}
