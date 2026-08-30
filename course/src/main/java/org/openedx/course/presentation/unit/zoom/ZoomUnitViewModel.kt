package org.openedx.course.presentation.unit.zoom

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openedx.core.data.storage.CorePreferences
import org.openedx.core.domain.model.Block
import org.openedx.course.domain.interactor.CourseInteractor
import org.openedx.foundation.presentation.BaseViewModel

class ZoomUnitViewModel(
    private val blockId: String,
    private val courseId: String,
    private val interactor: CourseInteractor,
    private val preferencesManager: CorePreferences
) : BaseViewModel() {

    private val _block = MutableStateFlow<Block?>(null)
    val block = _block.asStateFlow()

    val userName get() = preferencesManager.user?.name ?: "Learner"
    val userId get() = preferencesManager.user?.id?.toString() ?: ""

    init {
        loadBlock()
    }

    private fun loadBlock() {
        viewModelScope.launch {
            try {
                val courseStructure = interactor.getCourseStructureFromCache(courseId)
                _block.value = courseStructure.blockData.find { it.id == blockId }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
