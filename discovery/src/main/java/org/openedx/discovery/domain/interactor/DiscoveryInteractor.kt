package org.openedx.discovery.domain.interactor

import org.openedx.discovery.data.repository.DiscoveryRepository
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.domain.model.CourseList

class DiscoveryInteractor(private val repository: DiscoveryRepository) {

    suspend fun getCourseDetails(id: String) = repository.getCourseDetail(id)

    suspend fun getCourseDetailsFromCache(id: String) = repository.getCourseDetailFromCache(id)

    suspend fun enrollInACourse(id: String) {
        repository.enrollInACourse(courseId = id)
    }

    suspend fun getCourseCurriculum(courseId: String) = repository.getCourseCurriculum(courseId)
    suspend fun getCourseInstructors(courseId: String) = repository.getCourseInstructors(courseId)
    suspend fun getCourseReviews(courseId: String) = repository.getCourseReviews(courseId)

    suspend fun getCoursesList(
        username: String?,
        organization: String?,
        pageNumber: Int
    ): CourseList {
        return repository.getCoursesList(username, organization, pageNumber)
    }

    suspend fun getCoursesListByQuery(
        query: String,
        pageNumber: Int,
    ) = repository.getCoursesListByQuery(query, pageNumber)

    suspend fun getCoursesListFromCache(): List<Course> {
        return repository.getCachedCoursesList()
    }

    suspend fun addToWishlist(courseId: String) = repository.addToWishlist(courseId)
    suspend fun removeFromWishlist(courseId: String) = repository.removeFromWishlist(courseId)
}
