package org.openedx.discovery.presentation

import org.openedx.discovery.domain.model.Course

sealed class DiscoveryUIState {
    data class Courses(val courses: List<Course>, val totalCount: Int = 0) : DiscoveryUIState()
    data object Loading : DiscoveryUIState()
}
