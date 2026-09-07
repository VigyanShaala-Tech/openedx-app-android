package org.openedx.course.presentation.unit.video

import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import kotlinx.coroutines.launch
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.core.system.notifier.CourseVideoPositionChanged
import org.openedx.course.data.repository.CourseRepository
import org.openedx.course.presentation.CourseAnalytics

class VideoViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository,
    private val notifier: CourseNotifier,
    private val preferencesManager: CorePreferences,
    courseAnalytics: CourseAnalytics,
) : BaseVideoViewModel(courseId, courseAnalytics) {

    var videoUrl = ""
    var currentVideoTime = 0L
    var duration = 0L
    var isPlaying: Boolean? = null

    private var isBlockAlreadyCompleted = false

    fun sendTime() {
        if (currentVideoTime != C.TIME_UNSET) {
            viewModelScope.launch {
                notifier.send(
                    CourseVideoPositionChanged(
                        videoUrl,
                        currentVideoTime,
                        duration,
                        isPlaying == true
                    )
                )
            }
        }
    }

    fun markTopicCompleted(blockId: String) {
        viewModelScope.launch {
            courseRepository.markTopicCompleted(courseId, blockId)
        }
    }

    fun saveUserState(blockId: String, positionMs: Long) {
        if (courseId.isNotEmpty() && blockId.isNotEmpty() && positionMs >= 0) {
            val seconds = positionMs / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            val formattedTime = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
            viewModelScope.launch {
                try {
                    courseRepository.saveUserState(courseId, blockId, formattedTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun markBlockCompleted(blockId: String, medium: String) {
        if (!isBlockAlreadyCompleted) {
            logLoadedCompletedEvent(videoUrl, false, currentVideoTime, medium)
            saveUserState(blockId, currentVideoTime)
            viewModelScope.launch {
                try {
                    isBlockAlreadyCompleted = true
                    courseRepository.markBlocksCompletion(
                        courseId,
                        listOf(blockId)
                    )
                    courseRepository.markTopicCompleted(courseId, blockId)
                    notifier.send(CourseCompletionSet())
                } catch (e: Exception) {
                    e.printStackTrace()
                    isBlockAlreadyCompleted = false
                }
            }
        }
    }

    fun getVideoQuality() = preferencesManager.videoSettings.videoStreamingQuality
}
