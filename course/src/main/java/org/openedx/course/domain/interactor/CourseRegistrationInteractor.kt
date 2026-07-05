package org.openedx.course.domain.interactor

import org.openedx.core.domain.model.EligibilityResult
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.course.data.repository.CourseRepository

class CourseRegistrationInteractor(
    private val repository: CourseRepository
) {
    suspend fun getEnrollmentForm(formId: String): EnrollmentForm {
        return repository.getEnrollmentForm(formId)
    }

    suspend fun checkEligibility(formId: String, body: Map<String, String>): EligibilityResult {
        return repository.checkEligibility(formId, body)
    }
}
