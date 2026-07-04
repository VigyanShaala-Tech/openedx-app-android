package org.openedx.course.data.api

import org.openedx.course.data.model.EnrollmentFormResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseRegistrationApi {

    @GET("/api/v1/cohort-registration/{form_id}/form/")
    suspend fun getEnrollmentForm(
        @Path("form_id") formId: String
    ): EnrollmentFormResponse
}
