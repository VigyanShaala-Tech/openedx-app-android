package org.openedx.course.domain.interactor

import org.openedx.core.data.model.RegistrationSubmitResponse
import org.openedx.core.domain.model.EligibilityResult
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.course.data.repository.CourseRepository
import java.io.File

class CourseRegistrationInteractor(
    private val repository: CourseRepository
) {
    suspend fun getEnrollmentForm(formId: String): EnrollmentForm {
        return repository.getEnrollmentForm(formId)
    }

    suspend fun getPrefillData(formId: String, body: Map<String, String>): Map<String, Any> {
        return repository.getPrefillData(formId, body)
    }

    suspend fun checkEligibility(formId: String, body: Map<String, String>): EligibilityResult {
        return repository.checkEligibility(formId, body)
    }

    suspend fun submitRegistration(formId: String, body: Map<String, Any>): RegistrationSubmitResponse {
        return repository.submitRegistration(formId, body)
    }

    suspend fun uploadFile(formId: String, fieldKey: String, courseId: String, email: String, file: File) {
        repository.uploadFile(formId, fieldKey, courseId, email, file)
    }
}
