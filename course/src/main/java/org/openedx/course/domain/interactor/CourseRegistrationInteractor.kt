package org.openedx.course.domain.interactor

import org.openedx.course.data.model.EnrollmentFormResponse
import org.openedx.course.data.repository.CourseRegistrationRepository

class CourseRegistrationInteractor(
    private val repository: CourseRegistrationRepository
) {
    suspend fun getEnrollmentForm(formId: String): EnrollmentFormResponse {
        return repository.getEnrollmentForm(formId)
    }
}
