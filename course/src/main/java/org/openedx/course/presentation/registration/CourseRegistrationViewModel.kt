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
import org.openedx.core.domain.model.EnrollmentRegistrationField
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

    private val formId = "6c2d8d459edb4b37" // Placeholder or from courseId

    private val _uiState = MutableStateFlow<CourseRegistrationUIState>(CourseRegistrationUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage.asSharedFlow()

    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers = _answers.asStateFlow()

    private val _eligibilityErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val eligibilityErrors = _eligibilityErrors.asStateFlow()

    init {
        getEnrollmentForm()
    }

    fun getEnrollmentForm() {
        _uiState.update { CourseRegistrationUIState.Loading }
        viewModelScope.launch {
            try {
                val enrollmentForm = interactor.getEnrollmentForm(formId)
                _uiState.update {
                    CourseRegistrationUIState.CourseData(
                        enrollmentForm = enrollmentForm,
                        currentStep = 1
                    )
                }
                
                // Call prefill API
                try {
                    val prefillData = interactor.getPrefillData(formId)
                    val processedAnswers = mutableMapOf<String, String>()
                    
                    prefillData.forEach { (key, value) ->
                        if (value != "null") {
                            if (value is List<*>) {
                                processedAnswers[key] = value.joinToString(",")
                            } else {
                                processedAnswers[key] = value.toString()
                            }
                        }
                    }
                    
                    if (processedAnswers.isNotEmpty()) {
                        _answers.update { it + processedAnswers }
                        
                        // Check eligibility for prefilled fields if they are eligibility fields
                        val allFields = enrollmentForm.categories.flatMap { it.fields }
                        processedAnswers.forEach { (key, value) ->
                            val field = allFields.find { it.name == key }
                            if (field?.isEligibilityField == true && value.isNotEmpty()) {
                                checkEligibility(key)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                _uiState.update { CourseRegistrationUIState.Error }
                handleError(e)
            }
        }
    }

    fun updateAnswer(field: EnrollmentRegistrationField, answer: String) {
        val fieldName = field.name
        _answers.update { it + (fieldName to answer) }
        
        // Clear previous error for this field
        _eligibilityErrors.update { it - fieldName }

        if (field.isEligibilityField && answer.isNotEmpty()) {
            checkEligibility(fieldName)
        }
    }

    private fun checkEligibility(triggerField: String) {
        viewModelScope.launch {
            try {
                val body = _answers.value.toMutableMap()
                body["triggerField"] = triggerField
                val result = interactor.checkEligibility(formId, body)
                if (!result.isEligible) {
                    _eligibilityErrors.update { it + (triggerField to result.message) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun isFieldVisible(field: EnrollmentRegistrationField): Boolean {
        // If field depends on another field, check if parent is answered
        if (field.dependsOn.isNotEmpty()) {
            return _answers.value[field.dependsOn]?.isNotEmpty() == true
        }
        
        // Step 1 logic: use visible flag from API
        val currentState = _uiState.value
        if (currentState is CourseRegistrationUIState.CourseData) {
            val category = currentState.enrollmentForm.categories.find { it.fields.contains(field) }
            if (category?.id == "01_registration") {
                return field.visible
            }
        }
        
        // Step 2 & 3 logic: show if visible:true OR if no dependency (since visible:false in JSON seems to be wrong for Step 2)
        return true
    }

    fun isStepValid(): Boolean {
        val currentState = _uiState.value
        if (currentState is CourseRegistrationUIState.CourseData) {
            val category = currentState.enrollmentForm.categories.getOrNull(currentState.currentStep - 1)
            val stepFields = category?.fields ?: return false
            
            // Check required fields and eligibility errors
            stepFields.forEach { field ->
                if (isFieldVisible(field)) {
                    if (field.required && _answers.value[field.name].isNullOrBlank()) return false
                    if (_eligibilityErrors.value[field.name] != null) return false
                    
                    if (field.type == "email") {
                        val answer = _answers.value[field.name] ?: ""
                        if (answer.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(answer).matches()) {
                            return false
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun submitRegistration() {
        val currentState = _uiState.value
        if (currentState is CourseRegistrationUIState.CourseData) {
            _uiState.update { currentState.copy(isSubmitting = true) }
            viewModelScope.launch {
                try {
                    val submissionBody = mutableMapOf<String, Any>()
                    
                    // Collect all fields from all categories
                    val allFields = currentState.enrollmentForm.categories.flatMap { it.fields }
                    
                    _answers.value.forEach { (key, value) ->
                        val field = allFields.find { it.name == key }
                        if (field?.type == "multi-select") {
                            // Convert comma-separated string to list
                            submissionBody[key] = value.split(",").filter { it.isNotEmpty() }
                        } else {
                            submissionBody[key] = value
                        }
                    }
                    
                    interactor.submitRegistration(formId, submissionBody)
                    
                    _uiState.update { currentState.copy(isSubmitting = false) }
                    _uiState.update { CourseRegistrationUIState.SubmissionSuccess }
                } catch (e: Exception) {
                    _uiState.update { currentState.copy(isSubmitting = false) }
                    handleError(e)
                }
            }
        }
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
    object SubmissionSuccess : CourseRegistrationUIState()
}
