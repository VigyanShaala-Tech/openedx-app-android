package org.openedx.downloads.data.repository

import kotlinx.coroutines.flow.flow
import org.openedx.core.data.api.CourseApi
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.data.storage.CourseDao
import org.openedx.core.domain.model.CourseStructure
import org.openedx.core.exception.NoCachedDataException
import org.openedx.core.module.db.DownloadDao
import org.openedx.core.module.db.DownloadedState

import org.openedx.core.domain.model.DownloadCoursePreview as DomainDownloadCoursePreview

class DownloadRepository(
    private val api: CourseApi,
    private val dao: DownloadDao,
    private val courseDao: CourseDao,
    private val corePreferences: CorePreferences,
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
                val downloadCoursesPreviewEntity = response.map { it.mapToRoomEntity() }
                if (downloadCoursesPreviewEntity.isNotEmpty()) {
                    runCatching { dao.insertDownloadCoursePreview(downloadCoursesPreviewEntity) }
                }
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
        val previewMap = previews.associateBy { it.id }.toMutableMap()

        val roomPreviews = runCatching { dao.getDownloadCoursesPreview().map { it.mapToDomain() } }.getOrDefault(emptyList())
        roomPreviews.forEach { roomPreview ->
            if (!previewMap.containsKey(roomPreview.id) && roomPreview.id.isNotEmpty()) {
                previewMap[roomPreview.id] = roomPreview
            }
        }

        if (downloadModels.isEmpty()) return previewMap.values.toList()

        val downloadedByCourse = downloadModels.groupBy { it.courseId }

        downloadedByCourse.forEach { (courseId, models) ->
            if (courseId.isNotEmpty()) {
                val cachedCourseStructure = runCatching { courseDao.getCourseStructureById(courseId) }.getOrNull()
                val courseName = cachedCourseStructure?.name
                    ?: models.firstOrNull()?.title
                    ?: courseId
                val courseImage = cachedCourseStructure?.media?.courseImage?.uri
                    ?: cachedCourseStructure?.media?.image?.large
                    ?: ""
                val downloadedModels = models.filter { it.downloadedState == DownloadedState.DOWNLOADED }
                val totalDownloadedSize = if (downloadedModels.isNotEmpty()) downloadedModels.sumOf { it.size } else models.sumOf { it.size }

                val existingPreview = previewMap[courseId]
                val updatedPreview = if (existingPreview != null) {
                    existingPreview.copy(
                        name = if (existingPreview.name.isEmpty() || existingPreview.name == courseId) courseName else existingPreview.name,
                        image = if (existingPreview.image.isEmpty()) courseImage else existingPreview.image,
                        totalSize = maxOf(existingPreview.totalSize, totalDownloadedSize)
                    )
                } else {
                    DomainDownloadCoursePreview(
                        id = courseId,
                        name = courseName,
                        image = courseImage,
                        totalSize = totalDownloadedSize
                    )
                }
                previewMap[courseId] = updatedPreview

                val roomEntity = org.openedx.core.data.model.room.DownloadCoursePreview(
                    id = courseId,
                    name = updatedPreview.name,
                    image = updatedPreview.image,
                    totalSize = updatedPreview.totalSize
                )
                runCatching { dao.insertDownloadCoursePreview(listOf(roomEntity)) }
            }
        }
        return previewMap.values.toList()
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
