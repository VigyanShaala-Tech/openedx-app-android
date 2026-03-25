package org.openedx.dashboard.domain.interactor

import org.openedx.core.domain.model.DashboardCourseList
import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.data.model.AchievementsAllDto
import org.openedx.dashboard.data.model.WishlistItemData
import org.openedx.dashboard.data.repository.DashboardRepository
import org.openedx.dashboard.domain.CourseStatusFilter

class DashboardInteractor(
    private val repository: DashboardRepository,
) {

    suspend fun getEnrolledCourses(page: Int): DashboardCourseList {
        return repository.getEnrolledCourses(page)
    }

    suspend fun getEnrolledCoursesFromCache() = repository.getEnrolledCoursesFromCache()

    suspend fun getMainUserCourses(pageSize: Int) = repository.getMainUserCourses(pageSize)

    suspend fun getAllUserCourses(
        page: Int = 1,
        status: CourseStatusFilter? = null,
    ): DashboardCourseList {
        return repository.getAllUserCourses(
            page,
            status
        )
    }

    suspend fun getSummaryCards(): List<SummaryCardDto> = repository.getSummaryCards()
    suspend fun getContinueLearning(): List<CourseItemDto> = repository.getContinueLearning()
    suspend fun getAchievements(): List<AchievementDto> = repository.getAchievements()
    suspend fun getRecommended(): List<RecommendationDto> = repository.getRecommended()
    suspend fun getWishlist(): PaginatedDto<WishlistItemData> = repository.getWishlist()
    suspend fun getInProgress(): PaginatedDto<CourseItemDto> = repository.getInProgress()
    suspend fun getCompleted(): PaginatedDto<CourseItemDto> = repository.getCompleted()
    suspend fun getAllAchievements(): AchievementsAllDto = repository.getAllAchievements()

    suspend fun addToWishlist(courseId: String) = repository.addToWishlist(courseId)
    suspend fun removeFromWishlist(courseId: String) = repository.removeFromWishlist(courseId)
}
