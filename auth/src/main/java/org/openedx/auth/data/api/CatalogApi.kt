package org.openedx.auth.data.api

import retrofit2.http.GET
import retrofit2.http.Query

data class CatalogFiltersResponse(
    val categories: List<String>,
    val subjects: List<String>,
    val levels: List<String>
)

data class CatalogImage(
    val raw: String?,
    val small: String?,
    val large: String?
)

data class CatalogMedia(
    val banner_image: Map<String, String>?,
    val course_image: Map<String, String>?,
    val course_video: Map<String, String>?,
    val image: CatalogImage?
)

data class CatalogCourse(
    val id: String,
    val name: String,
    val org: String?,
    val short_description: String?,
    val media: CatalogMedia?,
    val start: String?,
    val end: String?,
    val start_display: String?,
    val start_type: String?,
    val mobile_available: Boolean?,
    val hidden: Boolean?,
    val invitation_only: Boolean?,
    val category: String?,
    val subjects: List<String>?,
    val level: String?,
    val rating: Double?,
    val no_of_reviews: Int?,
    val instructor_name: String?,
    val enrollments: Int?,
    val course_id: String?
)

data class CatalogPagination(
    val next: String?,
    val previous: String?,
    val count: Int,
    val num_pages: Int
)

data class CatalogCourseList(
    val results: List<CatalogCourse>,
    val pagination: CatalogPagination
)

interface CatalogApi {
    @GET("/api/v1/catalog/filters/")
    suspend fun getFilters(): CatalogFiltersResponse

    @GET("/api/v1/catalog/courses/")
    suspend fun getCourses(
        @Query("search_term") searchTerm: String? = null,
        @Query("category") category: String? = null,
        @Query("level") level: String? = null,
        @Query("subject") subject: String? = null
    ): CatalogCourseList
}
