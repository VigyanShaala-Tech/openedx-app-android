package org.openedx.course.data.repository

import com.google.gson.JsonParser
import org.openedx.course.data.api.LeaderboardApi
import org.openedx.course.data.model.LeaderboardResponse
import org.openedx.course.data.model.RankingOption
import org.openedx.course.data.model.University
import org.openedx.course.data.model.UserRanking

class LeaderboardRepository(
    private val api: LeaderboardApi
) {

    suspend fun getUniversities(): List<University> {
        val responseBody = try { api.getUniversities() } catch (_: Exception) { return emptyList() }
        val rawJson = try { responseBody.string() } catch (_: Exception) { "Error reading stream" }
        val json = try { JsonParser.parseString(rawJson) } catch (_: Exception) { null }
        
        if (json == null || (!json.isJsonArray && !json.isJsonObject)) {
            return listOf(University(id = "-1", name = "RAW: ${rawJson.take(200)}"))
        }

        val jsonArray = if (json.isJsonArray) {
            json.asJsonArray
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            val foundArray = obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            val nestedArray = if (foundArray == null) {
                obj.entrySet().firstOrNull { it.value.isJsonObject }?.value?.asJsonObject?.entrySet()?.firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            } else null
            
            foundArray ?: nestedArray ?: when {
                obj.has("results") -> obj.get("results")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("universities") -> obj.get("universities")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("university") -> obj.get("university")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("colleges") -> obj.get("colleges")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("college") -> obj.get("college")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("data") -> obj.get("data")?.let { 
                    if (it.isJsonArray) it.asJsonArray 
                    else if (it.isJsonObject) it.asJsonObject.entrySet().firstOrNull { e -> e.value.isJsonArray }?.value?.asJsonArray
                    else null
                }
                else -> null
            }
        } else {
            null
        }

        val results = jsonArray?.mapNotNull { element ->
            try {
                if (element.isJsonObject) {
                    val obj = element.asJsonObject
                    val id = when {
                        obj.has("id") -> obj.get("id").asString
                        obj.has("university_id") -> obj.get("university_id").asString
                        obj.has("college_id") -> obj.get("college_id").asString
                        else -> null
                    }
                    val name = obj.get("name")?.takeIf { it.isJsonPrimitive }?.asString
                    val university = obj.get("university")?.takeIf { it.isJsonPrimitive }?.asString
                    val universityNameSnake = obj.get("university_name")?.takeIf { it.isJsonPrimitive }?.asString
                    val college = obj.get("college")?.takeIf { it.isJsonPrimitive }?.asString
                    val collegeNameSnake = obj.get("college_name")?.takeIf { it.isJsonPrimitive }?.asString
                    val title = obj.get("title")?.takeIf { it.isJsonPrimitive }?.asString
                    val label = obj.get("label")?.takeIf { it.isJsonPrimitive }?.asString
                    val text = obj.get("text")?.takeIf { it.isJsonPrimitive }?.asString
                    val value = obj.get("value")?.takeIf { it.isJsonPrimitive }?.asString

                    University(
                        id = id ?: name ?: university ?: college ?: value ?: "unknown",
                        name = name,
                        universityName = university,
                        universityNameSnake = universityNameSnake,
                        collegeName = college,
                        collegeNameSnake = collegeNameSnake,
                        title = title,
                        label = label,
                        text = text,
                        value = value
                    )
                } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                    val name = element.asString
                    University(id = name, name = name)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        } ?: emptyList()
        
        return if (results.isEmpty()) {
            listOf(University(id = "-1", name = "No data found. RAW: ${rawJson.take(100)}"))
        } else results
    }

    suspend fun getRankingOptions(): List<RankingOption> {
        val responseBody = try { api.getRankingOptions() } catch (_: Exception) { return emptyList() }
        val rawJson = try { responseBody.string() } catch (_: Exception) { "Error reading stream" }
        val json = try { JsonParser.parseString(rawJson) } catch (_: Exception) { null }
        
        if (json == null || (!json.isJsonArray && !json.isJsonObject)) {
            return emptyList()
        }

        val jsonArray = if (json.isJsonArray) {
            json.asJsonArray
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            val foundArray = obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            foundArray ?: when {
                obj.has("results") -> obj.get("results")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("ranking_options") -> obj.get("ranking_options")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("data") -> obj.get("data")?.takeIf { it.isJsonArray }?.asJsonArray
                else -> null
            }
        } else {
            null
        }

        return jsonArray?.mapNotNull { element ->
            try {
                if (element.isJsonObject) {
                    val obj = element.asJsonObject
                    val id = obj.get("id")?.takeIf { it.isJsonPrimitive }?.asString
                    val value = obj.get("value")?.takeIf { it.isJsonPrimitive }?.asString
                    val label = obj.get("label")?.takeIf { it.isJsonPrimitive }?.asString
                    val name = obj.get("name")?.takeIf { it.isJsonPrimitive }?.asString
                    val displayName = obj.get("display_name")?.takeIf { it.isJsonPrimitive }?.asString
                    
                    RankingOption(
                        id = id ?: value ?: name ?: displayName ?: "unknown",
                        value = value,
                        label = label,
                        name = name,
                        displayName = displayName
                    )
                } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                    val name = element.asString
                    RankingOption(id = name, name = name)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        } ?: emptyList()
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
