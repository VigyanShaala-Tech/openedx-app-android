package org.openedx.dashboard.data.repository

import org.openedx.core.data.api.CourseApi
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.CourseEnrollments
import org.openedx.core.domain.model.DashboardCourseList
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.dashboard.data.api.DashboardApi
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.data.DashboardDao
import org.openedx.dashboard.data.model.WishlistRequest
import org.openedx.dashboard.data.model.WishlistResponse
import org.openedx.dashboard.domain.CourseStatusFilter
import org.openedx.foundation.utils.FileUtil

class DashboardRepository(
    private val api: CourseApi,
    private val dashboardApi: DashboardApi,
    private val dao: DashboardDao,
    private val preferencesManager: CorePreferences,
    private val fileUtil: FileUtil,
    private val wishlistApi: org.openedx.dashboard.data.api.WishlistApi,
) {

    suspend fun getEnrolledCourses(page: Int): DashboardCourseList {
        val user = preferencesManager.user
        val result = api.getEnrolledCourses(
            username = user?.username ?: "",
            page = page
        )
        preferencesManager.appConfig = result.configs.mapToDomain()

        if (page == 1) dao.clearCachedData()
        dao.insertEnrolledCourseEntity(
            *result.enrollments.results.map { it.mapToRoomEntity() }
                .toTypedArray()
        )
        return result.enrollments.mapToDomain()
    }

    suspend fun getEnrolledCoursesFromCache(): List<EnrolledCourse> {
        val list = dao.readAllData()
        return list.map { it.mapToDomain() }
    }

    suspend fun getMainUserCourses(pageSize: Int): CourseEnrollments {
        val result = api.getUserCourses(
            username = preferencesManager.user?.username ?: "",
            pageSize = pageSize
        )
        preferencesManager.appConfig = result.configs.mapToDomain()

        fileUtil.saveObjectToFile(result)
        return result.mapToDomain()
    }

    suspend fun getAllUserCourses(page: Int, status: CourseStatusFilter?): DashboardCourseList {
        val user = preferencesManager.user
        val result = api.getUserCourses(
            username = user?.username ?: "",
            page = page,
            status = status?.key,
            fields = listOf("course_progress")
        )
        preferencesManager.appConfig = result.configs.mapToDomain()

        dao.clearCachedData()
        dao.insertEnrolledCourseEntity(
            *result.enrollments.results
                .map { it.mapToRoomEntity() }
                .toTypedArray()
        )
        return result.enrollments.mapToDomain()
    }

    suspend fun getSummaryCards(): List<SummaryCardDto> {
        return dashboardApi.getSummaryCards()
    }

    suspend fun getContinueLearning(): List<CourseItemDto> {
        return dashboardApi.getContinueLearning()
    }

    suspend fun getAchievements(): List<AchievementDto> {
        return dashboardApi.getAchievements()
    }

    suspend fun getRecommended(): List<RecommendationDto> {
        return dashboardApi.getRecommendedCourses()
    }

    suspend fun getWishlist(): PaginatedDto<CourseItemDto> {
        return dashboardApi.getWishlist()
    }

    suspend fun getInProgress(): PaginatedDto<CourseItemDto> {
        return dashboardApi.getInProgress()
    }

    suspend fun getCompleted(): PaginatedDto<CourseItemDto> {
        return dashboardApi.getCompleted()
    }

    suspend fun removeFromWishlist(courseId: String): WishlistResponse {
        return wishlistApi.remove(WishlistRequest(courseId))
    }

    suspend fun addToWishlist(courseId: String): WishlistResponse {
        return wishlistApi.add(WishlistRequest(courseId))
    }
}
