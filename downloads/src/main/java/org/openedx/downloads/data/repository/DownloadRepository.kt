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
        val cachedDownloadCoursesPreview = getCachedAndDownloadedCoursesPreview()
        if (cachedDownloadCoursesPreview.isNotEmpty()) {
            emit(cachedDownloadCoursesPreview)
        }
        val username = corePreferences.user?.username ?: ""
        if (username.isNotEmpty()) {
            try {
                val response = api.getDownloadCoursesPreview(username)
                val downloadCoursesPreview = response.map { it.mapToDomain() }
                val downloadCoursesPreviewEntity = response.map { it.mapToRoomEntity() }
                dao.insertDownloadCoursePreview(downloadCoursesPreviewEntity)
                val mergedPreviews = mergeWithDownloadedModels(downloadCoursesPreview)
                emit(mergedPreviews)
            } catch (_: Exception) {
                val fallback = getCachedAndDownloadedCoursesPreview()
                if (fallback.isNotEmpty()) {
                    emit(fallback)
                } else if (cachedDownloadCoursesPreview.isEmpty()) {
                    emit(emptyList())
                }
            }
        } else {
            if (cachedDownloadCoursesPreview.isEmpty()) {
                emit(emptyList())
            }
        }
    }

    private suspend fun getCachedAndDownloadedCoursesPreview(): List<DomainDownloadCoursePreview> {
        val cached = dao.getDownloadCoursesPreview().map { it.mapToDomain() }
        return mergeWithDownloadedModels(cached)
    }

    private suspend fun mergeWithDownloadedModels(
        previews: List<DomainDownloadCoursePreview>
    ): List<DomainDownloadCoursePreview> {
        val downloadModels = dao.readAllData().map { it.mapToDomain() }
        if (downloadModels.isEmpty()) return previews

        val previewMap = previews.associateBy { it.id }.toMutableMap()
        val downloadedByCourse = downloadModels.groupBy { it.courseId }

        downloadedByCourse.forEach { (courseId, models) ->
            if (courseId.isNotEmpty()) {
                val cachedCourseStructure = courseDao.getCourseStructureById(courseId)
                val courseName = cachedCourseStructure?.name
                    ?: models.firstOrNull()?.title
                    ?: courseId
                val courseImage = cachedCourseStructure?.media?.courseImage?.uri
                    ?: ""
                val downloadedModels = models.filter { it.downloadedState == DownloadedState.DOWNLOADED }
                val totalDownloadedSize = if (downloadedModels.isNotEmpty()) downloadedModels.sumOf { it.size } else models.sumOf { it.size }

                val existingPreview = previewMap[courseId]
                val updatedPreview = if (existingPreview != null) {
                    existingPreview.copy(
                        name = if (existingPreview.name.isEmpty() || existingPreview.name == courseId) courseName else existingPreview.name,
                        image = if (existingPreview.image.isEmpty()) courseImage else existingPreview.image,
                        totalSize = if (totalDownloadedSize > 0) totalDownloadedSize else existingPreview.totalSize
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
                dao.insertDownloadCoursePreview(listOf(roomEntity))
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
