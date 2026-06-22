package org.openedx.core.data.model

import com.google.gson.annotations.SerializedName
import org.openedx.core.domain.model.DashboardProgress

data class DashboardProgressResponse(
    @SerializedName("result") val result: DashboardProgressResult?
) {
    data class DashboardProgressResult(
        @SerializedName("summaryCards") val summaryCards: List<SummaryCard>?,
        @SerializedName("charts") val charts: DashboardCharts?
    )

    data class SummaryCard(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("value") val value: String?,
        @SerializedName("icon") val icon: String?,
        @SerializedName("color") val color: String?
    )

    data class DashboardCharts(
        @SerializedName("quizScores") val quizScores: QuizScores?,
        @SerializedName("assignmentScores") val assignmentScores: AssignmentScores?,
        @SerializedName("overallPerformance") val overallPerformance: OverallPerformance?
    )

    data class QuizScores(
        @SerializedName("data") val data: List<QuizScoreData>?
    )

    data class QuizScoreData(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("score") val score: Double?,
        @SerializedName("max") val max: Double?
    )

    data class AssignmentScores(
        @SerializedName("data") val data: List<AssignmentScoreData>?
    )

    data class AssignmentScoreData(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("score") val score: Double?,
        @SerializedName("max") val max: Double?
    )

    data class OverallPerformance(
        @SerializedName("radialData") val radialData: List<RadialData>?,
        @SerializedName("progressBars") val progressBars: List<ProgressBarData>?,
        @SerializedName("encouragementMessage") val encouragementMessage: String?
    )

    data class RadialData(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("value") val value: Double?
    )

    data class ProgressBarData(
        @SerializedName("id") val id: Int?,
        @SerializedName("name") val name: String?,
        @SerializedName("value") val value: Double?
    )

    fun mapToDomain(): DashboardProgress {
        return DashboardProgress(
            summaryCards = result?.summaryCards?.map { card ->
                DashboardProgress.SummaryCard(
                    id = card.id ?: 0,
                    name = card.name ?: "",
                    value = card.value ?: "",
                    icon = card.icon ?: "",
                    color = card.color ?: "#000000"
                )
            } ?: emptyList(),
            quizScores = result?.charts?.quizScores?.data?.map { quiz ->
                DashboardProgress.QuizScoreData(
                    id = quiz.id ?: 0,
                    name = quiz.name ?: "",
                    score = quiz.score ?: 0.0,
                    max = quiz.max ?: 100.0
                )
            } ?: emptyList(),
            assignmentScores = result?.charts?.assignmentScores?.data?.map { assignment ->
                DashboardProgress.AssignmentScoreData(
                    id = assignment.id ?: 0,
                    name = assignment.name ?: "",
                    score = assignment.score ?: 0.0,
                    max = assignment.max ?: 100.0
                )
            } ?: emptyList(),
            overallPerformance = DashboardProgress.OverallPerformance(
                radialData = result?.charts?.overallPerformance?.radialData?.map { radial ->
                    DashboardProgress.RadialData(
                        id = radial.id ?: 0,
                        name = radial.name ?: "",
                        value = radial.value ?: 0.0
                    )
                } ?: emptyList(),
                progressBars = result?.charts?.overallPerformance?.progressBars?.map { progress ->
                    DashboardProgress.ProgressBarData(
                        id = progress.id ?: 0,
                        name = progress.name ?: "",
                        value = progress.value ?: 0.0
                    )
                } ?: emptyList(),
                encouragementMessage = result?.charts?.overallPerformance?.encouragementMessage ?: ""
            )
        )
    }
}
