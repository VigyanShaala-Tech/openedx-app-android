package org.openedx.dashboard.data.model

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
