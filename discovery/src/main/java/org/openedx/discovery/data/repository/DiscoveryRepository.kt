package org.openedx.discovery.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.ResponseBody
import org.openedx.core.data.model.EnrollBody
import org.openedx.core.data.storage.CorePreferences
import org.openedx.discovery.data.api.DiscoveryApi
import org.openedx.discovery.data.model.CourseDetails
import org.openedx.discovery.data.model.room.CourseEntity
import org.openedx.discovery.data.storage.DiscoveryDao
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.domain.model.CourseList

class DiscoveryRepository(
    private val api: DiscoveryApi,
    private val dao: DiscoveryDao,
    private val preferencesManager: CorePreferences,
    private val wishlistApi: org.openedx.discovery.data.api.WishlistApi,
) {

    suspend fun getCourseDetail(id: String): Course {
        val course = api.getCourseDetail(id, preferencesManager.user?.username)
        dao.updateCourseEntity(CourseEntity.createFrom(course))
        return course.mapToDomain()
    }

    suspend fun getCourseCurriculum(courseId: String): Map<String, List<String>> {
        return api.getCourseCurriculum(courseId)
    }

    suspend fun getCourseInstructors(courseId: String): List<org.openedx.discovery.domain.model.Instructor> {
        return api.getCourseInstructors(courseId).map { it.mapToDomain() }
    }

    suspend fun getCourseReviews(courseId: String): List<org.openedx.discovery.domain.model.Review> {
        return api.getCourseReviews(courseId).map { it.mapToDomain() }
    }

    suspend fun getCourseDetailFromCache(id: String): Course? {
        return dao.getCourseById(id)?.mapToDomain()
    }

    suspend fun enrollInACourse(courseId: String): ResponseBody {
        val enrollBody = EnrollBody(
            EnrollBody.CourseDetails(
                courseId = courseId,
                emailOptIn = preferencesManager.user?.email
            )
        )
        return api.enrollInACourse(enrollBody)
    }

    suspend fun getCoursesList(
        username: String?,
        organization: String?,
        pageNumber: Int,
    ): CourseList {
        val pageResponse = api.getCourseList(
            page = pageNumber,
            mobile = false,
            mobileSearch = false,
            username = username,
            org = organization
        )
        val results = pageResponse.results ?: emptyList()
        val enrichedResults = enrichMissingFields(results)
        if (pageNumber == 1) dao.clearCachedData()
        val cachedDataList = enrichedResults.map { CourseEntity.createFrom(it) }
        dao.insertCourseEntity(*cachedDataList.toTypedArray())
        return CourseList(
            pageResponse.pagination.mapToDomain(),
            enrichedResults.map { it.mapToDomain() }
        )
    }

    suspend fun getCachedCoursesList(): List<Course> {
        val dataFromDb = dao.readAllData()
        return dataFromDb.map { it.mapToDomain() }
    }

    suspend fun getCoursesListByQuery(
        query: String,
        pageNumber: Int,
    ): CourseList {
        val pageResponse = api.getCourseList(
            searchQuery = query,
            page = pageNumber,
            mobile = true,
            mobileSearch = true
        )
        val results = pageResponse.results ?: emptyList()
        val enrichedResults = enrichMissingFields(results)
        return CourseList(
            pageResponse.pagination.mapToDomain(),
            enrichedResults.map { it.mapToDomain() }
        )
    }

    suspend fun addToWishlist(courseId: String): org.openedx.discovery.data.api.WishlistResponse {
        return wishlistApi.add(org.openedx.discovery.data.api.WishlistRequest(courseId))
    }

    suspend fun removeFromWishlist(courseId: String): org.openedx.discovery.data.api.WishlistResponse {
        return wishlistApi.remove(org.openedx.discovery.data.api.WishlistRequest(courseId))
    }

    private suspend fun enrichMissingFields(results: List<CourseDetails>): List<CourseDetails> = coroutineScope {
        val tasks = results.map { item ->
            async {
                val needsEnrichment =
                    item.level.isNullOrEmpty() || item.category.isNullOrEmpty() || item.instructorName.isNullOrEmpty()
                if (!needsEnrichment) {
                    item
                } else {
                    val courseKey = item.courseId ?: item.id.orEmpty()
                    if (courseKey.isEmpty()) {
                        item
                    } else {
                        runCatching {
                            api.getCourseDetail(courseKey, preferencesManager.user?.username)
                        }.getOrNull()?.let { detail ->
                            item.copy(
                                instructorName = detail.instructorName ?: item.instructorName,
                                category = detail.category ?: item.category,
                                level = detail.level ?: item.level,
                                rating = detail.rating ?: item.rating,
                                noOfReviews = detail.noOfReviews ?: item.noOfReviews,
                                enrollments = detail.enrollments ?: item.enrollments,
                                isWishlisted = detail.isWishlisted ?: item.isWishlisted,
                                overview = detail.overview ?: item.overview
                            )
                        } ?: item
                    }
                }
            }
        }
        tasks.map { it.await() }
    }
}
