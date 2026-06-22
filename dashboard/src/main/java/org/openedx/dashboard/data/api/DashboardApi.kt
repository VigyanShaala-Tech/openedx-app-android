package org.openedx.dashboard.data.api

import org.openedx.dashboard.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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

    @POST("/api/v1/get/my/notifications/")
    suspend fun getNotifications(@Body request: NotificationRequest): NotificationResponse
}
