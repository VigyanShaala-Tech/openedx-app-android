package org.openedx.course.presentation.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.foundation.presentation.rememberWindowSize

class CourseRegistrationFragment : Fragment() {

    private val viewModel by viewModel<CourseRegistrationViewModel> {
        parametersOf(
            requireArguments().getString(ARG_COURSE_ID, ""),
            requireArguments().getString(ARG_FORM_ID, "")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.collectAsState()
                val uiMessage by viewModel.uiMessage.collectAsState(initial = null)
                val answers by viewModel.answers.collectAsState()
                val eligibilityErrors by viewModel.eligibilityErrors.collectAsState()

                LaunchedEffect(uiState) {
                    if (uiState is CourseRegistrationUIState.SubmissionSuccess) {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }

                CourseRegistrationScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    answers = answers,
                    eligibilityErrors = eligibilityErrors,
                    onBackClick = {
                        if (viewModel.uiState.value is CourseRegistrationUIState.CourseData &&
                            (viewModel.uiState.value as CourseRegistrationUIState.CourseData).currentStep > 1
                        ) {
                            viewModel.previousStep()
                        } else {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    },
                    onNextClick = {
                        viewModel.nextStep()
                    },
                    onPreviousClick = {
                        viewModel.previousStep()
                    },
                    onAnswerUpdate = { field, answer ->
                        viewModel.updateAnswer(field, answer)
                    },
                    onAnswerUpdateByName = { fieldName, answer ->
                        viewModel.updateAnswer(fieldName, answer)
                    },
                    isNextEnabled = viewModel.isStepValid(),
                    isFieldVisible = { field -> viewModel.isFieldVisible(field) }
                )
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_FORM_ID = "formId"

        fun newInstance(courseId: String, formId: String): CourseRegistrationFragment {
            val fragment = CourseRegistrationFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId,
                ARG_FORM_ID to formId
            )
            return fragment
        }
    }
}
