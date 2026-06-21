package org.openedx.course.data.repository

import org.openedx.course.data.api.LeaderboardApi
import org.openedx.course.data.model.LeaderboardResponse
import org.openedx.course.data.model.RankingOption
import org.openedx.course.data.model.University
import org.openedx.course.data.model.UserRanking

class LeaderboardRepository(
    private val api: LeaderboardApi
) {

    suspend fun getUniversities(): List<University> {
        return api.getUniversities()
    }

    suspend fun getRankingOptions(): List<RankingOption> {
        return api.getRankingOptions()
    }

    suspend fun getUserRanking(courseId: String): UserRanking {
        return api.getUserRanking(courseId)
    }

    suspend fun getLeaderboard(
        courseId: String,
        page: Int,
        pageSize: Int,
        rangeType: String,
        university: String?
    ): LeaderboardResponse {
        return api.getLeaderboard(courseId, page, pageSize, rangeType, university)
    }
}
