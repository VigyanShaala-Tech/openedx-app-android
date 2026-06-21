package org.openedx.course.data.api

import org.openedx.core.ApiConstants
import org.openedx.course.data.model.LeaderboardResponse
import org.openedx.course.data.model.RankingOption
import org.openedx.course.data.model.University
import org.openedx.course.data.model.UserRanking
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeaderboardApi {

    @GET(ApiConstants.URL_UNIVERSITIES)
    suspend fun getUniversities(): List<University>

    @GET(ApiConstants.URL_RANKING_OPTIONS)
    suspend fun getRankingOptions(): List<RankingOption>

    @GET(ApiConstants.URL_COURSE_USER_RANKING)
    suspend fun getUserRanking(@Path("course_id") courseId: String): UserRanking

    @GET(ApiConstants.URL_COURSE_LEADERBOARD)
    suspend fun getLeaderboard(
        @Path("course_id") courseId: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("range_type") rangeType: String,
        @Query("university") university: String? = null
    ): LeaderboardResponse
}
