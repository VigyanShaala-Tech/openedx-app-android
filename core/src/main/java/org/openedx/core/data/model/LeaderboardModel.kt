package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.domain.model.LeaderboardEntry as DomainLeaderboardEntry
import org.openedx.core.domain.model.LeaderboardList as DomainLeaderboardList
import org.openedx.core.domain.model.LeaderboardPagination as DomainLeaderboardPagination
import org.openedx.core.domain.model.RankingOption as DomainRankingOption
import org.openedx.core.domain.model.University as DomainUniversity
import org.openedx.core.domain.model.UserRanking as DomainUserRanking

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
) {
    fun mapToDomain(): DomainUniversity {
        return DomainUniversity(
            id = id ?: name ?: universityName ?: collegeName ?: value ?: "unknown",
            name = name ?: universityName ?: collegeName ?: title ?: label ?: text ?: value ?: ""
        )
    }
}

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
) {
    fun mapToDomain(): DomainRankingOption {
        return DomainRankingOption(
            id = id ?: value ?: name ?: "unknown",
            label = label ?: displayName ?: name ?: ""
        )
    }
}

data class UserRankingResponse(
    @SerializedName("result")
    val result: UserRanking
)

data class UserRanking(
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("points")
    val points: Int
) {
    fun mapToDomain(): DomainUserRanking {
        return DomainUserRanking(
            rank = rank,
            points = points
        )
    }
}

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
) {
    fun mapToDomain(): DomainLeaderboardEntry {
        return DomainLeaderboardEntry(
            rank = rank,
            username = username ?: "",
            name = name,
            university = university ?: "",
            points = points
        )
    }
}

data class LeaderboardResponse(
    @SerializedName("results")
    val results: List<LeaderboardEntry>,
    @SerializedName("pagination")
    val pagination: LeaderboardPagination? = null
) {
    fun mapToDomain(): DomainLeaderboardList {
        return DomainLeaderboardList(
            results = results.map { it.mapToDomain() },
            pagination = pagination?.mapToDomain()
        )
    }
}

data class LeaderboardPagination(
    @SerializedName("next")
    val next: String?,
    @SerializedName("previous")
    val previous: String?,
    @SerializedName("count")
    val count: Int,
    @SerializedName("num_pages")
    val numPages: Int
) {
    fun mapToDomain(): DomainLeaderboardPagination {
        return DomainLeaderboardPagination(
            count = count,
            numPages = numPages
        )
    }
}
