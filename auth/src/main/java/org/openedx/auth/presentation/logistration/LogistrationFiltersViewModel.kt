package org.openedx.auth.presentation.logistration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.openedx.foundation.presentation.BaseViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FilterOptions(
    val options: Map<String, List<String>>
)

class LogistrationFiltersRepository(
    private val catalogApi: org.openedx.auth.data.api.CatalogApi,
    private val gson: Gson
) {
    suspend fun getFilterOptions(): FilterOptions {
        val json = catalogApi.getFiltersRaw().string()
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        val map: Map<String, List<String>> = try {
            gson.fromJson<Map<String, List<String>>>(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
        val normalized = map.mapValues { (key, values) ->
            val allLabel = "All ${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            listOf(allLabel) + values
        }
        return FilterOptions(options = normalized)
    }
}

data class FiltersState(
    val options: FilterOptions = FilterOptions(emptyMap()),
    val selected: Map<String, String> = emptyMap()
)

class LogistrationFiltersViewModel(
    private val repository: LogistrationFiltersRepository
) : BaseViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(FiltersState())
    val state = _state

    init {
        viewModelScope.launch {
            val opts = repository.getFilterOptions()
            val initialSelected = opts.options.mapValues { (_, values) -> values.firstOrNull().orEmpty() }
            _state.value = FiltersState(options = opts, selected = initialSelected)
        }
    }

    fun select(key: String, value: String) {
        _state.value = _state.value.copy(selected = _state.value.selected + (key to value))
    }

    fun reset() {
        val initialSelected = _state.value.options.options.mapValues { (_, values) -> values.firstOrNull().orEmpty() }
        _state.value = _state.value.copy(selected = initialSelected)
    }
}
