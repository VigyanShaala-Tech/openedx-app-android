package org.openedx.course.presentation.unit.pdf

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.course.data.repository.CourseRepository
import org.openedx.foundation.presentation.BaseViewModel

class PdfUnitViewModel(
    val courseId: String,
    val blockId: String,
    private val repository: CourseRepository,
    private val notifier: CourseNotifier,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PdfUnitUIState>(PdfUnitUIState.Loading)
    val uiState = _uiState.asStateFlow()

    private var isBlockAlreadyCompleted = false

    fun markBlockCompleted() {
        if (!isBlockAlreadyCompleted) {
            viewModelScope.launch {
                try {
                    isBlockAlreadyCompleted = true
                    repository.markBlocksCompletion(courseId, listOf(blockId))
                    repository.markTopicCompleted(courseId, blockId)
                    notifier.send(CourseCompletionSet())
                } catch (e: Exception) {
                    e.printStackTrace()
                    isBlockAlreadyCompleted = false
                }
            }
        }
    }
}

sealed class PdfUnitUIState {
    object Loading : PdfUnitUIState()
}
