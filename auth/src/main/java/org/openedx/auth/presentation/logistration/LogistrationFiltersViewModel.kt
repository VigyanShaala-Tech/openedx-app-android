package org.openedx.auth.presentation.logistration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.foundation.presentation.BaseViewModel

data class FilterOptions(
    val categories: List<String>,
    val levels: List<String>,
    val subjects: List<String>,
)

class LogistrationFiltersRepository(
    private val catalogApi: org.openedx.auth.data.api.CatalogApi
) {
    suspend fun getFilterOptions(): FilterOptions {
        val resp = catalogApi.getFilters()
        return FilterOptions(
            categories = listOf("All Categories") + resp.categories,
            levels = listOf("All Levels") + resp.levels,
            subjects = listOf("All Subjects") + resp.subjects
        )
    }
}

data class FiltersState(
    val options: FilterOptions = FilterOptions(emptyList(), emptyList(), emptyList()),
    val selectedCategory: String = "",
    val selectedLevel: String = "",
    val selectedSubject: String = ""
)

class LogistrationFiltersViewModel(
    private val repository: LogistrationFiltersRepository
) : BaseViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(FiltersState())
    val state = _state

    init {
        viewModelScope.launch {
            val opts = repository.getFilterOptions()
            _state.value = FiltersState(
                options = opts,
                selectedCategory = opts.categories.firstOrNull().orEmpty(),
                selectedLevel = opts.levels.firstOrNull().orEmpty(),
                selectedSubject = opts.subjects.firstOrNull().orEmpty()
            )
        }
    }

    fun selectCategory(value: String) {
        _state.value = _state.value.copy(selectedCategory = value)
    }
    fun selectLevel(value: String) {
        _state.value = _state.value.copy(selectedLevel = value)
    }
    fun selectSubject(value: String) {
        _state.value = _state.value.copy(selectedSubject = value)
    }
}
