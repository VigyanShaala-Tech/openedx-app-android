package org.openedx.course.data.model

import com.google.gson.annotations.SerializedName

data class University(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class RankingOption(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

data class UserRanking(
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("points")
    val points: Int
)

data class LeaderboardEntry(
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("university")
    val university: String?,
    @SerializedName("points")
    val points: Int
)

data class LeaderboardResponse(
    @SerializedName("results")
    val results: List<LeaderboardEntry>,
    @SerializedName("count")
    val count: Int,
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?
)
