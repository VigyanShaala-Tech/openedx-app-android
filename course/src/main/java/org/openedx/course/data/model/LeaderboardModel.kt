package org.openedx.course.data.model

import com.google.gson.annotations.SerializedName

data class University(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("university")
    val universityName: String? = null,
    @SerializedName("university_name")
    val universityNameSnake: String? = null,
    @SerializedName("college")
    val collegeName: String? = null,
    @SerializedName("college_name")
    val collegeNameSnake: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("value")
    val value: String? = null
)

data class RankingOption(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("value")
    val value: String? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("display_name")
    val displayName: String? = null
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
    val username: String? = null,
    @SerializedName("user")
    val name: String,
    @SerializedName("college")
    val university: String?,
    @SerializedName("points")
    val points: Int
)

data class LeaderboardResponse(
    @SerializedName("results")
    val results: List<LeaderboardEntry>,
    @SerializedName("pagination")
    val pagination: LeaderboardPagination? = null
)

data class LeaderboardPagination(
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("num_pages")
    val numPages: Int
)
