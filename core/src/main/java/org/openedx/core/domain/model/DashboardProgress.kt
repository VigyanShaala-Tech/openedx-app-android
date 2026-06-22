package org.openedx.core.domain.model

data class DashboardProgress(
    val summaryCards: List<SummaryCard>,
    val quizScores: List<QuizScoreData>,
    val assignmentScores: List<AssignmentScoreData>,
    val overallPerformance: OverallPerformance
) {
    data class SummaryCard(
        val id: Int,
        val name: String,
        val value: String,
        val icon: String,
        val color: String
    )

    data class QuizScoreData(
        val id: Int,
        val name: String,
        val score: Double,
        val max: Double
    )

    data class AssignmentScoreData(
        val id: Int,
        val name: String,
        val score: Double,
        val max: Double
    )

    data class OverallPerformance(
        val radialData: List<RadialData>,
        val progressBars: List<ProgressBarData>,
        val encouragementMessage: String
    )

    data class RadialData(
        val id: Int,
        val name: String,
        val value: Double
    )

    data class ProgressBarData(
        val id: Int,
        val name: String,
        val value: Double
    )
}
