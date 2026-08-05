package org.openedx.course.presentation.unit.pdf

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.system.notifier.CourseCompletionSet
import org.openedx.core.system.notifier.CourseNotifier
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.foundation.presentation.BaseViewModel

class PdfUnitViewModel(
    val courseId: String,
    val blockId: String,
    private val notifier: CourseNotifier,
    private val interactor: CourseInteractor,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PdfUnitUIState>(PdfUnitUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        markTopicCompleted()
    }

    private fun markTopicCompleted() {
        viewModelScope.launch {
            interactor.markTopicCompleted(courseId, blockId)
        }
    }

    fun notifyCompletionSet() {
        viewModelScope.launch {
            notifier.send(CourseCompletionSet())
        }
    }
}

sealed class PdfUnitUIState {
    object Loading : PdfUnitUIState()
}
