package org.openedx.course.presentation.leaderboard

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.openedx.course.data.model.LeaderboardEntry
import org.openedx.course.data.model.RankingOption
import org.openedx.course.data.model.University
import org.openedx.course.data.model.UserRanking
import org.openedx.course.domain.interactor.LeaderboardInteractor
import org.openedx.foundation.extension.isInternetError
import org.openedx.foundation.presentation.BaseViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.system.ResourceManager
import org.openedx.core.R as coreR

class LeaderboardViewModel(
    val courseId: String,
    private val interactor: LeaderboardInteractor,
    private val resourceManager: ResourceManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiMessage = MutableSharedFlow<UIMessage>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val fetchedUniversities = try {
                    interactor.getUniversities()
                } catch (e: Exception) {
                    _uiMessage.emit(UIMessage.SnackBarMessage("Failed to load colleges: ${e.message}"))
                    emptyList()
                }
                
                // Filter out if "All Colleges" or similar already exists in fetched list to avoid duplicates
                val filteredFetched = fetchedUniversities.filter {
                    val name = (it.name ?: it.universityName ?: it.collegeName ?: it.label ?: it.text ?: "").trim()
                    !name.equals("All Colleges", ignoreCase = true) && 
                    !name.equals("All Students", ignoreCase = true) &&
                    it.id != "0"
                }
                val universities = listOf(University("0", "All Colleges")) + filteredFetched

                val rankingOptions = try {
                    interactor.getRankingOptions().ifEmpty { listOf(RankingOption("all", "All Students")) }
                } catch (e: Exception) {
                    _uiMessage.emit(UIMessage.SnackBarMessage("Failed to load ranking options: ${e.message}"))
                    listOf(RankingOption("all", "All Students"))
                }

                val userRanking = try {
                    interactor.getUserRanking(courseId)
                } catch (_: Exception) {
                    null
                }
                
                _uiState.update { 
                    it.copy(
                        universities = universities,
                        rankingOptions = rankingOptions,
                        userRanking = userRanking,
                        isLoading = false,
                        selectedUniversity = it.selectedUniversity ?: universities.first(),
                        selectedRankingOption = if ((it.selectedRankingOption.id == "all" || it.selectedRankingOption.id == null) && rankingOptions.isNotEmpty() && rankingOptions.first().id != "all")
                            rankingOptions.first() 
                        else 
                            it.selectedRankingOption
                    )
                }
                fetchLeaderboard()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                handleError(e)
            }
        }
    }

    fun onUniversitySelected(university: University?) {
        _uiState.update { it.copy(selectedUniversity = university, page = 1, leaderboardEntries = emptyList()) }
        fetchLeaderboard()
    }

    fun onRankingOptionSelected(option: RankingOption) {
        _uiState.update { it.copy(selectedRankingOption = option, page = 1, leaderboardEntries = emptyList()) }
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        val currentState = _uiState.value
        if (currentState.isLoading && currentState.page > 1) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val response = interactor.getLeaderboard(
                    courseId = courseId,
                    page = currentState.page,
                    pageSize = 10,
                    rangeType = currentState.selectedRankingOption.value ?: currentState.selectedRankingOption.id ?: "all",
                    university = if (currentState.selectedUniversity?.id == "0" || currentState.selectedUniversity?.id == null) null else currentState.selectedUniversity?.let { it.name ?: it.universityName ?: it.universityNameSnake ?: it.collegeName ?: it.collegeNameSnake ?: it.title ?: it.label ?: it.text ?: it.value }
                )
                
                _uiState.update { 
                    val newEntries = if (currentState.page == 1) response.results else it.leaderboardEntries + response.results
                    it.copy(
                        leaderboardEntries = newEntries,
                        hasMore = response.pagination?.next != null && response.results.isNotEmpty(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                handleError(e)
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoading) {
            _uiState.update { it.copy(page = it.page + 1) }
            fetchLeaderboard()
        }
    }

    private suspend fun handleError(e: Exception) {
        val errorMessage = if (e.isInternetError()) {
            resourceManager.getString(coreR.string.core_error_no_connection)
        } else {
            e.message ?: resourceManager.getString(coreR.string.core_error_unknown_error)
        }
        _uiMessage.emit(UIMessage.SnackBarMessage(errorMessage))
    }
}

data class LeaderboardUIState(
    val isLoading: Boolean = false,
    val universities: List<University> = emptyList(),
    val rankingOptions: List<RankingOption> = emptyList(),
    val userRanking: UserRanking? = null,
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val selectedUniversity: University? = null,
    val selectedRankingOption: RankingOption = RankingOption("all", "All Students"),
    val page: Int = 1,
    val hasMore: Boolean = false
)
