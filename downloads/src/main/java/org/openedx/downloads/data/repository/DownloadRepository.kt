package org.openedx.downloads.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.openedx.core.config.Config
import org.openedx.core.data.api.CourseApi
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.data.storage.CourseDao
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.exception.NoCachedDataException
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.db.DownloadedState
import org.openedx.core.utils.Sha1Util
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.utils.FileUtil
import java.io.File

import org.openedx.core.domain.model.DownloadCoursePreview as DomainDownloadCoursePreview

class DownloadRepository(
    private val api: CourseApi,
    private val dao: DownloadDao,
    private val courseDao: CourseDao,
    private val corePreferences: CorePreferences,
    private val fileUtil: FileUtil,
    private val config: Config,
) {
    fun getDownloadCoursesPreview(refresh: Boolean) = flow {
        val cachedDownloadCoursesPreview = runCatching { getCachedAndDownloadedCoursesPreview() }.getOrDefault(emptyList())
        if (cachedDownloadCoursesPreview.isNotEmpty()) {
            emit(cachedDownloadCoursesPreview)
        }
        val username = corePreferences.user?.username ?: ""
        if (username.isNotEmpty()) {
            try {
                val response = api.getDownloadCoursesPreview(username)
                val downloadCoursesPreview = response.map { it.mapToDomain() }
                val mergedPreviews = mergeWithDownloadedModels(downloadCoursesPreview)
                if (mergedPreviews.isNotEmpty()) {
                    emit(mergedPreviews)
                } else if (cachedDownloadCoursesPreview.isNotEmpty()) {
                    emit(cachedDownloadCoursesPreview)
                } else {
                    emit(emptyList())
                }
            } catch (_: Exception) {
                val fallback = runCatching { getCachedAndDownloadedCoursesPreview() }.getOrDefault(emptyList())
                if (fallback.isNotEmpty()) {
                    emit(fallback)
                } else if (cachedDownloadCoursesPreview.isNotEmpty()) {
                    emit(cachedDownloadCoursesPreview)
                } else {
                    emit(emptyList())
                }
            }
        } else {
            if (cachedDownloadCoursesPreview.isNotEmpty()) {
                emit(cachedDownloadCoursesPreview)
            } else {
                emit(emptyList())
            }
        }
    }

    private suspend fun getCachedAndDownloadedCoursesPreview(): List<DomainDownloadCoursePreview> {
        val cached = runCatching { dao.getDownloadCoursesPreview().map { it.mapToDomain() } }.getOrDefault(emptyList())
        val merged = mergeWithDownloadedModels(cached)
        if (merged.isNotEmpty()) {
            return merged
        }
        return cached
    }

    private suspend fun mergeWithDownloadedModels(
        previews: List<DomainDownloadCoursePreview>
    ): List<DomainDownloadCoursePreview> {
        val downloadModels = runCatching { dao.readAllData().map { it.mapToDomain() } }.getOrDefault(emptyList())
        val roomPreviews = runCatching { dao.getDownloadCoursesPreview().map { it.mapToDomain() } }.getOrDefault(emptyList())

        val previewMap = mutableMapOf<String, DomainDownloadCoursePreview>()
        previews.forEach { preview ->
            if (preview.id.isNotEmpty()) {
                previewMap[preview.id] = preview
            }
        }
        roomPreviews.forEach { roomPreview ->
            if (roomPreview.id.isNotEmpty() && !previewMap.containsKey(roomPreview.id)) {
                previewMap[roomPreview.id] = roomPreview
            }
        }

        val allCourseIds = (previewMap.keys + downloadModels.map { it.courseId }).filter { it.isNotEmpty() }.toSet()

        if (allCourseIds.isEmpty()) return emptyList()

        val apiHostUrl = config.getApiHostURL()

        allCourseIds.forEach { courseId ->
            val existingPreview = previewMap[courseId]
            val models = downloadModels.filter { it.courseId == courseId }

            val cachedCourseStructure = runCatching { courseDao.getCourseStructureById(courseId) }.getOrNull()
            val cachedEnrollmentDetails = runCatching { courseDao.getCourseEnrollmentDetailsById(courseId) }.getOrNull()

            val courseName = existingPreview?.name?.takeIf { it.isNotEmpty() && it != courseId }
                ?: cachedCourseStructure?.name?.takeIf { it.isNotEmpty() }
                ?: cachedEnrollmentDetails?.courseInfoOverview?.name?.takeIf { it.isNotEmpty() }
                ?: models.firstOrNull { it.title.isNotEmpty() }?.title
                ?: courseId

            val rawCourseImage = existingPreview?.image?.takeIf { it.isNotEmpty() && !isLocalFilePath(it) }
                ?: cachedCourseStructure?.media?.courseImage?.uri?.takeIf { it.isNotEmpty() }
                ?: cachedCourseStructure?.media?.image?.large?.takeIf { it.isNotEmpty() }
                ?: cachedCourseStructure?.media?.image?.raw?.takeIf { it.isNotEmpty() }
                ?: cachedEnrollmentDetails?.courseInfoOverview?.media?.courseImage?.uri?.takeIf { it.isNotEmpty() }
                ?: cachedEnrollmentDetails?.courseInfoOverview?.media?.image?.large?.takeIf { it.isNotEmpty() }
                ?: existingPreview?.image
                ?: ""

            val courseImage = ensureLocalCourseImage(
                courseId = courseId,
                rawCourseImage = rawCourseImage,
                apiHostUrl = apiHostUrl
            )

            val downloadedModels = models.filter { it.downloadedState == DownloadedState.DOWNLOADED }
            val totalDownloadedSize = if (downloadedModels.isNotEmpty()) downloadedModels.sumOf { it.size } else models.sumOf { it.size }
            val totalSize = maxOf(existingPreview?.totalSize ?: 0L, totalDownloadedSize)

            val updatedPreview = DomainDownloadCoursePreview(
                id = courseId,
                name = courseName,
                image = if (courseImage.isNotEmpty()) courseImage else (existingPreview?.image ?: ""),
                totalSize = totalSize
            )
            previewMap[courseId] = updatedPreview

            val roomEntity = org.openedx.core.data.model.room.DownloadCoursePreview(
                id = courseId,
                name = updatedPreview.name,
                image = updatedPreview.image,
                totalSize = updatedPreview.totalSize
            )
            runCatching { dao.insertDownloadCoursePreview(listOf(roomEntity)) }
        }

        return previewMap.values.toList()
    }

    private fun isLocalFilePath(path: String): Boolean {
        if (path.isEmpty()) return false
        val file = File(path.removePrefix("file://"))
        return file.exists() && file.isFile
    }

    private suspend fun ensureLocalCourseImage(
        courseId: String,
        rawCourseImage: String,
        apiHostUrl: String
    ): String {
        if (rawCourseImage.isEmpty()) return ""

        val rawFile = File(rawCourseImage.removePrefix("file://"))
        if (rawFile.exists() && rawFile.isFile && rawFile.length() > 0) {
            return rawFile.absolutePath
        }

        val appDir = fileUtil.getExternalAppDir()
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val imageFile = File(appDir, "course_image_${Sha1Util.SHA1(courseId)}.jpg")
        if (imageFile.exists() && imageFile.length() > 0) {
            return imageFile.absolutePath
        }

        val fullUrl = if (rawCourseImage.startsWith("http://") || rawCourseImage.startsWith("https://")) {
            rawCourseImage
        } else {
            rawCourseImage.toImageLink(apiHostUrl)
        }

        if (fullUrl.startsWith("http://") || fullUrl.startsWith("https://")) {
            runCatching {
                withContext(Dispatchers.IO) {
                    val url = java.net.URL(fullUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.instanceFollowRedirects = true
                    connection.connect()
                    if (connection.responseCode == 200) {
                        connection.inputStream.use { input ->
                            imageFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
        }

        if (imageFile.exists() && imageFile.length() > 0) {
            return imageFile.absolutePath
        }
        return fullUrl
    }

    suspend fun getCourseStructureFromCache(courseId: String): CourseStructure {
        val cachedCourseStructure = courseDao.getCourseStructureById(courseId)
        if (cachedCourseStructure != null) {
            return cachedCourseStructure.mapToDomain()
        } else {
            throw NoCachedDataException()
        }
    }

    suspend fun getCourseStructure(courseId: String): CourseStructure {
        try {
            val response = api.getCourseStructure(
                cacheControlHeaderParam = "stale-if-error=0",
                blocksApiVersion = "v4",
                username = corePreferences.user?.username,
                courseId = courseId
            )
            courseDao.insertCourseStructureEntity(response.mapToRoomEntity())
            return response.mapToDomain()
        } catch (_: Exception) {
            return getCourseStructureFromCache(courseId)
        }
    }

    suspend fun getDownloadModelsByCourseIds(courseId: String) =
        dao.getDownloadModelsByCourseIds(courseId).map { it.mapToDomain() }
}
