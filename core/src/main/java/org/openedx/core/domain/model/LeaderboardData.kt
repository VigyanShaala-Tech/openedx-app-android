package org.openedx.core.domain.model

data class University(
    val id: String,
    val name: String
)

data class RankingOption(
    val id: String,
    val label: String
)

data class UserRanking(
    val rank: Int,
    val points: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val name: String,
    val university: String,
    val points: Int
)

data class LeaderboardList(
    val results: List<LeaderboardEntry>,
    val pagination: LeaderboardPagination?
)

data class LeaderboardPagination(
    val count: Int,
    val numPages: Int
)
