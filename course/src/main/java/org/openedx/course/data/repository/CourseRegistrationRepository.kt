package org.openedx.course.data.repository

import org.openedx.course.data.api.CourseRegistrationApi
import org.openedx.course.data.model.EnrollmentFormResponse

class CourseRegistrationRepository(
    private val api: CourseRegistrationApi
) {
    suspend fun getEnrollmentForm(formId: String): EnrollmentFormResponse {
        return api.getEnrollmentForm(formId)
    }
}
