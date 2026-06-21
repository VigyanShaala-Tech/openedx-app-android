package org.openedx.course.data.api

import okhttp3.ResponseBody
import org.openedx.core.ApiConstants
import org.openedx.course.data.model.LeaderboardResponse
import org.openedx.course.data.model.UserRankingResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeaderboardApi {

    @GET("https://uat.vigyanshaala.com/api/v1/options/universities/")
    suspend fun getUniversities(): ResponseBody

    @GET("https://uat.vigyanshaala.com/api/v1/options/rankings/")
    suspend fun getRankingOptions(): ResponseBody

    @GET(ApiConstants.URL_COURSE_USER_RANKING)
    suspend fun getUserRanking(@Path("course_id") courseId: String): UserRankingResponse

    @GET(ApiConstants.URL_COURSE_LEADERBOARD)
    suspend fun getLeaderboard(
        @Path("course_id") courseId: String,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("range_type") rangeType: String,
        @Query("university") university: String? = null
    ): LeaderboardResponse
}
