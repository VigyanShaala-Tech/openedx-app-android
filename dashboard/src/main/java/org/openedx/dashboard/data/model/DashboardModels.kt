package org.openedx.dashboard.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SummaryCardDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("number")
    val number: Int? = null,
    @SerializedName("label")
    val label: String? = null
) {
    companion object {
        fun from(item: Any?): SummaryCardDto? {
            if (item == null) return null
            if (item is SummaryCardDto) return item
            if (item is Map<*, *>) {
                return SummaryCardDto(
                    id = (item["id"] as? Number)?.toInt() ?: item["id"]?.toString()?.toIntOrNull(),
                    icon = item["icon"]?.toString(),
                    number = (item["number"] as? Number)?.toInt() ?: item["number"]?.toString()?.toIntOrNull(),
                    label = item["label"]?.toString()
                )
            }
            return null
        }
    }
}

@Keep
data class CourseItemDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("course_image")
    val course_image: String? = null,
    @SerializedName("progress")
    val progress: Int? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("level")
    val level: String? = null
) {
    companion object {
        fun from(item: Any?): CourseItemDto? {
            if (item == null) return null
            if (item is CourseItemDto) return item
            if (item is Map<*, *>) {
                val progressInt = when (val p = item["progress"]) {
                    is Number -> p.toInt()
                    is String -> p.toIntOrNull()
                    else -> null
                }
                return CourseItemDto(
                    id = item["id"]?.toString(),
                    title = item["title"]?.toString(),
                    course_image = item["course_image"]?.toString(),
                    progress = progressInt,
                    category = item["category"]?.toString(),
                    level = item["level"]?.toString()
                )
            }
            return null
        }
    }
}

@Keep
data class AchievementDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("img")
    val img: String? = null
) {
    companion object {
        fun from(item: Any?): AchievementDto? {
            if (item == null) return null
            if (item is AchievementDto) return item
            if (item is Map<*, *>) {
                return AchievementDto(
                    id = (item["id"] as? Number)?.toInt() ?: item["id"]?.toString()?.toIntOrNull(),
                    title = item["title"]?.toString(),
                    img = item["img"]?.toString()
                )
            }
            return null
        }
    }
}

@Keep
data class RecommendationDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("rating")
    val rating: Double? = null,
    @SerializedName("reviews")
    val reviews: Int? = null,
    @SerializedName("instructor")
    val instructor: String? = null
) {
    companion object {
        fun from(item: Any?): RecommendationDto? {
            if (item == null) return null
            if (item is RecommendationDto) return item
            if (item is Map<*, *>) {
                return RecommendationDto(
                    id = item["id"]?.toString(),
                    title = item["title"]?.toString(),
                    description = item["description"]?.toString(),
                    category = item["category"]?.toString(),
                    duration = item["duration"]?.toString(),
                    level = item["level"]?.toString(),
                    image = item["image"]?.toString(),
                    rating = (item["rating"] as? Number)?.toDouble() ?: item["rating"]?.toString()?.toDoubleOrNull(),
                    reviews = (item["reviews"] as? Number)?.toInt() ?: item["reviews"]?.toString()?.toIntOrNull(),
                    instructor = item["instructor"]?.toString()
                )
            }
            return null
        }
    }
}

@Keep
data class PaginationDto(
    @SerializedName("next")
    val next: String? = null,
    @SerializedName("previous")
    val previous: String? = null,
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("num_pages")
    val num_pages: Int? = null
)

@Keep
data class PaginatedDto<T>(
    @SerializedName("results")
    val results: List<T> = emptyList(),
    @SerializedName("pagination")
    val pagination: PaginationDto = PaginationDto()
)

data class StatCardData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val value: String?,
    val label: String?,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Black,
    val iconName: String? = null
)

data class CourseCardData(
    val id: String?,
    val title: String?,
    val tag: String?,
    val imageUrl: String?,
    val progress: Int?
)

@Keep
data class WishlistItemData(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("progress")
    val progress: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("rating")
    val rating: Float? = null,
    @SerializedName("reviews")
    val reviews: Int? = null,
    @SerializedName("instructor")
    val instructor: String? = null,
) {
    companion object {
        fun from(item: Any?): WishlistItemData? {
            if (item == null) return null
            if (item is WishlistItemData) return item
            if (item is Map<*, *>) {
                return WishlistItemData(
                    id = item["id"]?.toString(),
                    title = item["title"]?.toString(),
                    description = item["description"]?.toString(),
                    image = item["image"]?.toString(),
                    duration = item["duration"]?.toString(),
                    progress = item["progress"]?.toString(),
                    category = item["category"]?.toString(),
                    level = item["level"]?.toString(),
                    rating = (item["rating"] as? Number)?.toFloat() ?: item["rating"]?.toString()?.toFloatOrNull(),
                    reviews = (item["reviews"] as? Number)?.toInt() ?: item["reviews"]?.toString()?.toIntOrNull(),
                    instructor = item["instructor"]?.toString()
                )
            }
            return null
        }
    }
}

data class AchievementData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class RecommendationData(
    val id: String?,
    val title: String?,
    val category: String?,
    val rating: String?,
    val description: String?,
    val imageUrl: String?
)

@Keep
data class AchievementsAllDto(
    @SerializedName("stats")
    val stats: List<AchievementStatDto> = emptyList(),
    @SerializedName("earned_badges")
    val earned_badges: List<EarnedBadgeDto> = emptyList(),
    @SerializedName("badges_in_progress")
    val badges_in_progress: List<BadgeProgressDto> = emptyList()
)

@Keep
data class AchievementStatDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("number")
    val number: Int? = null,
    @SerializedName("label")
    val label: String? = null
)

@Keep
data class EarnedBadgeDto(
    @SerializedName("icon_url")
    val icon_url: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null
)

@Keep
data class BadgeProgressDto(
    @SerializedName("icon_url")
    val icon_url: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("progress")
    val progress: Int? = null
)

@Keep
data class WishlistRequest(
    @SerializedName("course_id")
    val course_id: String
)

@Keep
data class WishlistResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)

@Keep
data class NotificationDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("is_read")
    val is_read: Boolean? = null,
    @SerializedName("created_at")
    val created_at: String? = null
)

@Keep
data class NotificationResponse(
    @SerializedName("haveNewNotification")
    val haveNewNotification: Boolean = false,
    @SerializedName("notifications")
    val notifications: List<NotificationDto> = emptyList()
)

@Keep
data class NotificationRequest(
    @SerializedName("checkedoutnewNotification")
    val checkedoutnewNotification: Boolean
)
