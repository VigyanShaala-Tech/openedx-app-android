package org.openedx.course.presentation.registration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.course.domain.interactor.CourseRegistrationInteractor
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.core.R as coreR

class CourseRegistrationViewModel(
    val courseId: String,
    private val interactor: CourseRegistrationInteractor,
    private val resourceManager: ResourceManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<CourseRegistrationUIState>(CourseRegistrationUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        getEnrollmentForm()
    }

    fun getEnrollmentForm() {
        _uiState.update { CourseRegistrationUIState.Loading }
        viewModelScope.launch {
            try {
                // TODO: Replace with actual form ID if needed, currently using a placeholder or courseId
                val formId = "6c2d8d459edb4b37" // Placeholder
                val enrollmentForm = interactor.getEnrollmentForm(formId)
                _uiState.update {
                    CourseRegistrationUIState.CourseData(
                        enrollmentForm = enrollmentForm,
                        currentStep = 1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { CourseRegistrationUIState.Error }
                handleError(e)
            }
        }
    }

    fun nextStep() {
        val currentState = _uiState.value
        if (currentState is CourseRegistrationUIState.CourseData) {
            if (currentState.currentStep < currentState.enrollmentForm.categories.size) {
                _uiState.update { currentState.copy(currentStep = currentState.currentStep + 1) }
            } else {
                // Handle form submission
                submitRegistration()
            }
        }
    }

    fun previousStep() {
        val currentState = _uiState.value
        if (currentState is CourseRegistrationUIState.CourseData) {
            if (currentState.currentStep > 1) {
                _uiState.update { currentState.copy(currentStep = currentState.currentStep - 1) }
            }
        }
    }

    private fun submitRegistration() {
        // TODO: Implement submission logic
    }

    private suspend fun handleError(e: Exception) {
        val errorMessage = if (e.isInternetError()) {
            resourceManager.getString(coreR.string.core_error_no_connection)
        } else {
            e.message ?: resourceManager.getString(coreR.string.core_error_unknown_error)
        }
        _uiMessage.emit(UIMessage.SnackBarMessage(errorMessage))
    }
}

sealed class CourseRegistrationUIState {
    object Loading : CourseRegistrationUIState()
    data class CourseData(
        val enrollmentForm: EnrollmentForm,
        val currentStep: Int,
        val isSubmitting: Boolean = false
    ) : CourseRegistrationUIState()
    object Error : CourseRegistrationUIState()
}
