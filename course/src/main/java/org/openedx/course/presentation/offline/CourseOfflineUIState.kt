package org.openedx.course.presentation.offline

import org.openedx.core.module.db.DownloadModel
import org.openedx.core.module.db.DownloadedState
import org.openedx.core.module.db.FileType

data class CourseOfflineUIState(
    val isHaveDownloadableBlocks: Boolean,
    val largestDownloads: List<DownloadModel>,
    val isDownloading: Boolean,
    val readyToDownloadSize: String,
    val downloadedSize: String,
    val progressBarValue: Float,
    val downloadableItems: List<DownloadableItemModel> = emptyList()
)

data class DownloadableItemModel(
    val id: String,
    val title: String,
    val size: Long,
    val downloadedState: DownloadedState,
    val type: FileType = FileType.VIDEO
)
