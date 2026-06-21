package org.openedx.course.domain.interactor

import org.openedx.course.data.repository.LeaderboardRepository

class LeaderboardInteractor(
    private val repository: LeaderboardRepository
) {

    suspend fun getUniversities() = repository.getUniversities()

    suspend fun getRankingOptions() = repository.getRankingOptions()

    suspend fun getUserRanking(courseId: String) = repository.getUserRanking(courseId)

    suspend fun getLeaderboard(
        courseId: String,
        page: Int,
        pageSize: Int,
        rangeType: String,
        university: String?
    ) = repository.getLeaderboard(courseId, page, pageSize, rangeType, university)
}
