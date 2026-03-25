package org.openedx.dashboard.data.api

import org.openedx.dashboard.data.model.AchievementDto
import org.openedx.dashboard.data.model.CourseItemDto
import org.openedx.dashboard.data.model.PaginatedDto
import org.openedx.dashboard.data.model.RecommendationDto
import org.openedx.dashboard.data.model.SummaryCardDto
import org.openedx.dashboard.data.model.AchievementsAllDto
import org.openedx.dashboard.data.model.WishlistItemData
import retrofit2.http.GET

interface DashboardApi {

    @GET("/api/v1/dashboard/summary-cards/")
    suspend fun getSummaryCards(): List<SummaryCardDto>

    @GET("/api/v1/dashboard/continue-learning/")
    suspend fun getContinueLearning(): List<CourseItemDto>

    @GET("/api/v1/dashboard/achievements/")
    suspend fun getAchievements(): List<AchievementDto>

    @GET("/api/v1/dashboard/recommended-courses/")
    suspend fun getRecommendedCourses(): List<RecommendationDto>

    @GET("/api/v1/dashboard/wishlist/")
    suspend fun getWishlist(): PaginatedDto<WishlistItemData>

    @GET("/api/v1/dashboard/in-progress-courses/")
    suspend fun getInProgress(): PaginatedDto<CourseItemDto>

    @GET("/api/v1/dashboard/completed-courses/")
    suspend fun getCompleted(): PaginatedDto<CourseItemDto>

    @GET("/api/v1/achievements/all/")
    suspend fun getAllAchievements(): AchievementsAllDto
}
