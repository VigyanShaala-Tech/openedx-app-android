package org.openedx.course.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.openedx.core.ApiConstants
import org.openedx.core.data.api.CourseApi
import org.openedx.core.data.model.BlocksCompletionBody
import org.openedx.core.data.model.room.CourseProgressEntity
import org.openedx.core.data.model.room.OfflineXBlockProgress
import org.openedx.core.data.model.room.VideoProgressEntity
import org.openedx.core.data.model.room.XBlockProgressData
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.data.storage.CourseDao
import org.openedx.core.domain.model.CourseComponentStatus
import org.openedx.core.domain.model.CourseDatesBannerInfo
import org.openedx.core.domain.model.CourseDatesResult
import org.openedx.core.domain.model.CourseEnrollmentDetails
import org.openedx.core.domain.model.CourseProgress
import org.openedx.core.domain.model.DashboardProgress
import org.openedx.core.data.model.OngoingSessionResponse
import org.openedx.core.data.model.RegistrationSubmitResponse
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.domain.model.EligibilityResult
import org.openedx.core.domain.model.EnrollmentForm
import org.openedx.core.domain.model.LeaderboardList
import org.openedx.core.domain.model.NotificationListResponse
import org.openedx.core.domain.model.RankingOption
import org.openedx.core.domain.model.University
import org.openedx.core.domain.model.UserRanking
import org.openedx.core.exception.NoCachedDataException
import org.openedx.core.extension.channelFlowWithAwait
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.system.connection.NetworkConnection
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class CourseRepository(
    private val api: CourseApi,
    private val courseDao: CourseDao,
    private val downloadDao: DownloadDao,
    private val preferencesManager: CorePreferences,
    private val networkConnection: NetworkConnection,
) {
    private val courseStructure = mutableMapOf<String, CourseStructure>()

    private val courseStatusMap = mutableMapOf<String, CourseComponentStatus>()
    private val courseDatesMap = mutableMapOf<String, CourseDatesResult>()

    suspend fun removeDownloadModel(id: String) {
        downloadDao.removeDownloadModel(id)
    }

    fun getDownloadModels() = downloadDao.getAllDataFlow().map { list ->
        list.map { it.mapToDomain() }
    }

    suspend fun getAllDownloadModels() = downloadDao.readAllData().map { it.mapToDomain() }

    suspend fun getCourseStructureFlow(
        courseId: String,
        forceRefresh: Boolean = true
    ): Flow<CourseStructure> =
        channelFlowWithAwait {
            var hasCourseStructure = false
            val cachedCourseStructure = courseStructure[courseId] ?: (
                    courseDao.getCourseStructureById(courseId)?.mapToDomain()
                    )
            if (cachedCourseStructure != null) {
                hasCourseStructure = true
                trySend(cachedCourseStructure)
            }
            val fetchRemoteCourse = !hasCourseStructure || forceRefresh
            if (networkConnection.isOnline() && fetchRemoteCourse) {
                val response = api.getCourseStructure(
                    "stale-if-error=0",
                    "v4",
                    preferencesManager.user?.username,
                    courseId
                )
                courseDao.insertCourseStructureEntity(response.mapToRoomEntity())
                val courseDomainModel = response.mapToDomain()
                courseStructure[courseId] = courseDomainModel
                trySend(courseDomainModel)
                hasCourseStructure = true
            }
            if (!hasCourseStructure) {
                throw NoCachedDataException()
            }
        }

    suspend fun getCourseStructureFromCache(courseId: String): CourseStructure {
        val cachedCourseStructure = courseDao.getCourseStructureById(courseId)
        if (cachedCourseStructure != null) {
            return cachedCourseStructure.mapToDomain()
        } else {
            throw NoCachedDataException()
        }
    }

    suspend fun getCourseStructure(courseId: String, isNeedRefresh: Boolean): CourseStructure {
        if (!isNeedRefresh) courseStructure[courseId]?.let { return it }

        if (networkConnection.isOnline()) {
            val response = api.getCourseStructure(
                "stale-if-error=0",
                "v4",
                preferencesManager.user?.username,
                courseId
            )
            courseDao.insertCourseStructureEntity(response.mapToRoomEntity())
            courseStructure[courseId] = response.mapToDomain()
        } else {
            val cachedCourseStructure = courseDao.getCourseStructureById(courseId)
            if (cachedCourseStructure != null) {
                courseStructure[courseId] = cachedCourseStructure.mapToDomain()
            } else {
                throw NoCachedDataException()
            }
        }

        return courseStructure[courseId]!!
    }

    suspend fun getEnrollmentDetailsFlow(courseId: String): Flow<CourseEnrollmentDetails> =
        channelFlowWithAwait {
            getCourseEnrollmentDetailsFromCache(courseId)?.let {
                trySend(it)
            }
            val details = getEnrollmentDetails(courseId)
            courseDao.insertCourseEnrollmentDetailsEntity(details.mapToEntity())
            trySend(details)
        }

    private suspend fun getCourseEnrollmentDetailsFromCache(courseId: String): CourseEnrollmentDetails? {
        return courseDao.getCourseEnrollmentDetailsById(id = courseId)
            ?.mapToDomain()
    }

    suspend fun getEnrollmentDetails(courseId: String): CourseEnrollmentDetails {
        return api.getEnrollmentDetails(courseId = courseId).mapToDomain()
    }

    suspend fun getCourseStatusFlow(courseId: String): Flow<CourseComponentStatus> =
        channelFlowWithAwait {
            val localStatus = courseStatusMap[courseId]
            localStatus?.let { trySend(it) }

            if (networkConnection.isOnline()) {
                val username = preferencesManager.user?.username ?: ""
                val status = api.getCourseStatus(username, courseId).mapToDomain()
                courseStatusMap[courseId] = status
                trySend(status)
            } else {
                val status = localStatus ?: CourseComponentStatus("")
                trySend(status)
            }
        }

    suspend fun getCourseStatus(courseId: String): CourseComponentStatus {
        val username = preferencesManager.user?.username ?: ""
        return api.getCourseStatus(username, courseId).mapToDomain()
    }

    suspend fun markBlocksCompletion(courseId: String, blocksId: List<String>) {
        val username = preferencesManager.user?.username ?: ""
        val blocksCompletionBody = BlocksCompletionBody(
            username,
            courseId,
            blocksId.associateWith { "1" }.toMap()
        )
        return api.markBlocksCompletion(blocksCompletionBody)
    }

    suspend fun getCourseDatesFlow(courseId: String): Flow<CourseDatesResult> =
        channelFlowWithAwait {
            val localDates = courseDatesMap[courseId]
            localDates?.let { trySend(it) }

            if (networkConnection.isOnline()) {
                val datesResult = api.getCourseDates(courseId).getCourseDatesResult()
                courseDatesMap[courseId] = datesResult
                trySend(datesResult)
            } else {
                val datesResult = localDates ?: CourseDatesResult(
                    datesSection = linkedMapOf(),
                    courseBanner = CourseDatesBannerInfo(
                        missedDeadlines = false,
                        missedGatedContent = false,
                        verifiedUpgradeLink = "",
                        contentTypeGatingEnabled = false,
                        hasEnded = false
                    )
                )
                trySend(datesResult)
            }
        }

    suspend fun getCourseDates(courseId: String) =
        api.getCourseDates(courseId).getCourseDatesResult()

    suspend fun resetCourseDates(courseId: String) =
        api.resetCourseDates(mapOf(ApiConstants.COURSE_KEY to courseId)).mapToDomain()

    suspend fun getDatesBannerInfo(courseId: String) =
        api.getDatesBannerInfo(courseId).mapToDomain()

    suspend fun getHandouts(courseId: String) = api.getHandouts(courseId).mapToDomain()

    suspend fun getAnnouncements(courseId: String) =
        api.getAnnouncements(courseId).map { it.mapToDomain() }

    suspend fun saveOfflineXBlockProgress(blockId: String, courseId: String, jsonProgress: String) {
        val offlineXBlockProgress = OfflineXBlockProgress(
            blockId = blockId,
            courseId = courseId,
            jsonProgress = XBlockProgressData.parseJson(jsonProgress)
        )
        downloadDao.insertOfflineXBlockProgress(offlineXBlockProgress)
    }

    suspend fun getXBlockProgress(blockId: String) = downloadDao.getOfflineXBlockProgress(blockId)

    suspend fun submitAllOfflineXBlockProgress() {
        val allOfflineXBlockProgress = downloadDao.getAllOfflineXBlockProgress()
        allOfflineXBlockProgress.forEach {
            submitOfflineXBlockProgress(it.blockId, it.courseId, it.jsonProgress.data)
        }
    }

    suspend fun submitOfflineXBlockProgress(blockId: String, courseId: String) {
        val jsonProgressData = getXBlockProgress(blockId)?.jsonProgress?.data
        submitOfflineXBlockProgress(blockId, courseId, jsonProgressData)
    }

    private suspend fun submitOfflineXBlockProgress(
        blockId: String,
        courseId: String,
        jsonProgressData: String?
    ) {
        if (!jsonProgressData.isNullOrEmpty()) {
            val parts = mutableListOf<MultipartBody.Part>()
            val decodedQuery = URLDecoder.decode(jsonProgressData, StandardCharsets.UTF_8.name())
            val keyValuePairs = decodedQuery.split("&")
            for (pair in keyValuePairs) {
                val (key, value) = pair.split("=")
                parts.add(MultipartBody.Part.createFormData(key, value))
            }
            api.submitOfflineXBlockProgress(courseId, blockId, parts)
            downloadDao.removeOfflineXBlockProgress(listOf(blockId))
        }
    }

    suspend fun saveVideoProgress(
        blockId: String,
        videoUrl: String,
        videoTime: Long,
        duration: Long
    ) {
        val videoProgressEntity = VideoProgressEntity(blockId, videoUrl, videoTime, duration)
        courseDao.insertVideoProgressEntity(videoProgressEntity)
    }

    suspend fun getVideoProgress(blockId: String): VideoProgressEntity {
        return courseDao.getVideoProgressByBlockId(blockId)
            ?: VideoProgressEntity(blockId, "", null, null)
    }

    fun getCourseProgress(
        courseId: String,
        isRefresh: Boolean,
        getOnlyCacheIfExist: Boolean // If true, only returns cached data if available, otherwise fetches from network
    ): Flow<CourseProgress> =
        channelFlowWithAwait {
            var courseProgress: CourseProgressEntity? = null
            if (!isRefresh) {
                courseProgress = courseDao.getCourseProgressById(courseId)
                if (courseProgress != null) {
                    trySend(courseProgress.mapToDomain())
                }
            }
            if (networkConnection.isOnline() && (!getOnlyCacheIfExist || courseProgress == null)) {
                val response = api.getCourseProgress(courseId)
                courseDao.insertCourseProgressEntity(response.mapToRoomEntity(courseId))
                trySend(response.mapToDomain())
            }
        }

    fun getDashboardProgress(
        courseId: String
    ): Flow<DashboardProgress> =
        channelFlowWithAwait {
            if (networkConnection.isOnline()) {
                val response = api.getDashboardProgress(courseId)
                trySend(response.mapToDomain())
            }
        }

    suspend fun getLiveClasses(courseId: String, type: String, page: Int) =
        api.getLiveClasses(courseId, type, page)

    suspend fun getOngoingSession(courseId: String) =
        api.getOngoingSession(courseId)

    suspend fun getJoinMeetingUrl(meetingId: String) =
        api.getJoinMeetingUrl(meetingId)

    suspend fun getEnrollmentForm(formId: String): EnrollmentForm {
        return api.getEnrollmentForm(formId).mapToDomain()
    }

    suspend fun getPrefillData(formId: String, body: Map<String, String>): Map<String, Any> {
        val response = api.getPrefillData(formId, body)
        return if (response.prefill) {
            response.answers
        } else {
            emptyMap()
        }
    }

    suspend fun checkEligibility(formId: String, body: Map<String, String>): EligibilityResult {
        val response = api.checkEligibility(formId, body)
        return EligibilityResult(
            isEligible = response.isEligible ?: false,
            message = response.message ?: ""
        )
    }

    suspend fun submitRegistration(formId: String, body: Map<String, Any>): RegistrationSubmitResponse {
        return api.submitRegistration(formId, body)
    }

    suspend fun uploadFile(formId: String, fieldKey: String, courseId: String, email: String, file: File) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val fieldKeyPart = fieldKey.toRequestBody("text/plain".toMediaTypeOrNull())
        val courseIdPart = courseId.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
        api.uploadFile(formId, fieldKeyPart, courseIdPart, emailPart, filePart)
    }

    suspend fun getCourseNotifications(courseId: String): NotificationListResponse {
        return try {
            api.getCourseNotifications(courseId).mapToDomain()
        } catch (e: Exception) {
            NotificationListResponse(false, emptyList())
        }
    }

    suspend fun getUniversities(): List<University> {
        val responseBody = try {
            api.getUniversities()
        } catch (_: Exception) {
            return emptyList()
        }
        val rawJson = try {
            responseBody.string()
        } catch (_: Exception) {
            ""
        }
        val json = try {
            JsonParser.parseString(rawJson)
        } catch (_: Exception) {
            null
        }

        if (json == null || (!json.isJsonArray && !json.isJsonObject)) {
            return emptyList()
        }

        val jsonArray = if (json.isJsonArray) {
            json.asJsonArray
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            val foundArray = obj.entrySet().firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            val nestedArray = if (foundArray == null) {
                obj.entrySet().firstOrNull { it.value.isJsonObject }?.value?.asJsonObject?.entrySet()
                    ?.firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            } else null

            foundArray ?: nestedArray ?: when {
                obj.has("results") -> obj.get("results")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("universities") -> obj.get("universities")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("university") -> obj.get("university")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("colleges") -> obj.get("colleges")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("college") -> obj.get("college")?.takeIf { it.isJsonArray }?.asJsonArray
                obj.has("data") -> obj.get("data")?.let {
                    if (it.isJsonArray) it.asJsonArray
                    else if (it.isJsonObject) it.asJsonObject.entrySet()
                        .firstOrNull { e -> e.value.isJsonArray }?.value?.asJsonArray
                    else null
                }

                else -> null
            }
        } else {
            null
        }

        return jsonArray?.mapNotNull { element ->
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
                    val universityNameSnake =
                        obj.get("university_name")?.takeIf { it.isJsonPrimitive }?.asString
                    val college = obj.get("college")?.takeIf { it.isJsonPrimitive }?.asString
                    val collegeNameSnake =
                        obj.get("college_name")?.takeIf { it.isJsonPrimitive }?.asString
                    val title = obj.get("title")?.takeIf { it.isJsonPrimitive }?.asString
                    val label = obj.get("label")?.takeIf { it.isJsonPrimitive }?.asString
                    val text = obj.get("text")?.takeIf { it.isJsonPrimitive }?.asString
                    val value = obj.get("value")?.takeIf { it.isJsonPrimitive }?.asString

                    University(
                        id = id ?: name ?: university ?: college ?: value ?: "unknown",
                        name = name ?: university ?: college ?: title ?: label ?: text ?: value ?: ""
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
    }

    suspend fun getRankingOptions(): List<RankingOption> {
        val responseBody = try {
            api.getRankingOptions()
        } catch (_: Exception) {
            return emptyList()
        }
        val rawJson = try {
            responseBody.string()
        } catch (_: Exception) {
            ""
        }
        val json = try {
            JsonParser.parseString(rawJson)
        } catch (_: Exception) {
            null
        }

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
                        id = value ?: id ?: name ?: displayName ?: "unknown",
                        label = label ?: displayName ?: name ?: ""
                    )
                } else if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                    val name = element.asString
                    RankingOption(id = name, label = name)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        } ?: emptyList()
    }

    suspend fun getUserRanking(courseId: String): UserRanking {
        return try {
            api.getUserRanking(courseId).result.mapToDomain()
        } catch (e: Exception) {
            UserRanking(0, 0)
        }
    }

    suspend fun getLeaderboard(
        courseId: String,
        page: Int,
        pageSize: Int,
        rangeType: String,
        university: String?
    ): LeaderboardList {
        return try {
            api.getLeaderboard(courseId, page, pageSize, rangeType, university).mapToDomain()
        } catch (e: Exception) {
            LeaderboardList(emptyList(), null)
        }
    }

    suspend fun markTopicCompleted(courseId: String, blockId: String) {
        try {
            api.markTopicCompleted(courseId, blockId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
