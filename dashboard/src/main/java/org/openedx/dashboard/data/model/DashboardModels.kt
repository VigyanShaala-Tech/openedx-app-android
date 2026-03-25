package org.openedx.dashboard.data.model

import com.google.gson.annotations.SerializedName

data class SummaryCardDto(
    val id: Int,
    val icon: String,
    val number: Int,
    val label: String
)

data class CourseItemDto(
    val id: String,
    val title: String,
    val course_image: String?,
    val progress: Int,
    val category: String?,
    val level: String?
)

data class AchievementDto(
    val id: Int,
    val title: String,
    val img: String?
)

data class RecommendationDto(
    val id: String,
    val title: String,
    val description: String?,
    val category: String?,
    val duration: String?,
    val level: String?,
    val image: String?,
    val rating: Int?,
    val reviews: Int?,
    val instructor: String?
)

data class PaginationDto(
    val next: String?,
    val previous: String?,
    val count: Int,
    val num_pages: Int
)

data class PaginatedDto<T>(
    val results: List<T>,
    val pagination: PaginationDto
)

data class StatCardData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val value: String,
    val label: String
)

data class CourseCardData(
    val id: String,
    val title: String,
    val tag: String,
    val imageUrl: String,
    val progress: Int
)

data class WishlistItemData(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("progress")
    val progress: String?,
    @SerializedName("category")
    val category: String,
    @SerializedName("level")
    val level: String,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("reviews")
    val reviews: Int?,
    @SerializedName("instructor")
    val instructor: String,
)

data class AchievementData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class RecommendationData(
    val id: String,
    val title: String,
    val category: String,
    val rating: String,
    val description: String,
    val imageUrl: String
)

data class AchievementsAllDto(
    val stats: List<AchievementStatDto>,
    val earned_badges: List<EarnedBadgeDto>,
    val badges_in_progress: List<BadgeProgressDto>
)

data class AchievementStatDto(
    val id: Int,
    val icon: String,
    val number: Int,
    val label: String
)

data class EarnedBadgeDto(
    val icon_url: String?,
    val title: String,
    val description: String
)

data class BadgeProgressDto(
    val icon_url: String?,
    val title: String,
    val description: String,
    val progress: Int?
)


data class WishlistRequest(val course_id: String)
data class WishlistResponse(val success: Boolean, val message: String?)
