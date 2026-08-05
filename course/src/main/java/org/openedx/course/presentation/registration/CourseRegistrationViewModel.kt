package org.openedx.course.presentation.registration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.course.domain.interactor.CourseRegistrationInteractor
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.core.domain.model.EnrollmentRegistrationField
import org.openedx.core.domain.model.EnrollmentRegistrationOption
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.openedx.core.system.notifier.CourseDashboardUpdate
import org.openedx.core.system.notifier.DiscoveryNotifier
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import java.io.File
import org.openedx.core.R as coreR

class CourseRegistrationViewModel(
    val courseId: String,
    val formId: String,
    private val interactor: CourseRegistrationInteractor,
    private val resourceManager: ResourceManager,
    private val notifier: DiscoveryNotifier,
    private val corePreferences: CorePreferences
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<CourseRegistrationUIState>(CourseRegistrationUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage.asSharedFlow()

    private val _answers = MutableStateFlow<Map<String, String>>(emptyMap())
    val answers = _answers.asStateFlow()

    private val _eligibilityErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val eligibilityErrors = _eligibilityErrors.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    init {
        getEnrollmentForm()
    }

    fun getEnrollmentForm() {
        _uiState.update { CourseRegistrationUIState.Loading }
        viewModelScope.launch {
            try {
                var enrollmentForm = interactor.getEnrollmentForm(formId)
                
                // Call prefill API
                val processedAnswers = try {
                    val body = mutableMapOf<String, String>()
                    corePreferences.user?.email?.let {
                        body["email"] = it
                    }
                    val prefillData = interactor.getPrefillData(formId, body)
                    val map = mutableMapOf<String, String>()
                    
                    prefillData.forEach { (key, value) ->
                        if (value != null && value.toString() != "null") {
                            if (value is List<*>) {
                                map[key] = value.joinToString("|")
                            } else {
                                map[key] = value.toString()
                            }
                        }
                    }
                    map
                } catch (_: Exception) {
                    mutableMapOf<String, String>()
                }

                val finalAnswers = processedAnswers.toMutableMap()
                val nonEditableFields = mutableSetOf<String>()

                // Check for full_name and email to prefill from user profile if not in prefill data
                val allFields = enrollmentForm.categories.flatMap { it.fields }
                
                allFields.forEach { field ->
                    if (field.name == "full_name" || field.name == "email") {
                        var value = finalAnswers[field.name]
                        if (value.isNullOrBlank()) {
                            value = if (field.name == "full_name") corePreferences.user?.name else corePreferences.user?.email
                        }
                        
                        if (!value.isNullOrBlank()) {
                            finalAnswers[field.name] = value
                            nonEditableFields.add(field.name)
                        }
                    }
                }

                // Update isEditable in enrollmentForm domain model
                val updatedCategories = enrollmentForm.categories.map { category ->
                    category.copy(fields = category.fields.map { field ->
                        if (nonEditableFields.contains(field.name)) {
                            field.copy(isEditable = false)
                        } else {
                            field
                        }
                    })
                }
                enrollmentForm = enrollmentForm.copy(categories = updatedCategories)

                if (finalAnswers.isNotEmpty()) {
                    _answers.update { it + finalAnswers }
                    
                    // Check eligibility for prefilled fields if they are eligibility fields
                    finalAnswers.forEach { (key, value) ->
                        val field = allFields.find { it.name == key }
                        if (field?.isEligibilityField == true && value.isNotEmpty()) {
                            checkEligibility(key)
                        }
                    }
                }

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

    fun updateAnswer(field: EnrollmentRegistrationField, answer: String) {
        val fieldName = field.name
        
        if (field.type == "file" && answer.isNotEmpty()) {
            val file = File(answer)
            if (file.exists()) {
                val validationError = validateFile(file)
                if (validationError != null) {
                    viewModelScope.launch {
                        _uiMessage.emit(UIMessage.SnackBarMessage(validationError))
                    }
                    return
                }

                _isUploading.value = true
                viewModelScope.launch {
                    try {
                        val email = corePreferences.user?.email ?: ""
                        interactor.uploadFile(formId, fieldName, courseId, email, file)
                        updateAnswer(fieldName, file.name)
                    } catch (e: Exception) {
                        handleError(e)
                    } finally {
                        _isUploading.value = false
                    }
                }
            }
        } else {
            updateAnswer(fieldName, answer)
        }

        if (field.isEligibilityField && answer.isNotEmpty()) {
            checkEligibility(fieldName)
        }
    }

    fun updateAnswer(fieldName: String, answer: String) {
        _answers.update { it + (fieldName to answer) }
        
        // Clear previous error for this field
        _eligibilityErrors.update { it - fieldName }
    }

    private fun validateFile(file: File): String? {
        val maxSizeInBytes = 5 * 1024 * 1024 // 5MB
        if (file.length() > maxSizeInBytes) {
            return "File size exceeds 5MB limit"
        }
        
        val allowedExtensions = listOf("pdf", "jpg", "jpeg", "png", "doc", "docx")
        val extension = file.extension.lowercase()
        if (extension !in allowedExtensions) {
            return "Unsupported file type. Please upload PDF, JPG, PNG or DOC file."
        }
        
        return null
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
                    val answer = _answers.value[field.name] ?: ""
                    if (field.required && answer.isBlank()) return false
                    
                    if (isOtherSelected(field, answer) && field.required) {
                        // The 'answer' itself is the manual input if Other is selected
                        if (answer.isBlank() || isDefaultOtherValue(answer)) return false
                    }

                    if (_eligibilityErrors.value[field.name] != null) return false
                    
                    if (field.type == "email") {
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
                            // Convert pipe-separated string to list
                            submissionBody[key] = value.split("|").filter { it.isNotEmpty() }
                        } else {
                            submissionBody[key] = value
                        }
                    }
                    
                    val response = interactor.submitRegistration(formId, submissionBody)
                    
                    _uiState.update { currentState.copy(isSubmitting = false) }
                    _uiMessage.emit(UIMessage.SnackBarMessage(response.thanksMessage))
                    _uiState.update { CourseRegistrationUIState.SubmissionSuccess }
                    notifier.send(CourseDashboardUpdate())
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

    private fun isDefaultOtherValue(value: String): Boolean {
        return value.lowercase() == "other" || value.lowercase() == "others" || value.lowercase() == "__other__"
    }

    private fun isOtherSelected(field: EnrollmentRegistrationField, answer: String): Boolean {
        // Only relevant for fields with predefined options
        if (field.type !in listOf("select", "multi-select", "radio")) return false

        val selectedValues = answer.split("|").filter { it.isNotEmpty() }
        if (selectedValues.isEmpty()) return false

        // Quick check on values
        if (selectedValues.any { isDefaultOtherValue(it) }) return true

        // Check if any selected value is NOT in the predefined options list (it's a custom manual value)
        val options = parseOptions(field)
        return selectedValues.any { valId ->
            options.none { it.value == valId }
        }
    }

    private fun parseOptions(field: EnrollmentRegistrationField): List<EnrollmentRegistrationOption> {
        val options = field.options ?: return emptyList()
        if (options is List<*>) {
            return options.filterIsInstance<EnrollmentRegistrationOption>()
        }
        return try {
            val gson = Gson()
            val json = gson.toJson(options)
            if (field.dependsOn.isNotEmpty()) {
                val type = object : TypeToken<Map<String, List<EnrollmentRegistrationOption>>>() {}.type
                val map = gson.fromJson<Map<String, List<EnrollmentRegistrationOption>>>(json, type)
                map[_answers.value[field.dependsOn]] ?: emptyList()
            } else {
                val type = object : TypeToken<List<EnrollmentRegistrationOption>>() {}.type
                gson.fromJson(json, type)
            }
        } catch (_: Exception) {
            emptyList()
        }
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
